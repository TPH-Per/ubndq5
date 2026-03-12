package com.example.demo.service;

import com.example.demo.dto.request.CreateHoSoRequest;
import com.example.demo.dto.request.UpdateHoSoStatusRequest;
import com.example.demo.dto.response.HoSoResponse;
import com.example.demo.entity.*;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Business logic for staff hồ sơ management.
 * Controller delegates all domain work here.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StaffHoSoService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationHistoryRepository applicationHistoryRepository;
    private final AppointmentRepository appointmentRepository;
    private final StaffRepository staffRepository;
    private final ProcedureRepository procedureRepository;
    private final ZaloAccountRepository zaloAccountRepository;

    // ======================== DASHBOARD ========================

    @Transactional(readOnly = true)
    public HoSoResponse.DashboardData getDashboard() {
        LocalDate today = LocalDate.now();

        List<Object[]> phaseCounts = applicationRepository.countGroupByPhase();
        Map<Integer, Long> countMap = new HashMap<>();
        for (Object[] row : phaseCounts) {
            countMap.put((Integer) row[0], (Long) row[1]);
        }

        long total     = countMap.values().stream().mapToLong(Long::longValue).sum();
        long choXuLy   = countMap.getOrDefault(Application.PHASE_QUEUE, 0L)
                       + countMap.getOrDefault(Application.PHASE_PENDING, 0L);
        long dangXuLy  = countMap.getOrDefault(Application.PHASE_PROCESSING, 0L)
                       + countMap.getOrDefault(Application.PHASE_RECEIVED, 0L);
        long hoanThanh = countMap.getOrDefault(Application.PHASE_COMPLETED, 0L);
        long treHan    = applicationRepository.countOverdue(today);

        return HoSoResponse.DashboardData.builder()
                .tongSoHoSo((int) total)
                .choXuLy((int) choXuLy)
                .dangXuLy((int) dangXuLy)
                .hoanThanh((int) hoanThanh)
                .treHan((int) treHan)
                .build();
    }

    // ======================== LIST ========================

    @Transactional(readOnly = true)
    public Page<HoSoResponse> getList(Integer trangThai, int page, int size) {
        if (size > 100) size = 100;
        if (page < 0)   page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Application> apps = (trangThai != null)
                ? applicationRepository.findByCurrentPhase(mapTrangThaiToPhase(trangThai), pageable)
                : applicationRepository.findAllPaged(pageable);

        return apps.map(this::mapToHoSoResponse);
    }

    // ======================== GET BY ID ========================

    @Transactional(readOnly = true)
    public HoSoResponse getById(Integer id) {
        return mapToHoSoResponse(applicationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND)));
    }

    // ======================== GET BY ZALO ========================

    @Transactional(readOnly = true)
    public List<HoSoResponse> getByZaloId(String zaloId) {
        ZaloAccount zaloAccount = zaloAccountRepository.findByZaloId(zaloId).orElse(null);
        if (zaloAccount == null) return List.of();

        return applicationRepository.findByZaloAccountId(zaloAccount.getId()).stream()
                .map(this::mapToHoSoResponse)
                .collect(Collectors.toList());
    }

    // ======================== CREATE ========================

    @Transactional
    public HoSoResponse create(CreateHoSoRequest req, String staffCode) {
        Staff staff = getStaff(staffCode);

        Procedure procedure = procedureRepository.findById(req.getLoaiThuTucId())
                .orElseThrow(() -> new AppException(ErrorCode.LOAITHUTUC_NOT_FOUND));

        String appCode = "HS" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Application app = Application.builder()
                .applicationCode(appCode)
                .procedure(procedure)
                .citizenCccd(req.getCccd())
                .citizenName(req.getHoTen())
                .citizenPhone(req.getSoDienThoai())
                .citizenEmail(req.getEmail())
                .currentPhase(Application.PHASE_PENDING)
                .priority(req.getDoUuTien() != null ? req.getDoUuTien() : 0)
                .deadline(LocalDate.now().plusDays(procedure.getProcessingDays()))
                .build();

        app = applicationRepository.save(app);

        String noiDungGhiChu = (req.getGhiChu() != null && !req.getGhiChu().trim().isEmpty())
                ? "Tạo hồ sơ mới. Ghi chú: " + req.getGhiChu()
                : "Tạo hồ sơ mới";

        saveHistory(app, staff, "TẠO HỒ SƠ", null, Application.PHASE_PENDING, noiDungGhiChu);

        log.info("Created hoso: {} for citizen {} by {}", app.getApplicationCode(), req.getCccd(), staff.getStaffCode());
        return mapToHoSoResponse(app);
    }

    // ======================== CREATE FROM LICH HEN ========================

    @Transactional
    public HoSoResponse createFromLichHen(Integer lichHenId, String staffCode) {
        Staff staff = getStaff(staffCode);
        Application app = applicationRepository.findById(lichHenId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        app.setCurrentPhase(Application.PHASE_PENDING);
        app.setDeadline(LocalDate.now().plusDays(app.getProcedure().getProcessingDays()));
        app = applicationRepository.save(app);

        saveHistory(app, staff, "TẠO HỒ SƠ TỪ LỊCH HẸN",
                Application.PHASE_COMPLETED, Application.PHASE_PENDING,
                "Tạo hồ sơ từ lịch hẹn " + app.getQueueDisplay());

        log.info("Created hoso from lichhen: {} by {}", app.getApplicationCode(), staff.getStaffCode());
        return mapToHoSoResponse(app);
    }

    // ======================== UPDATE STATUS ========================

    @Transactional
    public HoSoResponse updateStatus(Integer id, UpdateHoSoStatusRequest req, String staffCode) {
        Staff staff = getStaff(staffCode);
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        int oldPhase = app.getCurrentPhase();
        int newPhase = mapTrangThaiToPhase(req.getTrangThaiMoi());
        LocalDate appointmentDate = null;
        LocalTime expectedTime    = null;

        if (newPhase == Application.PHASE_SUPPLEMENT) {
            if (req.getNgayHen() == null || req.getGioHen() == null) {
                throw new AppException(ErrorCode.MISSING_REQUIRED_FIELD);
            }
            try {
                appointmentDate = LocalDate.parse(req.getNgayHen());
                expectedTime    = LocalTime.parse(req.getGioHen());
            } catch (DateTimeParseException e) {
                throw new AppException(ErrorCode.INVALID_FORMAT);
            }

            appointmentRepository.save(Appointment.builder()
                    .application(app)
                    .staff(staff)
                    .appointmentDate(appointmentDate)
                    .appointmentTime(expectedTime)
                    .status(Appointment.STATUS_SCHEDULED)
                    .build());
        }

        app.setCurrentPhase(newPhase);
        app = applicationRepository.save(app);

        if (newPhase == Application.PHASE_COMPLETED || newPhase == Application.PHASE_CANCELLED
                || newPhase == Application.PHASE_PROCESSING) {
            for (Appointment a : appointmentRepository.findActiveByApplicationId(app.getId())) {
                a.setStatus(newPhase == Application.PHASE_CANCELLED
                        ? Appointment.STATUS_CANCELLED : Appointment.STATUS_COMPLETED);
                appointmentRepository.save(a);
            }
        }

        saveHistoryExtended(app, staff, getActionByPhase(newPhase), oldPhase, newPhase,
                req.getNoiDung() != null ? req.getNoiDung() : HoSoResponse.getTrangThaiText(newPhase),
                appointmentDate, expectedTime);

        log.info("Updated hoso status: {} to {} by {}", app.getApplicationCode(), newPhase, staff.getStaffCode());
        return mapToHoSoResponse(app);
    }

    // ======================== UPDATE INFO ========================

    @Transactional
    public HoSoResponse update(Integer id, Map<String, Object> body, String staffCode) {
        Staff staff = getStaff(staffCode);
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        if (body.get("hoTen") != null)       app.setCitizenName((String) body.get("hoTen"));
        if (body.get("soDienThoai") != null)  app.setCitizenPhone((String) body.get("soDienThoai"));
        if (body.get("email") != null)         app.setCitizenEmail((String) body.get("email"));
        if (body.get("doUuTien") != null)      app.setPriority(((Number) body.get("doUuTien")).intValue());

        app = applicationRepository.save(app);
        saveHistory(app, staff, "CẬP NHẬT THÔNG TIN",
                app.getCurrentPhase(), app.getCurrentPhase(), "Cập nhật thông tin hồ sơ");

        log.info("Updated hoso: {} by {}", app.getApplicationCode(), staff.getStaffCode());
        return mapToHoSoResponse(app);
    }

    // ======================== HELPERS ========================

    private Staff getStaff(String staffCode) {
        return staffRepository.findByStaffCode(staffCode)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private int mapTrangThaiToPhase(Integer trangThai) {
        return switch (trangThai) {
            case 0 -> Application.PHASE_CANCELLED;
            case 1 -> Application.PHASE_QUEUE;
            case 2 -> Application.PHASE_PENDING;
            case 3 -> Application.PHASE_PROCESSING;
            case 4 -> Application.PHASE_COMPLETED;
            case 5 -> Application.PHASE_RECEIVED;
            case 6 -> Application.PHASE_SUPPLEMENT;
            default -> Application.PHASE_PENDING;
        };
    }

    private String getActionByPhase(int phase) {
        if (phase == Application.PHASE_SUPPLEMENT) return "YÊU CẦU BỔ SUNG";
        if (phase == Application.PHASE_COMPLETED)  return "HOÀN THÀNH";
        if (phase == Application.PHASE_PROCESSING) return "CHUYỂN GỌI SỐ";
        return "CẬP NHẬT TRẠNG THÁI";
    }

    private void saveHistory(Application app, Staff staff, String action,
            Integer oldPhase, int newPhase, String content) {
        saveHistoryExtended(app, staff, action, oldPhase, newPhase, content, null, null);
    }

    private void saveHistoryExtended(Application app, Staff staff, String action,
            Integer oldPhase, int newPhase, String content, LocalDate date, LocalTime time) {
        applicationHistoryRepository.save(ApplicationHistory.builder()
                .application(app)
                .counter(staff.getCounter())
                .staff(staff)
                .action(action)
                .phaseFrom(oldPhase)
                .phaseTo(newPhase)
                .content(content)
                .appointmentDate(date)
                .expectedTime(time)
                .createdAt(LocalDateTime.now())
                .build());
    }

    public HoSoResponse mapToHoSoResponse(Application app) {
        List<ApplicationHistory> histories = applicationHistoryRepository.findByApplicationId(app.getId());

        String tenQuay = histories.stream()
                .filter(h -> h.getCounter() != null)
                .map(h -> h.getCounter().getCounterName())
                .findFirst()
                .orElse(null);

        List<HoSoResponse.HistoryDto> historyDtos = histories.stream().map(h -> HoSoResponse.HistoryDto.builder()
                .nguoiXuLy(h.getStaff() != null ? h.getStaff().getFullName() : "Hệ thống")
                .hanhDong(h.getAction())
                .trangThaiCu(HoSoResponse.getTrangThaiText(h.getPhaseFrom()))
                .trangThaiMoi(HoSoResponse.getTrangThaiText(h.getPhaseTo()))
                .noiDung(h.getContent())
                .thoiGian(h.getCreatedAt())
                .build()).collect(Collectors.toList());

        return HoSoResponse.builder()
                .id(app.getId())
                .maHoSo(app.getApplicationCode())
                .cccd(app.getCitizenCccd())
                .hoTenCongDan(app.getCitizenName())
                .soDienThoai(app.getCitizenPhone())
                .email(app.getCitizenEmail())
                .tenThuTuc(app.getProcedure().getProcedureName())
                .maThuTuc(app.getProcedure().getProcedureCode())
                .tenQuay(tenQuay)
                .trangThai(app.getCurrentPhase())
                .trangThaiText(HoSoResponse.getTrangThaiText(app.getCurrentPhase()))
                .doUuTien(app.getPriority())
                .ngayNop(app.getCreatedAt())
                .hanXuLy(app.getDeadline())
                .nguonGoc(app.getZaloAccount() != null ? "Zalo" : "Trực tiếp")
                .maLichHen(app.getQueueDisplay())
                .loaiThuTucId(app.getProcedure().getId())
                .thoiGianXuLyQuyDinh(app.getProcedure().getProcessingDays())
                .lichSuXuLy(historyDtos)
                .zaloId(app.getZaloAccount() != null ? app.getZaloAccount().getZaloId() : null)
                .zaloName(app.getZaloAccount() != null ? app.getZaloAccount().getZaloName() : null)
                .build();
    }
}
