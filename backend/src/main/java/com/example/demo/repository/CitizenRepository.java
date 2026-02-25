package com.example.demo.repository;

import com.example.demo.entity.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitizenRepository extends JpaRepository<Citizen, String> {
    // Primary Key is now citizenId (CCCD) - String type

    // findById(citizenId) is built-in

    Optional<Citizen> findByCitizenId(String citizenId);

    Optional<Citizen> findByPhone(String phone);

    @Query("SELECT c FROM Citizen c WHERE c.fullName LIKE %:name%")
    List<Citizen> findByFullNameContaining(String name);
}
