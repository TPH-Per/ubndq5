package com.example.demo.service;

import com.example.demo.entity.ZaloAccount;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.ZaloAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZaloAccountService {

    private final ZaloAccountRepository zaloAccountRepository;

    private final RestClient restClient = RestClient.builder().build();

    @Transactional
    public ZaloAccount syncProfile(String zaloId, String zaloName, String avatar, String oaUserId, String phoneNumber) {
        ZaloAccount account = zaloAccountRepository.findByZaloId(zaloId)
                .orElseGet(() -> ZaloAccount.builder()
                        .zaloId(zaloId)
                        .isActive(true)
                        .build());

        if (hasText(zaloName)) {
            account.setZaloName(zaloName.trim());
        }

        if (hasText(avatar)) {
            account.setZaloAvatar(avatar.trim());
        }

        if (hasText(oaUserId)) {
            account.setOaUserId(oaUserId.trim());
        }

        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        if (normalizedPhone != null) {
            account.setPhoneNumber(normalizedPhone);
        }

        account.setIsActive(true);
        account.setLastSyncedAt(LocalDateTime.now());

        return zaloAccountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public ZaloAccount findByZaloId(String zaloId) {
        return zaloAccountRepository.findByZaloId(zaloId).orElse(null);
    }

    /**
     * Verify Zalo access token bằng cách gọi Graph API.
     * Trả về zaloId đã được verify — KHÔNG dùng zaloId client gửi lên.
     *
     * @param accessToken Zalo Mini App access token
     * @return verified zaloId from Zalo's response
     * @throws AppException nếu token invalid hoặc Zalo API error
     */
    @SuppressWarnings("unchecked")
    public String verifyAccessToken(String accessToken) {
        try {
            Map<String, Object> body = restClient.get()
                    .uri("https://graph.zalo.me/v2.0/me?fields=id")
                    .headers(headers -> {
                        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
                        headers.set("access_token", accessToken);
                        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
                    })
                    .retrieve()
                    .body(Map.class);

            if (body == null || body.get("id") == null) {
                log.warn("Zalo verify failed: no id in response");
                throw new AppException(ErrorCode.ZALO_TOKEN_INVALID);
            }

            String verifiedZaloId = String.valueOf(body.get("id"));
            log.info("Zalo token verified: zaloId={}", verifiedZaloId);
            return verifiedZaloId;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Zalo token verification error: {}", e.getMessage());
            throw new AppException(ErrorCode.ZALO_TOKEN_INVALID);
        }
    }

    public String normalizePhoneNumber(String phoneNumber) {
        if (!hasText(phoneNumber)) {
            return null;
        }

        String digitsOnly = phoneNumber.replaceAll("\\D", "");
        if (digitsOnly.isBlank()) {
            return null;
        }

        if (digitsOnly.startsWith("84")) {
            return digitsOnly;
        }

        if (digitsOnly.startsWith("0") && digitsOnly.length() >= 9) {
            return "84" + digitsOnly.substring(1);
        }

        return digitsOnly;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
