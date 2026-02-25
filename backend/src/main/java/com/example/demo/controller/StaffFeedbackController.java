package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.FeedbackResponse;
import com.example.demo.entity.Report;
import com.example.demo.entity.Reply;
import com.example.demo.entity.Staff;
import com.example.demo.repository.ReportRepository;
import com.example.demo.repository.ReplyRepository;
import com.example.demo.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/staff/feedbacks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('Staff') or hasRole('Admin')")
@Transactional
public class StaffFeedbackController {

        private final ReportRepository reportRepository;
        private final ReplyRepository replyRepository;
        private final StaffRepository staffRepository;

        @GetMapping
        public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getList(
                        @RequestParam(required = false) Integer status) {

                List<Report> reports;
                if (status != null) {
                        reports = reportRepository.findByStatus(status);
                } else {
                        reports = reportRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
                }

                List<FeedbackResponse> response = reports.stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<FeedbackResponse>> getDetail(@PathVariable Integer id) {
                Report report = reportRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy phản ánh"));
                return ResponseEntity.ok(ApiResponse.success(mapToResponse(report)));
        }

        @PostMapping("/{id}/reply")
        public ResponseEntity<ApiResponse<FeedbackResponse>> reply(
                        @PathVariable Integer id,
                        @RequestBody Map<String, String> body,
                        Authentication authentication) {

                String content = body.get("content");
                if (content == null || content.isBlank()) {
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.error("EMPTY_CONTENT",
                                                        "Nội dung trả lời không được để trống"));
                }

                Staff staff = staffRepository.findByStaffCode(authentication.getName())
                                .orElseThrow(() -> new RuntimeException("Staff not found"));

                Report report = reportRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Report not found"));

                Reply reply = Reply.builder()
                                .report(report)
                                .staff(staff)
                                .content(content)
                                .build();
                replyRepository.save(reply);

                // Update report status to RESOLVED if it was NEW or PROCESSING
                if (report.getStatus() != Report.STATUS_RESOLVED) {
                        report.setStatus(Report.STATUS_RESOLVED);
                        reportRepository.save(report);
                }

                return ResponseEntity.ok(ApiResponse.success(mapToResponse(report), "Đã trả lời phản ánh"));
        }

        private FeedbackResponse mapToResponse(Report r) {
                List<Reply> replies = replyRepository.findByReportId(r.getId());

                List<FeedbackResponse.ReplyDto> replyDtos = replies.stream()
                                .map(re -> FeedbackResponse.ReplyDto.builder()
                                                .id(re.getId())
                                                .content(re.getContent())
                                                .staffName(re.getStaff().getFullName())
                                                .createdAt(re.getCreatedAt())
                                                .build())
                                .collect(Collectors.toList());

                return FeedbackResponse.builder()
                                .id(r.getId())
                                .type(r.getReportType())
                                .title(r.getTitle())
                                .content(r.getContent())
                                .citizenName(r.getCitizen().getFullName())
                                .citizenId(r.getCitizen().getCitizenId())
                                .applicationCode(r.getApplication() != null ? r.getApplication().getApplicationCode()
                                                : null)
                                .status(r.getStatus())
                                .createdAt(r.getCreatedAt())
                                .replies(replyDtos)
                                .build();
        }
}
