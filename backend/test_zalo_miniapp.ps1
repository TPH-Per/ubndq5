$BASE = "http://localhost:8081/api/citizen"
$ZALO_ID = "zalo_user_test_12345"
$ZALO_NAME = "Nguyen Van Test (Zalo)"
$CCCD = "079088001234"
$HO_TEN = "Nguyen Van Test"
$SDT = "0901234567"
$EMAIL = "nvtest@gmail.com"
$tomorrow = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")
$script:pass = 0
$script:fail = 0
$script:appId = $null
$script:appCode = $null
$script:queueDisplay = $null
$script:procedureId = 1
$script:slotTime = "07:30"

function ok($label, $cond, $detail = "") {
    if ($cond) {
        Write-Host "  [PASS] $label" -ForegroundColor Green
        $script:pass++
    }
    else {
        Write-Host "  [FAIL] $label" -ForegroundColor Red
        $script:fail++
    }
    if ($detail) { Write-Host "         $detail" -ForegroundColor Gray }
}

function api($method, $url, $body = $null) {
    try {
        $p = @{ Uri = $url; Method = $method; ContentType = "application/json" }
        if ($body) { $p.Body = ($body | ConvertTo-Json -Depth 5) }
        $r = Invoke-RestMethod @p
        return @{ ok = $true; data = $r }
    }
    catch {
        return @{ ok = $false; err = $_.Exception.Message; body = $_.ErrorDetails.Message }
    }
}

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  ZALO MINI APP API TEST" -ForegroundColor Cyan
Write-Host "  Backend : $BASE" -ForegroundColor Cyan
Write-Host "  ZaloID  : $ZALO_ID" -ForegroundColor Cyan
Write-Host "  CCCD    : $CCCD" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

# --- [1] GET /specialties ---
Write-Host "`n--- [1] GET /specialties ---" -ForegroundColor Yellow
$r = api "GET" "$BASE/specialties"
ok "Lay danh sach linh vuc" $r.ok "$($r.data.data.Count) linh vuc"
if ($r.ok) {
    $r.data.data | ForEach-Object { Write-Host "       [$($_.id)] $($_.name)" -ForegroundColor DarkGray }
}

# --- [2] GET /procedures ---
Write-Host "`n--- [2] GET /procedures ---" -ForegroundColor Yellow
$r = api "GET" "$BASE/procedures"
ok "Lay danh sach thu tuc" $r.ok "$($r.data.data.Count) thu tuc"
if ($r.ok -and $r.data.data.Count -gt 0) {
    $script:procedureId = $r.data.data[0].id
    $r.data.data | Select-Object -First 5 | ForEach-Object {
        Write-Host "       [$($_.id)] $($_.name) ($($_.estimatedDays) ngay)" -ForegroundColor DarkGray
    }
}

# --- [3] GET /appointments/available-slots ---
Write-Host "`n--- [3] GET /appointments/available-slots?date=$tomorrow ---" -ForegroundColor Yellow
$r = api "GET" "$BASE/appointments/available-slots?date=$tomorrow"
ok "Lay slot kha dung" $r.ok
if ($r.ok) {
    $all = $r.data.data.slots
    $avail = $all | Where-Object { $_.available -gt 0 }
    Write-Host "         Tong slot: $($all.Count) | Con cho: $($avail.Count)" -ForegroundColor Gray
    $avail | Select-Object -First 3 | ForEach-Object {
        Write-Host "         $($_.time) - Con: $($_.available)/$($_.maxCapacity)" -ForegroundColor DarkGray
    }
    if ($avail.Count -gt 0) { $script:slotTime = $avail[0].time }
}

# --- [4] POST /appointments (kem Zalo info) ---
Write-Host "`n--- [4] POST /appointments (kem zaloId + zaloName) ---" -ForegroundColor Yellow
$r = api "POST" "$BASE/appointments" @{
    procedureId     = $script:procedureId
    appointmentDate = $tomorrow
    appointmentTime = $script:slotTime
    citizenName     = $HO_TEN
    citizenCccd     = $CCCD
    citizenPhone    = $SDT
    citizenEmail    = $EMAIL
    zaloId          = $ZALO_ID
    zaloName        = $ZALO_NAME
}
ok "Dat lich hen kem Zalo info" $r.ok "$($r.err)$($r.body)"
if ($r.ok) {
    $script:appId = $r.data.data.id
    $script:appCode = $r.data.data.code
    $script:queueDisplay = $r.data.data.queueDisplay
    ok "  zaloLinked = true" ($r.data.data.zaloLinked -eq $true) "zaloLinked=$($r.data.data.zaloLinked)"
    Write-Host "         Ma ho so : $($script:appCode)" -ForegroundColor Gray
    Write-Host "         So thu tu: $($script:queueDisplay)" -ForegroundColor Gray
    Write-Host "         Slot      : $($script:slotTime) ngay $tomorrow" -ForegroundColor Gray
}
else {
    Write-Host "         Error: $($r.err)" -ForegroundColor Red
    Write-Host "         Body : $($r.body)" -ForegroundColor Red
}

# --- [5] GET /queue/{code} ---
Write-Host "`n--- [5] GET /queue/{ticketCode} ---" -ForegroundColor Yellow
if ($script:appCode) {
    $r = api "GET" "$BASE/queue/$($script:appCode)"
    ok "Tra cuu hang cho bang ma ve" $r.ok
    if ($r.ok) {
        $d = $r.data.data
        Write-Host "         STT: $($d.ticketDisplay) | Dang phuc vu: $($d.currentServing) | Cho truoc: $($d.waitingCount) nguoi" -ForegroundColor Gray
    }
}
else { Write-Host "  [SKIP] Khong co appCode" -ForegroundColor DarkGray }

# --- [6] POST /applications/search ---
Write-Host "`n--- [6] POST /applications/search ---" -ForegroundColor Yellow
$r = api "POST" "$BASE/applications/search" @{ cccd = $CCCD }
ok "Tim ho so theo CCCD" $r.ok "Tim thay $($r.data.data.Count) ho so"
$r.data.data | Select-Object -First 3 | ForEach-Object {
    Write-Host "       [$($_.id)] $($_.code) - $($_.procedureName) - $($_.status)" -ForegroundColor DarkGray
}

# --- [7] POST /applications/{id}/view ---
Write-Host "`n--- [7] POST /applications/{id}/view ---" -ForegroundColor Yellow
if ($script:appId) {
    $r = api "POST" "$BASE/applications/$($script:appId)/view" @{ cccd = $CCCD }
    ok "Xem chi tiet ho so" $r.ok
    if ($r.ok) {
        ok "  CCCD khop trong response" ($r.data.data.citizenCccd -eq $CCCD) "citizenCccd=$($r.data.data.citizenCccd)"
        Write-Host "         Ma: $($r.data.data.code) | TT: $($r.data.data.status) | Ten: $($r.data.data.citizenName)" -ForegroundColor Gray
    }
    # Security: CCCD sai -> 404
    $r2 = api "POST" "$BASE/applications/$($script:appId)/view" @{ cccd = "000000000000" }
    ok "  Security: CCCD sai -> 404 (chong enum ID)" ($r2.ok -eq $false) "Expected FAIL"
}
else { Write-Host "  [SKIP] Khong co appId" -ForegroundColor DarkGray }

# --- [8] POST /applications/{id}/history ---
Write-Host "`n--- [8] POST /applications/{id}/history ---" -ForegroundColor Yellow
if ($script:appId) {
    $r = api "POST" "$BASE/applications/$($script:appId)/history" @{ cccd = $CCCD }
    ok "Xem lich su xu ly" $r.ok "$($r.data.data.Count) buoc"
    $r.data.data | ForEach-Object {
        Write-Host "         [$($_.createdAt)] $($_.action): $($_.statusFrom) -> $($_.statusTo)" -ForegroundColor DarkGray
    }
}
else { Write-Host "  [SKIP] Khong co appId" -ForegroundColor DarkGray }

# --- [9] POST /appointments/search ---
Write-Host "`n--- [9] POST /appointments/search ---" -ForegroundColor Yellow
$r = api "POST" "$BASE/appointments/search" @{ cccd = $CCCD }
ok "Tim lich hen theo CCCD" $r.ok "Tim thay $($r.data.data.Count) lich hen"
$r.data.data | Select-Object -First 3 | ForEach-Object {
    Write-Host "       [$($_.id)] $($_.code) - $($_.status) - $($_.appointmentDate) $($_.appointmentTime)" -ForegroundColor DarkGray
}
$r2 = api "POST" "$BASE/appointments/search" @{ cccd = $CCCD; status = "UPCOMING" }
ok "  Filter UPCOMING" $r2.ok "Count=$($r2.data.data.Count)"

# --- [10] POST /appointments/{id}/view ---
Write-Host "`n--- [10] POST /appointments/{id}/view ---" -ForegroundColor Yellow
if ($script:appId) {
    $r = api "POST" "$BASE/appointments/$($script:appId)/view" @{ cccd = $CCCD }
    ok "Xem chi tiet lich hen" $r.ok
    if ($r.ok) {
        $d = $r.data.data
        Write-Host "         Ma: $($d.code) | STT: $($d.queueDisplay) | Ngay: $($d.appointmentDate) $($d.appointmentTime) | Cho truoc: $($d.peopleAhead) nguoi" -ForegroundColor Gray
    }
    else {
        Write-Host "         Error: $($r.err)" -ForegroundColor Red
        Write-Host "         Body : $($r.body)" -ForegroundColor Red
    }
}
else { Write-Host "  [SKIP] Khong co appId" -ForegroundColor DarkGray }

# --- [11] POST /reports (Gop y) ---
Write-Host "`n--- [11] POST /reports (Gop y) ---" -ForegroundColor Yellow
$r = api "POST" "$BASE/reports" @{
    type          = 1
    title         = "Gop y dich vu dat lich online"
    content       = "He thong dat lich qua Zalo Mini App rat tien loi. De xuat them thong bao nhac lich."
    citizenCccd   = $CCCD
    citizenName   = $HO_TEN
    applicationId = $script:appId
}
ok "Gui gop y kem ho so" $r.ok "$($r.err)$($r.body)"
if ($r.ok) {
    Write-Host "         Feedback ID: $($r.data.data.id) | Status: $($r.data.data.status)" -ForegroundColor Gray
}

# --- [12] POST /reports/search ---
Write-Host "`n--- [12] POST /reports/search ---" -ForegroundColor Yellow
$r = api "POST" "$BASE/reports/search" @{ cccd = $CCCD }
ok "Xem gop y theo CCCD" $r.ok "Tim thay $($r.data.data.Count) gop y"

# --- [13] POST /appointments/{id}/cancel ---
Write-Host "`n--- [13] POST /appointments/{id}/cancel ---" -ForegroundColor Yellow
if ($script:appId) {
    $r = api "POST" "$BASE/appointments/$($script:appId)/cancel" @{ cccd = $CCCD }
    ok "Huy lich hen" $r.ok "$($r.err)$($r.body)"
    # Huy lan 2 phai FAIL
    $r2 = api "POST" "$BASE/appointments/$($script:appId)/cancel" @{ cccd = $CCCD }
    ok "  Huy lan 2 phai FAIL (da huy roi)" ($r2.ok -eq $false) "Expected FAIL"
}
else { Write-Host "  [SKIP] Khong co appId" -ForegroundColor DarkGray }

# --- [14] Validate CCCD khong hop le ---
Write-Host "`n--- [14] Validate CCCD khong hop le ---" -ForegroundColor Yellow
$r = api "POST" "$BASE/appointments/search" @{ cccd = "123" }
ok "CCCD qua ngan (3 ky tu) -> reject" ($r.ok -eq $false)
$r = api "POST" "$BASE/appointments/search" @{ cccd = "abcdefabcdef" }
ok "CCCD co chu -> reject" ($r.ok -eq $false)

# --- TONG KET ---
$total = $script:pass + $script:fail
Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  KET QUA TEST ZALO MINI APP API" -ForegroundColor Cyan
Write-Host "  PASS: $($script:pass) / $total" -ForegroundColor $(if ($script:pass -eq $total) { "Green" } else { "Yellow" })
Write-Host "  FAIL: $($script:fail) / $total" -ForegroundColor $(if ($script:fail -gt 0) { "Red" } else { "Green" })
Write-Host "==================================================" -ForegroundColor Cyan
