package com.example.demo.repository;

import com.example.demo.entity.Counter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CounterRepository extends JpaRepository<Counter, Integer> {

    Optional<Counter> findByCounterCode(String counterCode);

    boolean existsByCounterCode(String counterCode);

    List<Counter> findByIsActiveTrueOrderByCounterCode();

    @Query("SELECT c FROM Counter c WHERE c.isActive = true ORDER BY c.counterCode")
    List<Counter> findAllActive();

    @Query("SELECT c FROM Counter c WHERE c.procedureType.id = :procedureTypeId")
    List<Counter> findByProcedureTypeId(Integer procedureTypeId);
}
