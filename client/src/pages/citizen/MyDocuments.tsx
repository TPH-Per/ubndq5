import React from 'react';
import { MOCK_DOCUMENTS } from '../../data/mock';
import { FileText, Download, Eye, MoreVertical, CheckCircle2, Clock, AlertCircle } from 'lucide-react';
import { cn } from '../../lib/utils';

export const MyDocuments = () => {
  const StatusIcon = ({ status }: { status: string }) => {
    switch(status) {
      case 'verified': return <CheckCircle2 className="h-4 w-4 text-green-500" />;
      case 'pending': return <Clock className="h-4 w-4 text-yellow-500" />;
      case 'rejected': return <AlertCircle className="h-4 w-4 text-red-500" />;
      default: return null;
    }
  };

  const categoryLabels: Record<string, string> = {
    'Personal': 'Cá nhân',
    'Business': 'Doanh nghiệp',
    'Forms': 'Biểu mẫu'
  };

  return (
    <div className="min-h-full bg-gray-50 p-4">
      <div className="flex justify-between items-center mb-6">
        <h2 className="font-bold text-lg text-gray-900">Ví giấy tờ số</h2>
        <button className="text-primary text-sm font-medium">+ Tải lên</button>
      </div>

      <div className="space-y-6">
        {['Personal', 'Business', 'Forms'].map((category) => {
          const docs = MOCK_DOCUMENTS.filter(d => d.category === category);
          if (docs.length === 0) return null;

          return (
            <div key={category}>
              <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-3 ml-1">
                {categoryLabels[category]}
              </h3>
              <div className="space-y-3">
                {docs.map((doc) => (
                  <div key={doc.id} className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex items-center gap-4">
                    <div className="h-10 w-10 bg-blue-50 rounded-lg flex items-center justify-center text-primary font-bold text-xs">
                      {doc.type}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2">
                        <h4 className="font-medium text-sm truncate text-gray-900">{doc.name}</h4>
                        <StatusIcon status={doc.status} />
                      </div>
                      <p className="text-xs text-gray-400 mt-0.5">{doc.size} • {doc.date}</p>
                    </div>
                    <button className="p-2 hover:bg-gray-50 rounded-full text-gray-400">
                      <MoreVertical className="h-5 w-5" />
                    </button>
                  </div>
                ))}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
