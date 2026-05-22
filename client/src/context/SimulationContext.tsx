import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { format } from 'date-fns';
import * as api from '../services/citizenApi';
import { loadZaloProfile } from '../lib/zalo';

export interface Appointment {
  id: string;
  queueNumber: string;
  citizenName: string;
  procedure: string;
  time: string;
  status: 'scheduled' | 'waiting' | 'ready' | 'serving' | 'completed' | 'cancelled' | 'upcoming' | 'processing' | 'supplement';
  date: string;
  counter?: string;
  cancelReason?: string;
  code?: string;
  zaloLinked?: boolean;
}

interface SimulationContextType {
  myAppointments: Appointment[];
  citizenId: string;
  citizenName: string;
  citizenPhone: string;
  zaloId: string;
  zaloName: string;
  zaloAvatar?: string;
  setCitizenId: (id: string) => void;
  setCitizenName: (name: string) => void;
  setCitizenPhone: (phone: string) => void;
  bookAppointment: (data: {
    procedureId: number;
    date: Date;
    time: string;
    phone?: string;
    email?: string;
    notes?: string;
  }) => Promise<api.AppointmentResponse | null>;
  cancelAppointment: (id: string, reason?: string) => Promise<void>;
  refreshAppointments: () => Promise<void>;
  isLoading: boolean;
}

const SimulationContext = createContext<SimulationContextType | undefined>(undefined);

const mapStatus = (apiStatus: string): Appointment['status'] => {
  const statusMap: Record<string, Appointment['status']> = {
    CHO_XU_LY: 'waiting',
    'CHỜ XỬ LÝ': 'waiting',
    'CHỜ TIẾP': 'waiting',
    'ĐANG XỬ LÝ': 'serving',
    'HOÀN THÀNH': 'completed',
    'ĐÃ HỦY': 'cancelled',
    'SẮP TỚI HẠN': 'upcoming',
    PENDING: 'scheduled',
    IN_QUEUE: 'waiting',
    PROCESSING: 'serving',
    COMPLETED: 'completed',
    CANCELLED: 'cancelled',
    RECEIVED: 'completed',
    SUPPLEMENT: 'supplement',
  };

  return statusMap[apiStatus] || 'waiting';
};

export const SimulationProvider = ({ children }: { children: React.ReactNode }) => {
  const [myAppointments, setMyAppointments] = useState<Appointment[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const [citizenId, setCitizenId] = useState('');
  const [citizenName, setCitizenName] = useState('');
  const [citizenPhone, setCitizenPhone] = useState('');
  const [zaloId, setZaloId] = useState('');
  const [zaloName, setZaloName] = useState('');
  const [zaloAvatar, setZaloAvatar] = useState<string | undefined>(undefined);

  const refreshAppointments = useCallback(async () => {
    if (!zaloId) {
      setMyAppointments([]);
      return;
    }

    setIsLoading(true);
    try {
      const appointments = await api.getMyAppointments(zaloId);
      const mapped: Appointment[] = appointments.map((app) => ({
        id: app.id.toString(),
        queueNumber: app.queueDisplay || '',
        citizenName: app.citizenName || citizenName || zaloName || 'Công dân',
        procedure: app.procedureName,
        time: app.appointmentTime || '',
        status: mapStatus(app.status),
        date: app.appointmentDate || app.createdAt?.split('T')[0] || '',
        counter: app.counter || undefined,
        code: app.code,
        zaloLinked: app.zaloLinked || false,
      }));

      setMyAppointments(mapped);
    } catch (error) {
      console.error('Failed to fetch appointments:', error);
    } finally {
      setIsLoading(false);
    }
  }, [citizenName, zaloId, zaloName]);

  useEffect(() => {
    let cancelled = false;

    const syncZaloIdentity = async () => {
      const profile = await loadZaloProfile();
      if (!profile || cancelled) {
        return;
      }

      setZaloId(profile.id);
      setZaloAvatar(profile.avatar);

      if (profile.name) {
        setZaloName(profile.name);
        setCitizenName((current) => current || profile.name || '');
      }

      try {
        const syncedProfile = await api.syncZaloProfile({
          zaloId: profile.id,
          zaloName: profile.name,
          avatar: profile.avatar,
          oaUserId: profile.oaUserId,
        });

        if (cancelled) {
          return;
        }

        if (syncedProfile.zaloName) {
          setZaloName(syncedProfile.zaloName);
          setCitizenName((current) => current || syncedProfile.zaloName || '');
        }

        if (syncedProfile.avatar) {
          setZaloAvatar(syncedProfile.avatar);
        }

        if (syncedProfile.phoneNumber) {
          setCitizenPhone(syncedProfile.phoneNumber);
        }
      } catch (error) {
        console.warn('Unable to sync Zalo profile with backend', error);
      }
    };

    void syncZaloIdentity();

    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    void refreshAppointments();
  }, [refreshAppointments]);

  const bookAppointment = async (data: {
    procedureId: number;
    date: Date;
    time: string;
    phone?: string;
    email?: string;
    notes?: string;
  }): Promise<api.AppointmentResponse | null> => {
    if (!zaloId) {
      console.error('Booking requires an authenticated Zalo account');
      return null;
    }

    try {
      setIsLoading(true);

      const request: api.CreateAppointmentRequest = {
        procedureId: data.procedureId,
        appointmentDate: format(data.date, 'yyyy-MM-dd'),
        appointmentTime: data.time,
        citizenName: citizenName || zaloName || 'Công dân Zalo',
        citizenCccd: citizenId,
        citizenPhone: data.phone || citizenPhone,
        citizenEmail: data.email,
        zaloId,
        zaloName: zaloName || citizenName,
        notes: data.notes,
      };

      const result = await api.createAppointment(request);

      const newAppointment: Appointment = {
        id: result.id.toString(),
        queueNumber: result.queueDisplay,
        citizenName: citizenName || zaloName || 'Công dân Zalo',
        procedure: result.procedureName,
        time: result.appointmentTime,
        status: 'scheduled',
        date: result.appointmentDate,
        code: result.code,
        zaloLinked: result.zaloLinked || true,
      };

      if (data.phone) {
        setCitizenPhone(data.phone);
      }

      setMyAppointments((prev) => [newAppointment, ...prev]);
      return result;
    } catch (error) {
      console.error('Failed to book appointment:', error);
      return null;
    } finally {
      setIsLoading(false);
    }
  };

  const cancelAppointment = async (id: string, reason: string = 'Cancelled by user') => {
    if (!zaloId) {
      throw new Error('Cần đăng nhập Zalo để hủy lịch');
    }

    try {
      setIsLoading(true);
      await api.cancelAppointment(parseInt(id, 10), zaloId);

      setMyAppointments((prev) =>
        prev.map((appointment) =>
          appointment.id === id
            ? { ...appointment, status: 'cancelled' as const, cancelReason: reason }
            : appointment,
        ),
      );
    } catch (error) {
      console.error('Failed to cancel appointment:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <SimulationContext.Provider
      value={{
        myAppointments,
        citizenId,
        citizenName,
        citizenPhone,
        zaloId,
        zaloName,
        zaloAvatar,
        setCitizenId,
        setCitizenName,
        setCitizenPhone,
        bookAppointment,
        cancelAppointment,
        refreshAppointments,
        isLoading,
      }}
    >
      {children}
    </SimulationContext.Provider>
  );
};

export const useSimulation = () => {
  const context = useContext(SimulationContext);
  if (!context) {
    throw new Error('useSimulation must be used within a SimulationProvider');
  }

  return context;
};
