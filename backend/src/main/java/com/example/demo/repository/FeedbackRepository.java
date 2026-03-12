package com.example.demo.repository;

import com.example.demo.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {

    // Tìm góp ý theo CCCD công dân (inline field, không FK)
    List<Feedback> findByCitizenCccdOrderByCreatedAtDesc(String citizenCccd);

    // Alias cho CitizenController
    default List<Feedback> findByCitizenCccd(String cccd) {
        return findByCitizenCccdOrderByCreatedAtDesc(cccd);
    }

    List<Feedback> findByStatus(Integer status);

    @Query("SELECT f FROM Feedback f ORDER BY f.createdAt DESC")
    List<Feedback> findAllOrderByCreatedAtDesc();

    /**
     * Feedback search with ownership verification:
     * returns feedbacks where the zaloId matches (submitted by this user)
     * OR the feedback has no zaloId (anonymous) and the CCCD matches.
     */
    @Query("SELECT f FROM Feedback f WHERE f.zaloId = :zaloId " +
           "OR (f.zaloId IS NULL AND f.citizenCccd = :cccd) " +
           "ORDER BY f.createdAt DESC")
    List<Feedback> findByZaloIdOrAnonymousCccd(
            @org.springframework.data.repository.query.Param("zaloId") String zaloId,
            @org.springframework.data.repository.query.Param("cccd") String cccd);
}
