package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.FeedbackResponse;
import com.example.demo.entity.Feedback;
import com.example.demo.entity.Reply;
import com.example.demo.entity.Staff;
import com.example.demo.repository.FeedbackRepository;
import com.example.demo.repository.ReplyRepository;
import com.example.demo.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
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

        private final FeedbackRepository feedbackRepository;
        private final ReplyRepository replyRepository;
        private final StaffRepository staffRepository;

        @GetMapping
        public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getList(
                        @RequestParam(required = false) Integer status) {

                List<Feedback> feedbacks;
                if (status != null) {
                        feedbacks = feedbackRepository.findByStatus(status);
                } else {
                        feedbacks = feedbackRepository.findAllOrderByCreatedAtDesc();
                }

                List<FeedbackResponse> response = feedbacks.stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<FeedbackResponse>> getDetail(@PathVariable Integer id) {
                Feedback feedback = feedbackRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy phản ánh"));
                return ResponseEntity.ok(ApiResponse.success(mapToResponse(feedback)));
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

                Feedback feedback = feedbackRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Feedback not found"));

                Reply reply = Reply.builder()
                                .feedback(feedback)
                                .staff(staff)
                                .content(content)
                                .build();
                replyRepository.save(reply);

                if (feedback.getStatus() < Feedback.STATUS_RESOLVED) {
                        feedback.setStatus(Feedback.STATUS_RESOLVED);
                        feedback.setProcessedAt(LocalDateTime.now());
                        feedbackRepository.save(feedback);
                }

                return ResponseEntity.ok(ApiResponse.success(mapToResponse(feedback), "Đã trả lời phản ánh"));
        }

        private FeedbackResponse mapToResponse(Feedback f) {
                List<Reply> replies = replyRepository.findByFeedbackId(f.getId());

                List<FeedbackResponse.ReplyDto> replyDtos = replies.stream()
                                .map(re -> FeedbackResponse.ReplyDto.builder()
                                                .id(re.getId())
                                                .content(re.getContent())
                                                .staffName(re.getStaff().getFullName())
                                                .createdAt(re.getCreatedAt())
                                                .build())
                                .collect(Collectors.toList());

                return FeedbackResponse.builder()
                                .id(f.getId())
                                .type(f.getType())
                                .title(f.getTitle())
                                .content(f.getContent())
                                .citizenName(f.getCitizenName())
                                .citizenId(f.getCitizenCccd())
                                .applicationCode(f.getApplication() != null ? f.getApplication().getApplicationCode() : null)
                                .status(f.getStatus())
                                .createdAt(f.getCreatedAt())
                                .replies(replyDtos)
                                .build();
        }
}
