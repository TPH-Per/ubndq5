import React, { createContext, useContext, useState, useCallback } from 'react';
import { MOCK_APPOINTMENTS, WAITING_LIST_MOCK } from '../data/mock';
import { format, isSameDay, addDays, subDays, parseISO } from 'date-fns';

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
}

export interface FileRecord {
  id: string;
  fileCode: string;
  citizenName: string;
  procedure: string;
  startTime: string;
  status: 'processing' | 'completed' | 'need_docs' | 'rejected';
  staff: string;
  deadline: string; // YYYY-MM-DD
}

interface SimulationContextType {
  waitingList: Appointment[];
  activeFiles: FileRecord[];
  currentServing: Appointment | null;
  completedList: Appointment[];
  myAppointments: Appointment[];
  stats: {
    waiting: number;
    completed: number;
    avgWait: number;
    processing: number;
  };
  bookAppointment: (data: any) => void;
  callNext: () => void;
  completeCurrent: () => void;
  cancelAppointment: (id: string, reason?: string) => void;
  approveToProcessing: (id: string) => void;
  updateFile: (id: string, updates: Partial<FileRecord>) => void;
  getQueueDataByDate: (date: Date) => Appointment[];
}

const SimulationContext = createContext<SimulationContextType | undefined>(undefined);

// Initial Mock Files
const MOCK_FILES: FileRecord[] = [
  { 
    id: 'F001', 
    fileCode: 'HS202512001', 
    citizenName: 'Phạm Văn X', 
    procedure: 'Đăng ký kinh doanh', 
    startTime: '09:15', 
    status: 'processing', 
    staff: 'Admin Staff',
    deadline: format(addDays(new Date(), 2), 'yyyy-MM-dd')
  },
  { 
    id: 'F002', 
    fileCode: 'HS202512002', 
    citizenName: 'Lê Thị Y', 
    procedure: 'Cấp lại CCCD', 
    startTime: '09:45', 
    status: 'need_docs', 
    staff: 'Admin Staff',
    deadline: format(addDays(new Date(), 5), 'yyyy-MM-dd')
  },
];

export const SimulationProvider = ({ children }: { children: React.ReactNode }) => {
  // Initialize with RICH hardcoded data
  const [waitingList, setWaitingList] = useState<Appointment[]>(WAITING_LIST_MOCK as Appointment[]);
  const [activeFiles, setActiveFiles] = useState<FileRecord[]>(MOCK_FILES);

  const [currentServing, setCurrentServing] = useState<Appointment | null>({
    id: '0', 
    queueNumber: 'A045', 
    citizenName: 'Đặng Văn F', 
    procedure: 'CCCD', 
    time: '10:30', 
    status: 'serving', 
    date: format(new Date(), 'yyyy-MM-dd'),
    counter: 'Counter A'
  });

  const [completedList, setCompletedList] = useState<Appointment[]>([]);
  
  // Initialize Citizen's appointments with MOCK_APPOINTMENTS
  const [myAppointments, setMyAppointments] = useState<Appointment[]>(MOCK_APPOINTMENTS as Appointment[]);

  // Cache for generated history/future data to keep it consistent during session
  const [historyCache, setHistoryCache] = useState<Record<string, Appointment[]>>({});

  const bookAppointment = (data: any) => {
    const newId = Math.random().toString(36).substr(2, 9);
    const lastNum = waitingList.length > 0 
      ? parseInt(waitingList[waitingList.length - 1].queueNumber.replace('A', '')) 
      : parseInt(currentServing?.queueNumber.replace('A', '') || '0');
    
    const newQueueNum = `A${(lastNum + 1).toString().padStart(3, '0')}`;

    const newAppt: Appointment = {
      id: newId,
      queueNumber: newQueueNum,
      citizenName: data.fullName || 'Nguyễn Văn A',
      procedure: data.procedureName || 'Thủ tục hành chính',
      time: '12:00',
      status: 'waiting',
      date: data.date ? format(data.date, 'yyyy-MM-dd') : format(new Date(), 'yyyy-MM-dd'),
      counter: 'Counter A - Tầng 1'
    };

    setWaitingList(prev => [...prev, newAppt]);
    setMyAppointments(prev => [newAppt, ...prev]);
  };

  const callNext = () => {
    if (waitingList.length === 0) return;

    if (currentServing) {
      setCompletedList(prev => [currentServing, ...prev]);
    }

    const nextPerson = { ...waitingList[0], status: 'serving' as const };
    const remainingList = waitingList.slice(1);

    setCurrentServing(nextPerson);
    setWaitingList(remainingList);

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

  const cancelAppointment = (id: string, reason: string = 'Cancelled by staff') => {
    setWaitingList(prev => prev.filter(a => a.id !== id));
    setMyAppointments(prev => prev.map(a => a.id === id ? { ...a, status: 'cancelled', cancelReason: reason } : a));
  };

  const approveToProcessing = (id: string) => {
    const appt = waitingList.find(a => a.id === id);
    if (!appt) return;

    // Remove from waiting list
    setWaitingList(prev => prev.filter(a => a.id !== id));

    // Add to Active Files
    const newFile: FileRecord = {
      id: `F-${Math.random().toString(36).substr(2, 5)}`,
      fileCode: `HS${new Date().getFullYear()}${Math.floor(Math.random() * 10000)}`,
      citizenName: appt.citizenName,
      procedure: appt.procedure,
      startTime: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      status: 'processing',
      staff: 'Admin Staff',
      deadline: format(addDays(new Date(), 3), 'yyyy-MM-dd') // Default 3 days deadline
    };
    setActiveFiles(prev => [newFile, ...prev]);

    // Update Citizen View
    setMyAppointments(prev => prev.map(a => 
      a.id === id ? { ...a, status: 'processing' } : a
    ));
  };

  const updateFile = (id: string, updates: Partial<FileRecord>) => {
    setActiveFiles(prev => prev.map(f => f.id === id ? { ...f, ...updates } : f));
  };

  // Helper to generate consistent mock data for other days
  const generateMockDataForDate = (date: Date): Appointment[] => {
    const dateStr = format(date, 'yyyy-MM-dd');
    const isPast = date < new Date() && !isSameDay(date, new Date());
    
    const count = Math.floor(Math.random() * 10) + 5; // 5-15 records
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
        time: `${8 + Math.floor(i/2)}:${i % 2 === 0 ? '00' : '30'}`,
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
      // For Today: Return Live Data
      const liveData = [
        ...completedList,
        ...(currentServing ? [currentServing] : []),
        ...waitingList
      ];
      // Sort by queue number roughly
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
      stats,
      bookAppointment,
      callNext,
      completeCurrent,
      cancelAppointment,
      approveToProcessing,
      updateFile,
      getQueueDataByDate
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
