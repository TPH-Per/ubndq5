package com.example.demo.service;

import com.example.demo.entity.ZaloAccount;
import com.example.demo.repository.ZaloAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ZaloAccountService {

    private final ZaloAccountRepository zaloAccountRepository;

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
