package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.Procedure;
import com.example.demo.repository.ProcedureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Public Procedure Controller
 * API công khai để lấy danh sách thủ tục hành chính (không cần auth)
 */
@RestController
@RequestMapping("/api/public/loaithutucs")
@RequiredArgsConstructor
public class PublicProcedureController {

    private final ProcedureRepository procedureRepository;

    /**
     * GET /api/public/loaithutucs
     * Lấy danh sách tất cả thủ tục đang hoạt động
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllActive() {
        List<Procedure> procedures = procedureRepository.findAllActive();

        List<Map<String, Object>> result = procedures.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("maThuTuc", p.getProcedureCode());
            map.put("tenThuTuc", p.getProcedureName());
            map.put("moTa", p.getDescription());
            map.put("thoiGianXuLy", p.getProcessingDays());
            return map;
        }).toList();

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
