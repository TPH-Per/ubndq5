package com.example.demo.repository;

import com.example.demo.entity.ProcedureType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcedureTypeRepository extends JpaRepository<ProcedureType, Integer> {

    Optional<ProcedureType> findByName(String name);

    boolean existsByName(String name);

    List<ProcedureType> findByIsActiveTrueOrderByName();

    @Query("SELECT COUNT(c) FROM Counter c WHERE c.procedureType.id = :procedureTypeId")
    long countCountersByProcedureTypeId(Integer procedureTypeId);

    @Query("SELECT COUNT(p) FROM Procedure p WHERE p.procedureType.id = :procedureTypeId")
    long countProceduresByProcedureTypeId(Integer procedureTypeId);
}
