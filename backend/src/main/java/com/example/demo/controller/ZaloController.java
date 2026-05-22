package com.example.demo.controller;

import com.example.demo.dto.request.SyncZaloProfileRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.ZaloAccount;
import com.example.demo.service.ZaloAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Citizen-facing Zalo bridge APIs.
 *
 * - Sync Mini App profile from client runtime into local DB
 * - Optional server-side profile fetch from Zalo Graph API via access token
 */
@RestController
@RequestMapping("/api/citizen/zalo")
@RequiredArgsConstructor
@Slf4j
public class ZaloController {

    private final ZaloAccountService zaloAccountService;

    private final RestClient restClient = RestClient.builder().build();

    /**
     * Đồng bộ hồ sơ Zalo do Mini App SDK trả về xuống backend.
     * POST /api/citizen/zalo/profile/sync
     */
    @PostMapping("/profile/sync")
    public ResponseEntity<ApiResponse<Map<String, Object>>> syncZaloProfile(
            @Valid @RequestBody SyncZaloProfileRequest request) {

        ZaloAccount account = zaloAccountService.syncProfile(
                request.getZaloId(),
                request.getZaloName(),
                request.getAvatar(),
                request.getOaUserId(),
                request.getPhoneNumber());

        return ResponseEntity.ok(ApiResponse.success(toResponse(account), "Đồng bộ hồ sơ Zalo thành công"));
    }

    /**
     * Fetch profile từ access token rồi lưu lại.
     * GET /api/citizen/zalo/profile?accessToken=...
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getZaloProfile(@RequestParam String accessToken) {
        try {
            Map<String, Object> body = restClient.get()
                    .uri("https://graph.zalo.me/v2.0/me?fields=id,name,picture")
                    .headers(headers -> {
                        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
                        headers.set("access_token", accessToken);
                        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
                    })
                    .retrieve()
                    .body(Map.class);

            if (body == null || body.get("id") == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("ZALO_ERROR", "Không lấy được profile từ Zalo"));
            }

            ZaloAccount account = zaloAccountService.syncProfile(
                    String.valueOf(body.get("id")),
                    body.get("name") != null ? String.valueOf(body.get("name")) : null,
                    extractAvatar(body.get("picture")),
                    body.get("idByOA") != null ? String.valueOf(body.get("idByOA")) : null,
                    null);

            Map<String, Object> response = new HashMap<>(body);
            response.put("localProfile", toResponse(account));

            return ResponseEntity.ok(ApiResponse.success(response, "Profile fetched from Zalo and synced."));
        } catch (Exception e) {
            log.error("Error in Zalo bridge", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("ZALO_BRIDGE_ERROR", "System error while calling Zalo API: " + e.getMessage()));
        }
    }

    private Map<String, Object> toResponse(ZaloAccount account) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", account.getId());
        response.put("zaloId", account.getZaloId());
        response.put("zaloName", account.getZaloName());
        response.put("avatar", account.getZaloAvatar());
        response.put("oaUserId", account.getOaUserId());
        response.put("phoneNumber", account.getPhoneNumber());
        response.put("isActive", account.getIsActive());
        response.put("lastSyncedAt", account.getLastSyncedAt());
        return response;
    }

    @SuppressWarnings("unchecked")
    private String extractAvatar(Object picturePayload) {
        if (picturePayload instanceof String pictureUrl) {
            return pictureUrl;
        }

        if (picturePayload instanceof Map<?, ?> pictureMap) {
            Object data = pictureMap.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                Object url = dataMap.get("url");
                if (url != null) {
                    return String.valueOf(url);
                }
            }
        }

        return null;
    }
}
