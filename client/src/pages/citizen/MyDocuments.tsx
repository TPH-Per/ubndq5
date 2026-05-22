import React, { useEffect, useMemo, useState } from 'react';
import {
  AlertCircle,
  ChevronRight,
  Clock,
  FileText,
  Loader2,
  MessageSquare,
  ShieldCheck,
  Smartphone,
  X,
} from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { cn } from '../../lib/utils';
import { useSimulation } from '../../context/SimulationContext';
import * as api from '../../services/citizenApi';

type SelectedApplicationState = {
  detail: api.ApplicationDetail;
  history: api.ApplicationHistory[];
};

const STATUS_STYLES: Record<string, string> = {
  PENDING: 'bg-blue-100 text-blue-700',
  IN_QUEUE: 'bg-yellow-100 text-yellow-700',
  PROCESSING: 'bg-green-100 text-green-700',
  SUPPLEMENT: 'bg-orange-100 text-orange-700',
  RECEIVED: 'bg-indigo-100 text-indigo-700',
  COMPLETED: 'bg-gray-100 text-gray-700',
  CANCELLED: 'bg-red-100 text-red-700',
};

const STATUS_LABELS: Record<string, string> = {
  PENDING: 'Đã tạo hồ sơ',
  IN_QUEUE: 'Đang chờ tiếp nhận',
  PROCESSING: 'Đang xử lý',
  SUPPLEMENT: 'Cần bổ sung',
  RECEIVED: 'Chờ nhận kết quả',
  COMPLETED: 'Hoàn thành',
  CANCELLED: 'Đã hủy',
};

const formatDateTime = (date?: string, time?: string) => {
  if (!date) {
    return 'Chưa có lịch hẹn';
  }

  return `${date}${time ? ` • ${time}` : ''}`;
};

const StatusBadge = ({ status }: { status: string }) => (
  <span
    className={cn(
      'inline-flex items-center rounded-full px-2.5 py-1 text-[10px] font-bold uppercase tracking-wide',
      STATUS_STYLES[status] || 'bg-gray-100 text-gray-700',
    )}
  >
    {STATUS_LABELS[status] || status}
  </span>
);

export const MyDocuments = () => {
  const { zaloId, accessToken } = useSimulation();
  const [applications, setApplications] = useState<api.ApplicationSummary[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [loadingDetailId, setLoadingDetailId] = useState<number | null>(null);
  const [selected, setSelected] = useState<SelectedApplicationState | null>(null);
  const [error, setError] = useState<string | null>(null);

  const refreshApplications = async () => {
    if (!zaloId) {
      setApplications([]);
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      const data = await api.getMyApplications(zaloId, undefined, accessToken);
      setApplications(data);
    } catch (fetchError) {
      console.error('Failed to fetch applications:', fetchError);
      setError('Không thể tải danh sách hồ sơ lúc này.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    void refreshApplications();
  }, [zaloId]);

  const groupedApplications = useMemo(() => {
    const active = applications.filter((application) =>
      ['PENDING', 'IN_QUEUE', 'PROCESSING', 'SUPPLEMENT', 'RECEIVED'].includes(application.status),
    );
    const completed = applications.filter((application) => application.status === 'COMPLETED');
    const cancelled = applications.filter((application) => application.status === 'CANCELLED');

    return [
      { key: 'active', label: 'Đang xử lý', items: active },
      { key: 'completed', label: 'Đã hoàn thành', items: completed },
      { key: 'cancelled', label: 'Đã hủy', items: cancelled },
    ];
  }, [applications]);

  const openApplication = async (applicationId: number) => {
    if (!zaloId) {
      return;
    }

    setLoadingDetailId(applicationId);
    try {
      const [detail, history] = await Promise.all([
        api.getApplicationDetail(applicationId, zaloId, accessToken),
        api.getApplicationHistory(applicationId, zaloId, accessToken),
      ]);

      setSelected({ detail, history });
    } catch (detailError) {
      console.error('Failed to load application detail:', detailError);
      setError('Không thể tải chi tiết hồ sơ.');
    } finally {
      setLoadingDetailId(null);
    }
  };

  if (!zaloId) {
    return (
      <div className="min-h-full bg-gray-50 p-4">
        <div className="rounded-2xl border border-yellow-200 bg-yellow-50 p-5 text-center">
          <Smartphone className="mx-auto mb-3 h-8 w-8 text-yellow-700" />
          <h2 className="text-lg font-bold text-yellow-900">Chưa liên kết Zalo</h2>
          <p className="mt-2 text-sm text-yellow-800">
            Vui lòng mở Mini App trong Zalo để tra cứu hồ sơ gắn với tài khoản của bạn.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-full bg-gray-50 p-4">
      <div className="mb-5 flex items-center justify-between">
        <div>
          <h2 className="text-lg font-bold text-gray-900">Hồ sơ của tôi</h2>
          <p className="text-xs text-gray-500">Danh sách hồ sơ được tạo từ tài khoản Zalo hiện tại</p>
        </div>
        <Button size="sm" variant="outline" onClick={() => void refreshApplications()}>
          Làm mới
        </Button>
      </div>

      {error && (
        <div className="mb-4 flex items-start gap-3 rounded-xl border border-red-100 bg-red-50 p-4 text-sm text-red-700">
          <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      ) : applications.length === 0 ? (
        <div className="rounded-2xl border border-gray-200 bg-white p-8 text-center shadow-sm">
          <FileText className="mx-auto mb-3 h-8 w-8 text-gray-300" />
          <h3 className="font-bold text-gray-900">Chưa có hồ sơ nào</h3>
          <p className="mt-2 text-sm text-gray-500">
            Hồ sơ sẽ xuất hiện ở đây sau khi bạn đặt lịch và tạo hồ sơ từ Mini App.
          </p>
        </div>
      ) : (
        <div className="space-y-6">
          {groupedApplications.map((group) => {
            if (group.items.length === 0) {
              return null;
            }

            return (
              <section key={group.key}>
                <h3 className="mb-3 ml-1 text-xs font-bold uppercase tracking-wider text-gray-400">
                  {group.label}
                </h3>
                <div className="space-y-3">
                  {group.items.map((application) => (
                    <button
                      key={application.id}
                      type="button"
                      onClick={() => void openApplication(application.id)}
                      className="w-full rounded-2xl border border-gray-100 bg-white p-4 text-left shadow-sm transition-all hover:border-primary/20 hover:shadow-md"
                    >
                      <div className="mb-3 flex items-start justify-between gap-3">
                        <div>
                          <p className="text-xs font-semibold uppercase tracking-wide text-gray-400">{application.code}</p>
                          <h4 className="mt-1 font-bold text-gray-900">{application.procedureName}</h4>
                        </div>
                        <StatusBadge status={application.status} />
                      </div>

                      <div className="space-y-2 text-sm text-gray-600">
                        <div className="flex items-center gap-2">
                          <Clock className="h-4 w-4 text-gray-400" />
                          <span>{formatDateTime(application.appointmentDate, application.appointmentTime)}</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <ShieldCheck className="h-4 w-4 text-gray-400" />
                          <span>{application.queueDisplay || 'Chưa cấp số hồ sơ'}</span>
                        </div>
                      </div>

                      <div className="mt-4 flex items-center justify-between">
                        {application.zaloLinked ? (
                          <span className="inline-flex items-center gap-1 rounded-full border border-blue-100 bg-blue-50 px-2.5 py-1 text-[10px] font-bold uppercase tracking-wide text-blue-700">
                            <Smartphone className="h-3 w-3" />
                            Gắn với Zalo
                          </span>
                        ) : (
                          <span className="text-[10px] font-semibold uppercase tracking-wide text-gray-400">
                            Chưa xác nhận liên kết
                          </span>
                        )}

                        {loadingDetailId === application.id ? (
                          <Loader2 className="h-4 w-4 animate-spin text-primary" />
                        ) : (
                          <ChevronRight className="h-4 w-4 text-gray-400" />
                        )}
                      </div>
                    </button>
                  ))}
                </div>
              </section>
            );
          })}
        </div>
      )}

      {selected && (
        <div className="fixed inset-0 z-[70] flex items-end justify-center bg-black/40 p-4 backdrop-blur-[2px]">
          <div className="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-3xl bg-white shadow-2xl">
            <div className="sticky top-0 flex items-center justify-between border-b border-gray-100 bg-white px-5 py-4">
              <div>
                <p className="text-xs font-semibold uppercase tracking-wide text-gray-400">{selected.detail.code}</p>
                <h3 className="font-bold text-gray-900">{selected.detail.procedureName}</h3>
              </div>
              <button
                type="button"
                onClick={() => setSelected(null)}
                className="rounded-full bg-gray-100 p-2 text-gray-500 transition-colors hover:bg-gray-200"
              >
                <X className="h-4 w-4" />
              </button>
            </div>

            <div className="space-y-5 p-5">
              <div className="flex items-center justify-between">
                <StatusBadge status={selected.detail.status} />
                {selected.detail.zaloLinked && (
                  <span className="inline-flex items-center gap-1 rounded-full border border-blue-100 bg-blue-50 px-2.5 py-1 text-[10px] font-bold uppercase tracking-wide text-blue-700">
                    <Smartphone className="h-3 w-3" />
                    Zalo
                  </span>
                )}
              </div>

              <div className="grid grid-cols-1 gap-3 rounded-2xl bg-gray-50 p-4 text-sm text-gray-700">
                <div>
                  <p className="text-xs font-semibold uppercase tracking-wide text-gray-400">Người nộp</p>
                  <p className="mt-1 font-medium text-gray-900">{selected.detail.citizenName || 'Chưa cập nhật'}</p>
                </div>
                <div>
                  <p className="text-xs font-semibold uppercase tracking-wide text-gray-400">CCCD</p>
                  <p className="mt-1 font-medium text-gray-900">{selected.detail.citizenCccd || 'Chưa cập nhật'}</p>
                </div>
                <div>
                  <p className="text-xs font-semibold uppercase tracking-wide text-gray-400">Số điện thoại</p>
                  <p className="mt-1 font-medium text-gray-900">{selected.detail.phone || 'Chưa cập nhật'}</p>
                </div>
                <div>
                  <p className="text-xs font-semibold uppercase tracking-wide text-gray-400">Lịch hẹn</p>
                  <p className="mt-1 font-medium text-gray-900">
                    {formatDateTime(selected.detail.appointmentDate, selected.detail.appointmentTime)}
                  </p>
                </div>
                <div>
                  <p className="text-xs font-semibold uppercase tracking-wide text-gray-400">Hạn xử lý</p>
                  <p className="mt-1 font-medium text-gray-900">{selected.detail.deadline || 'Chưa xác định'}</p>
                </div>
              </div>

              <div>
                <div className="mb-2 flex items-center gap-2">
                  <FileText className="h-4 w-4 text-primary" />
                  <h4 className="font-bold text-gray-900">Lịch sử xử lý</h4>
                </div>
                {selected.history.length === 0 ? (
                  <div className="rounded-2xl border border-gray-100 bg-gray-50 p-4 text-sm text-gray-500">
                    Chưa có lịch sử xử lý.
                  </div>
                ) : (
                  <div className="space-y-3">
                    {selected.history.map((history, index) => (
                      <div key={`${history.createdAt}-${index}`} className="rounded-2xl border border-gray-100 p-4">
                        <div className="flex items-start justify-between gap-3">
                          <div>
                            <p className="font-semibold text-gray-900">{history.action}</p>
                            <p className="mt-1 text-xs text-gray-500">{history.createdAt}</p>
                          </div>
                          <span className="rounded-full bg-gray-100 px-2 py-1 text-[10px] font-bold uppercase tracking-wide text-gray-600">
                            {history.statusTo}
                          </span>
                        </div>
                        {history.content && <p className="mt-3 text-sm text-gray-600">{history.content}</p>}
                        <div className="mt-3 flex items-center justify-between text-xs text-gray-500">
                          <span>{history.staffName || 'Hệ thống'}</span>
                          <span>{history.statusFrom ? `${history.statusFrom} -> ${history.statusTo}` : history.statusTo}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <Button
                fullWidth
                variant="outline"
                onClick={() => {
                  setSelected(null);
                }}
              >
                <MessageSquare className="mr-2 h-4 w-4" />
                Đóng chi tiết
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
