package com.example.demo.repository;

import com.example.demo.entity.ZaloAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ZaloAccountRepository extends JpaRepository<ZaloAccount, Integer> {

    Optional<ZaloAccount> findByZaloId(String zaloId);

    boolean existsByZaloId(String zaloId);
}
