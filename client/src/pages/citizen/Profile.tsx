import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { PROFILE_DATA } from '../../data/mock';
import { Phone, MessageSquare, ChevronRight, X, MessageCircle } from 'lucide-react';
import { cn } from '../../lib/utils';
import { AnimatePresence, motion } from 'framer-motion';

export const Profile = () => {
  const navigate = useNavigate();
  const [showContact, setShowContact] = useState(false);

  const menuItems = [
    { 
      icon: Phone, 
      label: 'Liên hệ', 
      color: 'text-blue-600', 
      bg: 'bg-blue-50',
      action: () => setShowContact(true)
    },
    { 
      icon: MessageSquare, 
      label: 'Góp ý', 
      color: 'text-green-600', 
      bg: 'bg-green-50',
      action: () => navigate('/citizen/feedback')
    }
  ];

  return (
    <div className="min-h-full bg-gray-50 relative">
      {/* Header Profile Card */}
      <div className="bg-white p-8 pb-10 rounded-b-[2rem] shadow-sm border-b border-gray-100">
        <div className="flex flex-col items-center">
          <div className="relative mb-4 group">
            <div className="absolute -inset-0.5 bg-gradient-to-r from-blue-600 to-blue-400 rounded-full opacity-75 group-hover:opacity-100 transition duration-200 blur"></div>
            <img 
              src={PROFILE_DATA.avatar} 
              alt="Profile" 
              className="relative h-28 w-28 rounded-full object-cover border-4 border-white shadow-xl"
            />
          </div>
          <h2 className="text-2xl font-bold text-gray-900 mb-1">{PROFILE_DATA.name}</h2>
          
          <div className="flex items-center gap-2 mt-2 bg-blue-50 px-4 py-1.5 rounded-full border border-blue-100">
            <Phone className="h-3.5 w-3.5 text-primary" />
            <span className="text-sm font-bold text-primary">{PROFILE_DATA.phone}</span>
          </div>
        </div>
      </div>

      {/* Menu Section */}
      <div className="p-4 -mt-4 relative z-10">
        <div className="bg-white rounded-2xl shadow-lg shadow-gray-200/50 border border-gray-100 overflow-hidden">
          {menuItems.map((item, i) => (
            <button 
              key={i} 
              onClick={item.action}
              className="w-full flex items-center gap-4 p-5 hover:bg-gray-50 transition-colors border-b border-gray-100 last:border-none group"
            >
              <div className={cn("h-10 w-10 rounded-xl flex items-center justify-center transition-colors", item.bg)}>
                <item.icon className={cn("h-5 w-5", item.color)} />
              </div>
              <span className="flex-1 text-left text-base font-medium text-gray-700 group-hover:text-primary transition-colors">
                {item.label}
              </span>
              <ChevronRight className="h-5 w-5 text-gray-300 group-hover:text-primary group-hover:translate-x-1 transition-transform" />
            </button>
          ))}
        </div>
      </div>
      
      <div className="text-center mt-8">
        <p className="text-xs text-gray-400">Hành Chính Phục Vụ Phường Chợ Lớn HCMC</p>
        <p className="text-[10px] text-gray-300 mt-1">Version 1.0.2</p>
      </div>

      {/* Contact Bottom Sheet Modal */}
      <AnimatePresence>
        {showContact && (
          <>
            {/* Backdrop */}
            <motion.div 
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setShowContact(false)}
              className="fixed inset-0 bg-black/50 backdrop-blur-sm z-[60]"
            />
            
            {/* Bottom Sheet */}
            <motion.div 
              initial={{ y: '100%' }}
              animate={{ y: 0 }}
              exit={{ y: '100%' }}
              transition={{ type: "spring", damping: 25, stiffness: 300 }}
              className="fixed bottom-0 left-0 right-0 z-[70] bg-white rounded-t-3xl p-6"
            >
              <div className="flex justify-between items-center mb-6">
                <h3 className="font-bold text-xl text-gray-900">Liên hệ hỗ trợ</h3>
                <button 
                  onClick={() => setShowContact(false)}
                  className="p-2 bg-gray-100 rounded-full hover:bg-gray-200 transition-colors"
                >
                  <X className="h-5 w-5 text-gray-500" />
                </button>
              </div>

              <div className="space-y-4 mb-safe">
                {/* Hotline Option */}
                <a 
                  href="tel:19001022"
                  className="flex items-center gap-4 p-4 bg-red-50 rounded-2xl border border-red-100 active:scale-[0.98] transition-all hover:shadow-md group"
                >
                  <div className="h-14 w-14 bg-red-500 rounded-full flex items-center justify-center text-white shadow-lg shadow-red-200 group-hover:scale-110 transition-transform">
                    <Phone className="h-7 w-7" />
                  </div>
                  <div>
                    <p className="font-bold text-gray-900 text-lg">Tổng đài Hotline</p>
                    <p className="text-sm text-red-600 font-medium">1900 1022</p>
                  </div>
                </a>

                {/* Zalo OA Option */}
                <button 
                  className="w-full flex items-center gap-4 p-4 bg-blue-50 rounded-2xl border border-blue-100 active:scale-[0.98] transition-all hover:shadow-md group text-left"
                >
                  <div className="h-14 w-14 bg-blue-600 rounded-full flex items-center justify-center text-white shadow-lg shadow-blue-200 group-hover:scale-110 transition-transform">
                    <MessageCircle className="h-7 w-7" />
                  </div>
                  <div>
                    <p className="font-bold text-gray-900 text-lg">Zalo OA Official</p>
                    <p className="text-sm text-blue-600 font-medium">Chat ngay với hỗ trợ viên</p>
                  </div>
                </button>
              </div>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </div>
  );
};
