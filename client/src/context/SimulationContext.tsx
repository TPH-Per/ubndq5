import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { format, isSameDay, addDays } from 'date-fns';
import * as api from '../services/citizenApi';
import mockZaloData from '../data/mockZaloAccount.json';

// Types
export interface Appointment {
  id: string;
  queueNumber: string;
  citizenName: string;
  procedure: string;
  time: string;
  status: 'waiting' | 'ready' | 'serving' | 'completed' | 'cancelled' | 'upcoming' | 'processing';
  date: string;
  counter?: string;
  cancelReason?: string;
  code?: string;
  zaloLinked?: boolean;
}



export interface FileRecord {
  id: string;
  fileCode: string;
  citizenName: string;
  procedure: string;
  startTime: string;
  status: 'processing' | 'completed' | 'need_docs' | 'rejected';
  staff: string;
  deadline: string;
}

interface SimulationContextType {
  waitingList: Appointment[];
  activeFiles: FileRecord[];
  currentServing: Appointment | null;
  completedList: Appointment[];
  myAppointments: Appointment[];
  citizenId: string;
  citizenName: string;
  setCitizenId: (id: string) => void;
  setCitizenName: (name: string) => void;
  stats: {
    waiting: number;
    completed: number;
    avgWait: number;
    processing: number;
  };
  bookAppointment: (data: any) => Promise<api.AppointmentResponse | null>;
  callNext: () => void;
  completeCurrent: () => void;
  cancelAppointment: (id: string, reason?: string) => Promise<void>;
  approveToProcessing: (id: string) => void;
  updateFile: (id: string, updates: Partial<FileRecord>) => void;
  getQueueDataByDate: (date: Date) => Appointment[];
  refreshAppointments: () => Promise<void>;
  isLoading: boolean;
}

const SimulationContext = createContext<SimulationContextType | undefined>(undefined);

// Default citizen for demo (from mock Zalo data)
const DEFAULT_ZALO_ID = mockZaloData.zaloUser.id;
const DEFAULT_ZALO_NAME = mockZaloData.zaloUser.name;
const DEFAULT_CITIZEN_ID = mockZaloData.citizen.citizenId;
const DEFAULT_CITIZEN_NAME = mockZaloData.citizen.fullName;

export const SimulationProvider = ({ children }: { children: React.ReactNode }) => {
  const [waitingList, setWaitingList] = useState<Appointment[]>([]);
  const [activeFiles, setActiveFiles] = useState<FileRecord[]>([]);
  const [currentServing, setCurrentServing] = useState<Appointment | null>(null);
  const [completedList, setCompletedList] = useState<Appointment[]>([]);
  const [myAppointments, setMyAppointments] = useState<Appointment[]>([]);
  const [historyCache, setHistoryCache] = useState<Record<string, Appointment[]>>({});
  const [isLoading, setIsLoading] = useState(false);

  // Citizen identity (linked to Zalo account)
  const [citizenId, setCitizenId] = useState<string>(DEFAULT_CITIZEN_ID);
  const [citizenName, setCitizenName] = useState<string>(DEFAULT_CITIZEN_NAME);
  const [zaloId] = useState<string>(DEFAULT_ZALO_ID);
  const [zaloName] = useState<string>(DEFAULT_ZALO_NAME);

  // Map API status to local status
  const mapStatus = (apiStatus: string): Appointment['status'] => {
    const statusMap: Record<string, Appointment['status']> = {
      // Vietnamese labels
      'CHỜ XỬ LÝ': 'waiting',
      'CHỜ TIẾP': 'waiting',
      'ĐANG XỬ LÝ': 'serving',
      'HOÀN THÀNH': 'completed',
      'ĐÃ HỦY': 'cancelled',
      'SẮP TỚI HẠN': 'upcoming',
      // English labels from backend
      'PENDING': 'waiting',
      'IN_QUEUE': 'waiting',
      'PROCESSING': 'serving',
      'COMPLETED': 'completed',
      'CANCELLED': 'cancelled',
      'RECEIVED': 'completed',
      'SUPPLEMENT': 'waiting',
    };
    return statusMap[apiStatus] || 'waiting';
  };

  // Fetch appointments on mount and when citizenId changes
  const refreshAppointments = useCallback(async () => {
    if (!citizenId) return;

    setIsLoading(true);
    try {
      const appointments = await api.getMyAppointments(citizenId);

      const mapped: Appointment[] = appointments.map(app => ({
        id: app.id.toString(),
        queueNumber: app.queueDisplay || '',
        citizenName: app.citizenName || citizenName,
        procedure: app.procedureName,
        time: app.appointmentTime || '',
        status: mapStatus(app.status),
        date: app.appointmentDate || app.createdAt?.split('T')[0] || '',
        code: app.code,
        zaloLinked: app.zaloLinked || false,
      }));

      setMyAppointments(mapped);

      // Update waiting list for today
      const today = format(new Date(), 'yyyy-MM-dd');
      const todayAppts = mapped.filter(a => a.date === today && a.status === 'waiting');
      setWaitingList(todayAppts);

    } catch (error) {
      console.error('Failed to fetch appointments:', error);
    } finally {
      setIsLoading(false);
    }
  }, [citizenId, citizenName]);

  useEffect(() => {
    refreshAppointments();
  }, [refreshAppointments]);

  const bookAppointment = async (data: any): Promise<api.AppointmentResponse | null> => {
    try {
      setIsLoading(true);

      const request: api.CreateAppointmentRequest = {
        procedureId: data.procedureId,
        appointmentDate: format(data.date, 'yyyy-MM-dd'),
        appointmentTime: data.time,
        citizenName: citizenName,
        citizenId: citizenId,
        phoneNumber: data.phone || '0901234567',
        notes: data.notes,
        // Include Zalo account info for backend linking
        zaloId: zaloId,
        zaloName: zaloName,
      };

      const result = await api.createAppointment(request);

      // Add to local state immediately
      const newAppt: Appointment = {
        id: result.id.toString(),
        queueNumber: result.queueDisplay,
        citizenName: citizenName,
        procedure: result.procedureName,
        time: result.appointmentTime,
        status: 'waiting',
        date: result.appointmentDate,
        code: result.code,
      };

      setMyAppointments(prev => [newAppt, ...prev]);
      setWaitingList(prev => [...prev, newAppt]);

      return result;
    } catch (error) {
      console.error('Failed to book appointment:', error);
      return null;
    } finally {
      setIsLoading(false);
    }
  };

  const cancelAppointment = async (id: string, reason: string = 'Cancelled by user') => {
    try {
      setIsLoading(true);
      await api.cancelAppointment(parseInt(id), citizenId);

      setWaitingList(prev => prev.filter(a => a.id !== id));
      setMyAppointments(prev => prev.map(a =>
        a.id === id ? { ...a, status: 'cancelled' as const, cancelReason: reason } : a
      ));
    } catch (error) {
      console.error('Failed to cancel appointment:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  // Local-only functions for simulation
  const callNext = () => {
    if (waitingList.length === 0) return;
    if (currentServing) {
      setCompletedList(prev => [currentServing, ...prev]);
    }
    const nextPerson = { ...waitingList[0], status: 'serving' as const };
    setCurrentServing(nextPerson);
    setWaitingList(prev => prev.slice(1));
    setMyAppointments(prev => prev.map(appt =>
      appt.id === nextPerson.id ? { ...appt, status: 'serving' } : appt
    ));
  };

  const completeCurrent = () => {
    if (!currentServing) return;
    const completed = { ...currentServing, status: 'completed' as const };
    setCompletedList(prev => [completed, ...prev]);
    setCurrentServing(null);
    setMyAppointments(prev => prev.map(appt =>
      appt.id === completed.id ? { ...appt, status: 'completed' } : appt
    ));
  };

  const approveToProcessing = (id: string) => {
    const appt = waitingList.find(a => a.id === id);
    if (!appt) return;
    setWaitingList(prev => prev.filter(a => a.id !== id));
    const newFile: FileRecord = {
      id: `F-${Math.random().toString(36).substr(2, 5)}`,
      fileCode: `HS${new Date().getFullYear()}${Math.floor(Math.random() * 10000)}`,
      citizenName: appt.citizenName,
      procedure: appt.procedure,
      startTime: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      status: 'processing',
      staff: 'Admin Staff',
      deadline: format(addDays(new Date(), 3), 'yyyy-MM-dd')
    };
    setActiveFiles(prev => [newFile, ...prev]);
    setMyAppointments(prev => prev.map(a =>
      a.id === id ? { ...a, status: 'processing' } : a
    ));
  };

  const updateFile = (id: string, updates: Partial<FileRecord>) => {
    setActiveFiles(prev => prev.map(f => f.id === id ? { ...f, ...updates } : f));
  };

  const generateMockDataForDate = (date: Date): Appointment[] => {
    const dateStr = format(date, 'yyyy-MM-dd');
    const isPast = date < new Date() && !isSameDay(date, new Date());
    const count = Math.floor(Math.random() * 10) + 5;
    const records: Appointment[] = [];
    for (let i = 0; i < count; i++) {
      const statusOptions = isPast
        ? ['completed', 'completed', 'cancelled', 'processing']
        : ['upcoming'];
      const status = statusOptions[Math.floor(Math.random() * statusOptions.length)] as any;
      records.push({
        id: `${dateStr}-${i}`,
        queueNumber: `A${(i + 1).toString().padStart(3, '0')}`,
        citizenName: `Citizen ${dateStr.slice(5)} - ${i + 1}`,
        procedure: ['CCCD', 'Khai sinh', 'Đất đai', 'Đăng ký KD'][Math.floor(Math.random() * 4)],
        time: `${8 + Math.floor(i / 2)}:${i % 2 === 0 ? '00' : '30'}`,
        status: status,
        date: dateStr,
        cancelReason: status === 'cancelled' ? 'Khách không đến' : undefined
      });
    }
    return records;
  };

  const getQueueDataByDate = useCallback((date: Date) => {
    const today = new Date();
    if (isSameDay(date, today)) {
      const liveData = [
        ...completedList,
        ...(currentServing ? [currentServing] : []),
        ...waitingList
      ];
      return liveData.sort((a, b) => a.queueNumber.localeCompare(b.queueNumber));
    }
    const dateKey = format(date, 'yyyy-MM-dd');
    if (historyCache[dateKey]) {
      return historyCache[dateKey];
    }
    const newData = generateMockDataForDate(date);
    setHistoryCache(prev => ({ ...prev, [dateKey]: newData }));
    return newData;
  }, [waitingList, currentServing, completedList, historyCache]);

  const stats = {
    waiting: waitingList.length,
    completed: completedList.length + 28,
    avgWait: 18,
    processing: activeFiles.length
  };

  return (
    <SimulationContext.Provider value={{
      waitingList,
      activeFiles,
      currentServing,
      completedList,
      myAppointments,
      citizenId,
      citizenName,
      setCitizenId,
      setCitizenName,
      stats,
      bookAppointment,
      callNext,
      completeCurrent,
      cancelAppointment,
      approveToProcessing,
      updateFile,
      getQueueDataByDate,
      refreshAppointments,
      isLoading,
    }}>
      {children}
    </SimulationContext.Provider>
  );
};

export const useSimulation = () => {
  const context = useContext(SimulationContext);
  if (!context) throw new Error('useSimulation must be used within a SimulationProvider');
  return context;
};
