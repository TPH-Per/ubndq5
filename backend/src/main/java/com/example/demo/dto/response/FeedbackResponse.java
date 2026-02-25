package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class FeedbackResponse {
    private Integer id;
    private Integer type; // 0: Feedback, 1: Complaint, 2: Praise
    private String title;
    private String content;
    private String citizenName;
    private String citizenId;
    private String applicationCode;
    private Integer status; // 0: NEW, 1: PROCESSING, 2: RESOLVED
    private LocalDateTime createdAt;
    private List<ReplyDto> replies;

    @Data
    @Builder
    public static class ReplyDto {
        private Integer id;
        private String content;
        private String staffName;
        private LocalDateTime createdAt;
    }
}
