# 🔒 Chiến Lược Backup Hệ Thống Hành Chính Công Q5

## 📋 Tổng Quan

Hệ thống bao gồm:
- **Backend**: Spring Boot API (Java)
- **Frontend Client**: React (Vite)
- **Frontend AdminStaff**: Vue.js (Vite)
- **Database**: PostgreSQL

---

## 🗄️ 1. Backup Database (PostgreSQL)

### 1.1 Backup Thủ Công

```powershell
# Full database backup
pg_dump -U postgres -h localhost -p 5432 -F c -b -v -f "backup_$(Get-Date -Format 'yyyyMMdd_HHmmss').backup" cholon_db

# Backup chỉ schema
pg_dump -U postgres -h localhost -s -f "schema_$(Get-Date -Format 'yyyyMMdd').sql" cholon_db

# Backup chỉ data
pg_dump -U postgres -h localhost -a -f "data_$(Get-Date -Format 'yyyyMMdd').sql" cholon_db
```

### 1.2 Script Backup Tự Động (Windows Task Scheduler)

Tạo file `backup_database.ps1`:

```powershell
# backup_database.ps1
$backupDir = "C:\Backups\HANHCHINHCONGQ5"
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$backupFile = "$backupDir\db_backup_$timestamp.backup"

# Tạo thư mục nếu chưa có
if (!(Test-Path $backupDir)) {
    New-Item -ItemType Directory -Path $backupDir -Force
}

# Thực hiện backup
$env:PGPASSWORD = "your_password"
pg_dump -U postgres -h localhost -p 5432 -F c -b -v -f $backupFile cholon_db

# Xóa backup cũ hơn 30 ngày
Get-ChildItem $backupDir -Filter "db_backup_*.backup" | 
    Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-30) } | 
    Remove-Item -Force

# Log kết quả
$logFile = "$backupDir\backup_log.txt"
"$(Get-Date): Backup completed - $backupFile" | Out-File -Append $logFile
```

### 1.3 Lịch Backup Database

| Loại Backup | Tần Suất | Thời Gian | Lưu Trữ |
|-------------|----------|-----------|---------|
| Full Backup | Hàng ngày | 02:00 AM | 30 ngày |
| Incremental | Mỗi 6 giờ | 08:00, 14:00, 20:00 | 7 ngày |
| Weekly Full | Chủ Nhật | 03:00 AM | 3 tháng |
| Monthly Full | Ngày 1 | 04:00 AM | 1 năm |

---

## 📁 2. Backup Source Code

### 2.1 Git Repository

```bash
# Đẩy code lên remote repository
git push origin main

# Tạo tag cho phiên bản
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin --tags
```

### 2.2 Backup Toàn Bộ Project

```powershell
# backup_project.ps1
$sourceDir = "C:\Users\Per\Desktop\HANHCHINHCONGQ5CHOLON"
$backupDir = "C:\Backups\HANHCHINHCONGQ5\project"
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"

# Loại trừ node_modules và target
$exclude = @("node_modules", "target", "dist", ".git")

# Nén và backup
Compress-Archive -Path $sourceDir -DestinationPath "$backupDir\project_$timestamp.zip" -CompressionLevel Optimal
```

---

## ⚙️ 3. Backup Configuration

### 3.1 Các File Cần Backup

```
backend/
├── src/main/resources/
│   ├── application.properties      # Cấu hình chính
│   ├── application-dev.properties  # Cấu hình dev
│   └── application-prod.properties # Cấu hình production

client/
├── .env                            # Environment variables
├── vite.config.ts                  # Vite config

AdminStaff/
├── .env
├── vite.config.ts
```

### 3.2 Script Backup Config

```powershell
# backup_config.ps1
$configFiles = @(
    "backend\src\main\resources\application.properties",
    "backend\src\main\resources\application-*.properties",
    "client\.env*",
    "AdminStaff\.env*"
)

$backupDir = "C:\Backups\HANHCHINHCONGQ5\config"
$timestamp = Get-Date -Format "yyyyMMdd"

foreach ($file in $configFiles) {
    Copy-Item $file "$backupDir\$timestamp\" -Force
}
```

---

## 🔄 4. Quy Trình Restore (Phục Hồi)

### 4.1 Restore Database

```powershell
# Restore từ file backup
pg_restore -U postgres -h localhost -d cholon_db -v "backup_file.backup"

# Hoặc từ SQL file
psql -U postgres -h localhost -d cholon_db -f "data_backup.sql"
```

### 4.2 Restore Full System

1. **Stop tất cả services**
   ```powershell
   # Stop backend
   Get-Process -Name java | Stop-Process -Force
   
   # Stop frontend dev servers
   Get-Process -Name node | Stop-Process -Force
   ```

2. **Restore database**
   ```powershell
   pg_restore -U postgres -d cholon_db -c backup_file.backup
   ```

3. **Restore source code**
   ```powershell
   Expand-Archive -Path project_backup.zip -DestinationPath C:\restore\
   ```

4. **Reinstall dependencies**
   ```powershell
   cd backend && .\mvnw.cmd clean install
   cd client && npm install
   cd AdminStaff && npm install
   ```

5. **Start services**
   ```powershell
   cd backend && .\mvnw.cmd spring-boot:run
   cd client && npm run dev
   cd AdminStaff && npm run dev
   ```

---

## 📊 5. Monitoring & Alerts

### 5.1 Kiểm Tra Backup

```powershell
# check_backup.ps1
$backupDir = "C:\Backups\HANHCHINHCONGQ5"
$today = Get-Date -Format "yyyyMMdd"

# Kiểm tra backup hôm nay có tồn tại không
$todayBackup = Get-ChildItem $backupDir -Filter "*$today*" -Recurse

if ($todayBackup.Count -eq 0) {
    # Gửi email cảnh báo
    Send-MailMessage -To "admin@example.com" -Subject "⚠️ Backup Failed" -Body "No backup found for $today"
}
```

### 5.2 Dashboard Kiểm Tra

| Metric | Script | Alert Threshold |
|--------|--------|-----------------|
| Backup Size | Check file size | < 1MB hoặc > 10GB |
| Backup Age | Check timestamp | > 24 hours |
| Disk Space | Check free space | < 10GB |
| Integrity | pg_restore --list | Error count > 0 |

---

## ☁️ 6. Backup Offsite (Cloud)

### 6.1 Sync lên Cloud Storage

```powershell
# Sử dụng rclone để sync lên Google Drive / OneDrive
rclone sync "C:\Backups\HANHCHINHCONGQ5" "gdrive:Backups/HANHCHINHCONGQ5"

# Hoặc AWS S3
aws s3 sync "C:\Backups\HANHCHINHCONGQ5" s3://your-bucket/backups/
```

### 6.2 Encryption (Mã hóa)

```powershell
# Mã hóa backup trước khi upload
7z a -p"YourSecretPassword" -mhe=on backup_encrypted.7z backup_file.backup
```

---

## 📝 7. Checklist Backup Hàng Tuần

- [ ] Kiểm tra backup database đã chạy đúng lịch
- [ ] Verify backup file size hợp lý
- [ ] Test restore trên môi trường staging
- [ ] Kiểm tra dung lượng ổ đĩa backup
- [ ] Sync backup lên cloud
- [ ] Review backup logs
- [ ] Cập nhật documentation nếu có thay đổi

---

## 🚨 8. Disaster Recovery Plan

### 8.1 RTO & RPO

| Metric | Target | Mô tả |
|--------|--------|-------|
| **RTO** (Recovery Time Objective) | 4 giờ | Thời gian tối đa để khôi phục hệ thống |
| **RPO** (Recovery Point Objective) | 6 giờ | Dữ liệu chấp nhận mất tối đa |

### 8.2 Các Bước Khôi Phục Khẩn Cấp

1. **Đánh giá thiệt hại** (15 phút)
2. **Notify stakeholders** (5 phút)
3. **Restore database từ backup gần nhất** (1 giờ)
4. **Deploy application** (30 phút)
5. **Verify và test** (1 giờ)
6. **Notify users hệ thống đã hoạt động** (5 phút)

---

## 📞 Liên Hệ Khẩn Cấp

| Role | Tên | Phone | Email |
|------|-----|-------|-------|
| Database Admin | [Tên] | [Phone] | [Email] |
| DevOps | [Tên] | [Phone] | [Email] |
| Project Manager | [Tên] | [Phone] | [Email] |

---

*Tài liệu được tạo: 2026-02-05*
*Cập nhật lần cuối: 2026-02-05*
