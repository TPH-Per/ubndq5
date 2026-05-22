package com.example.demo.service;

import com.example.demo.entity.Application;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZaloZnsService {

    private final ZaloAccountService zaloAccountService;

    private final RestClient restClient = RestClient.builder().build();

    @Value("${app.zalo.zns.enabled:false}")
    private boolean enabled;

    @Value("${app.zalo.zns.endpoint:https://business.openapi.zalo.me/message/template}")
    private String endpoint;

    @Value("${app.zalo.zns.access-token:}")
    private String accessToken;

    @Value("${app.zalo.zns.template-id.appointment-created:}")
    private String appointmentCreatedTemplateId;

    @Value("${app.zalo.zns.template-id.appointment-cancelled:}")
    private String appointmentCancelledTemplateId;

    public void sendAppointmentCreated(Application application, String appointmentDate, String appointmentTime) {
        sendTemplate(
                application,
                appointmentCreatedTemplateId,
                "appointment-created-" + application.getId(),
                buildTemplateData(application, appointmentDate, appointmentTime, "Đã đặt lịch"),
                "booked");
    }

    public void sendAppointmentCancelled(Application application, String appointmentDate, String appointmentTime) {
        sendTemplate(
                application,
                appointmentCancelledTemplateId,
                "appointment-cancelled-" + application.getId(),
                buildTemplateData(application, appointmentDate, appointmentTime, "Đã hủy lịch"),
                "cancelled");
    }

    private void sendTemplate(
            Application application,
            String templateId,
            String trackingId,
            Map<String, Object> templateData,
            String eventName) {

        if (!enabled) {
            return;
        }

        String phone = resolvePhoneNumber(application);
        if (!hasText(accessToken) || !hasText(templateId) || !hasText(phone)) {
            log.warn("Skip ZNS {} for application {} due to missing config or phone", eventName, application.getId());
            return;
        }

        Integer parsedTemplateId = parseTemplateId(templateId);
        if (parsedTemplateId == null) {
            log.warn("Skip ZNS {} for application {} because template id is invalid: {}", eventName, application.getId(), templateId);
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("phone", phone);
        payload.put("template_id", parsedTemplateId);
        payload.put("template_data", templateData);
        payload.put("tracking_id", trackingId);

        try {
            Map<?, ?> response = restClient.post()
                    .uri(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        headers.set("access_token", accessToken);
                        headers.setBearerAuth(accessToken);
                    })
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            log.info("ZNS {} sent for application {}: {}", eventName, application.getId(), response);
        } catch (Exception ex) {
            log.warn("Failed to send ZNS {} for application {}: {}", eventName, application.getId(), ex.getMessage());
        }
    }

    private Map<String, Object> buildTemplateData(
            Application application,
            String appointmentDate,
            String appointmentTime,
            String statusLabel) {
        Map<String, Object> templateData = new LinkedHashMap<>();
        templateData.put("customer_name", defaultString(application.getCitizenName(), "Công dân"));
        templateData.put("application_code", application.getApplicationCode());
        templateData.put("procedure_name", application.getProcedure() != null ? application.getProcedure().getProcedureName() : "");
        templateData.put("appointment_date", appointmentDate);
        templateData.put("appointment_time", appointmentTime);
        templateData.put("queue_display", defaultString(application.getQueueDisplay(), ""));
        templateData.put("status", statusLabel);
        return templateData;
    }

    private String resolvePhoneNumber(Application application) {
        String normalized = zaloAccountService.normalizePhoneNumber(application.getCitizenPhone());
        if (normalized != null) {
            return normalized;
        }

        if (application.getZaloAccount() == null) {
            return null;
        }

        return zaloAccountService.normalizePhoneNumber(application.getZaloAccount().getPhoneNumber());
    }

    private Integer parseTemplateId(String templateId) {
        try {
            return Integer.parseInt(templateId.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String defaultString(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }
}
