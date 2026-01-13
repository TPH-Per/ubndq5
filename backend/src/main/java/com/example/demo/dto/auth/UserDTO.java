package com.example.demo.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    
    
    private Integer id;
    
    
    private String maNhanVien;
    
   
    private String hoTen;
    
    
    private String email;
    
    
    private String soDienThoai;
    
    
    private String roleName;
    
    
    private String roleDisplayName;
    
    
    private String tenQuay;
    
    
    private Integer quayId;
    
    
    private Boolean trangThai;
    
    
    private LocalDateTime lanDangNhapCuoi;
}
