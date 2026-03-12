package com.example.demo.repository;

/**
 * @deprecated Không còn dùng sau khi bỏ bảng Citizen.
 * Dùng {@link ApplicationRepository#findByCitizenCccd(String)} để tra cứu theo CCCD.
 */
@Deprecated
public interface CitizenRepository {
    // Empty - replaced by inline CCCD queries in ApplicationRepository
}
