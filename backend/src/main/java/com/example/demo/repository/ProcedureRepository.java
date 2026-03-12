package com.example.demo.repository;

import com.example.demo.entity.Procedure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcedureRepository extends JpaRepository<Procedure, Integer> {

    Optional<Procedure> findByProcedureCode(String procedureCode);

    boolean existsByProcedureCode(String procedureCode);

    List<Procedure> findByIsActiveTrueOrderByDisplayOrder();

    @Query("SELECT p FROM Procedure p WHERE p.isActive = true ORDER BY p.displayOrder")
    List<Procedure> findAllActive();

    @Query("SELECT p FROM Procedure p LEFT JOIN FETCH p.procedureType WHERE p.procedureType.id = :procedureTypeId")
    List<Procedure> findByProcedureTypeId(Integer procedureTypeId);

    @Query("SELECT p FROM Procedure p WHERE p.procedureType.id = :procedureTypeId AND p.isActive = :isActive ORDER BY p.displayOrder")
    List<Procedure> findByProcedureTypeIdAndIsActive(Integer procedureTypeId, Boolean isActive);
}
