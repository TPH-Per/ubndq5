#!/usr/bin/env bash
# ==============================================================================
# test_api.sh - Test script cho chức năng mới (citizen inline, no FK)
# Hệ thống Hành chính công Q5 Chợ Lớn
#
# Cách dùng:
#   chmod +x test_api.sh
#   ./test_api.sh                         # Chạy toàn bộ test
#   API_BASE_URL=http://x.x.x.x:8081 ./test_api.sh
#
# Yêu cầu: curl, jq
# ==============================================================================

# ─── CẤU HÌNH ────────────────────────────────────────────────────────────────
BASE_URL="${API_BASE_URL:-http://localhost:8081}"
ADMIN_CODE="${ADMIN_CODE:-ADMIN}"
ADMIN_PASS="${ADMIN_PASS:-admin123}"
STAFF_CODE="${STAFF_CODE:-NV001}"
STAFF_PASS="${STAFF_PASS:-123456}"

# Màu sắc terminal
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
BLUE='\033[0;34m'; CYAN='\033[0;36m'; BOLD='\033[1m'; NC='\033[0m'

# Counters
PASS=0; FAIL=0; SKIP=0
ADMIN_TOKEN=""; STAFF_TOKEN=""

# ==============================================================================
# HELPER FUNCTIONS
# ==============================================================================
log_section() {
    echo ""
    echo -e "${BOLD}${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BOLD}${BLUE}  $1${NC}"
    echo -e "${BOLD}${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}
log_test() { echo -e "${CYAN}  ▶ TEST: $1${NC}"; }
pass()     { echo -e "    ${GREEN}✓ PASSED${NC} - $1"; PASS=$((PASS+1)); }
fail()     { echo -e "    ${RED}✗ FAILED${NC} - $1"; FAIL=$((FAIL+1)); }
skip()     { echo -e "    ${YELLOW}⟳ SKIPPED${NC} - $1"; SKIP=$((SKIP+1)); }

api_call() {
    local method="$1" path="$2" token="${3:-}" body="${4:-}"
    if [[ -n "$body" ]]; then
        curl -s -X "$method" -H "Content-Type: application/json" \
            ${token:+-H "Authorization: Bearer $token"} \
            -d "$body" "${BASE_URL}${path}" 2>/dev/null
    else
        curl -s -X "$method" -H "Content-Type: application/json" \
            ${token:+-H "Authorization: Bearer $token"} \
            "${BASE_URL}${path}" 2>/dev/null
    fi
}

api_status() {
    local method="$1" path="$2" token="${3:-}" body="${4:-}"
    if [[ -n "$body" ]]; then
        curl -s -o /dev/null -w "%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            ${token:+-H "Authorization: Bearer $token"} \
            -d "$body" "${BASE_URL}${path}" 2>/dev/null
    else
        curl -s -o /dev/null -w "%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            ${token:+-H "Authorization: Bearer $token"} \
            "${BASE_URL}${path}" 2>/dev/null
    fi
}

check_success() {
    local json="$1"
    echo "$json" | jq -r ".success" 2>/dev/null | grep -q "true"
}

rand9() { echo "$(date +%s%N 2>/dev/null || date +%s)" | tail -c 10 | head -c 9; }
rand_cccd() { echo "079$(rand9)"; }

# ==============================================================================
# SECTION 1: HEALTH CHECK
# ==============================================================================
test_health() {
    log_section "1. HEALTH CHECK"
    log_test "Server đang chạy tại $BASE_URL"
    local status
    status=$(api_status "GET" "/api/auth/health" 2>/dev/null || echo "000")
    if [[ "$status" == "200" ]]; then
        pass "Server trả về 200 OK"
    elif [[ "$status" == "000" ]]; then
        fail "Không kết nối được server tại $BASE_URL (connection refused)"
        echo -e "    ${RED}→ Kiểm tra server đang chạy chưa: docker compose up${NC}"
    else
        pass "Server phản hồi với HTTP $status (server đang chạy)"
    fi
}

# ==============================================================================
# SECTION 2: AUTHENTICATION
# ==============================================================================
test_auth() {
    log_section "2. AUTHENTICATION - Login & JWT"

    log_test "Đăng nhập Admin ($ADMIN_CODE)"
    local resp
    resp=$(api_call "POST" "/api/auth/login" "" \
        "{\"staffCode\":\"$ADMIN_CODE\",\"password\":\"$ADMIN_PASS\"}")
    if echo "$resp" | jq -r ".data.token" 2>/dev/null | grep -qv "null"; then
        ADMIN_TOKEN=$(echo "$resp" | jq -r ".data.token")
        pass "Login Admin OK - Token nhận thành công"
    else
        fail "Login Admin thất bại: $(echo "$resp" | jq -r '.message // .error // "server error"' 2>/dev/null)"
    fi

    log_test "Đăng nhập Staff ($STAFF_CODE)"
    resp=$(api_call "POST" "/api/auth/login" "" \
        "{\"staffCode\":\"$STAFF_CODE\",\"password\":\"$STAFF_PASS\"}")
    if echo "$resp" | jq -r ".data.token" 2>/dev/null | grep -qv "null"; then
        STAFF_TOKEN=$(echo "$resp" | jq -r ".data.token")
        pass "Login Staff OK"
    else
        fail "Login Staff thất bại"
    fi

    log_test "Sai mật khẩu -> 401"
    local status
    status=$(api_status "POST" "/api/auth/login" "" \
        "{\"staffCode\":\"$ADMIN_CODE\",\"password\":\"wrong\"}")
    if [[ "$status" == "401" || "$status" == "400" ]]; then
        pass "Sai mật khẩu -> HTTP $status (bảo mật đúng)"
    else
        fail "Sai mật khẩu nhưng HTTP $status (expect 401)"
    fi

    log_test "Không có token -> 401"
    status=$(api_status "GET" "/api/staff/hoso/dashboard")
    if [[ "$status" == "401" || "$status" == "403" ]]; then
        pass "Không token -> HTTP $status (bảo mật đúng)"
    else
        fail "Không token nhưng HTTP $status (expect 401)"
    fi

    log_test "GET /api/auth/me - thông tin hiện tại"
    if [[ -n "$ADMIN_TOKEN" ]]; then
        resp=$(api_call "GET" "/api/auth/me" "$ADMIN_TOKEN")
        if check_success "$resp" 2>/dev/null; then
            pass "Lấy thông tin user: $(echo "$resp" | jq -r '.data.hoTen // .data.fullName // "OK"')"
        else
            fail "Không lấy được /me: $(echo "$resp" | jq -r '.message // "unknown"')"
        fi
    else
        skip "Chưa có Admin token"
    fi
}

# ==============================================================================
# SECTION 3: CITIZEN API - Inline citizen fields (không có bảng Citizen)
# ==============================================================================
test_citizen_api() {
    log_section "3. CITIZEN API - Thông tin công dân Inline (KHÔNG có FK Citizen)"

    local CCCD_A; CCCD_A=$(rand_cccd)
    local CCCD_B; CCCD_B=$(rand_cccd)
    local CREATED_ID="" CREATED_CODE=""
    local tomorrow; tomorrow=$(date -d "+1 day" +%Y-%m-%d 2>/dev/null \
        || date -v+1d +%Y-%m-%d 2>/dev/null || echo "2026-03-10")

    # 3.1 Danh sách chuyên môn
    log_test "GET /api/citizen/specialties"
    local resp; resp=$(api_call "GET" "/api/citizen/specialties")
    if check_success "$resp" 2>/dev/null; then
        pass "Chuyên môn OK: $(echo "$resp" | jq '.data | length') nhóm"
    else
        fail "Lấy chuyên môn thất bại"
    fi

    # 3.2 Danh sách thủ tục
    log_test "GET /api/citizen/procedures"
    resp=$(api_call "GET" "/api/citizen/procedures")
    if check_success "$resp" 2>/dev/null; then
        pass "Thủ tục OK: $(echo "$resp" | jq '.data | length') thủ tục"
    else
        fail "Lấy thủ tục thất bại"
    fi

    # 3.3 Available slots
    log_test "GET /api/citizen/appointments/available-slots?date=$tomorrow"
    resp=$(api_call "GET" "/api/citizen/appointments/available-slots?date=$tomorrow")
    if check_success "$resp" 2>/dev/null; then
        pass "Slots OK: $(echo "$resp" | jq '.data.slots | length') slot"
    else
        fail "Lấy slots thất bại"
    fi

    # 3.4 ĐẶT LỊCH - citizen info INLINE (không cần table Citizen)
    log_test "POST /api/citizen/appointments - Đặt lịch (citizen INLINE)"
    resp=$(api_call "POST" "/api/citizen/appointments" "" "$(cat <<EOF
{
  "procedureId": 1,
  "appointmentDate": "$tomorrow",
  "appointmentTime": "09:06",
  "citizenName": "Nguyễn Văn Test",
  "citizenCccd": "$CCCD_A",
  "citizenPhone": "0901111222",
  "citizenEmail": "nv.test@example.com"
}
EOF
)")
    if check_success "$resp" 2>/dev/null; then
        CREATED_ID=$(echo "$resp" | jq -r ".data.id")
        CREATED_CODE=$(echo "$resp" | jq -r ".data.code")
        pass "Đặt lịch OK - id=$CREATED_ID, code=$CREATED_CODE (CCCD: $CCCD_A lưu inline)"
    else
        fail "Đặt lịch thất bại: $(echo "$resp" | jq -r '.message // .data // "unknown"' 2>/dev/null)"
    fi

    # 3.5 CCCD sai format -> 400
    log_test "POST /api/citizen/appointments - CCCD sai (6 số) -> 400"
    local bad_status; bad_status=$(api_status "POST" "/api/citizen/appointments" "" "$(cat <<EOF
{
  "procedureId": 1,
  "appointmentDate": "$tomorrow",
  "appointmentTime": "09:30",
  "citizenName": "Test",
  "citizenCccd": "123456",
  "citizenPhone": "0901111222"
}
EOF
)")
    if [[ "$bad_status" == "400" ]]; then
        pass "Validation CCCD OK -> HTTP 400"
    else
        fail "CCCD sai nhưng HTTP $bad_status (expect 400)"
    fi

    # 3.6 Tra cứu theo CCCD
    log_test "GET /api/citizen/appointments?cccd=$CCCD_A"
    resp=$(api_call "GET" "/api/citizen/appointments?cccd=$CCCD_A")
    if check_success "$resp" 2>/dev/null; then
        pass "Tra cứu theo CCCD OK: $(echo "$resp" | jq '.data | length') lịch hẹn"
    else
        fail "Tra cứu CCCD thất bại"
    fi

    # 3.7 Chi tiết - xác nhận citizen name lưu inline
    if [[ -n "$CREATED_ID" && "$CREATED_ID" != "null" ]]; then
        log_test "GET /api/citizen/appointments/$CREATED_ID?cccd=$CCCD_A - verify inline data"
        resp=$(api_call "GET" "/api/citizen/appointments/$CREATED_ID?cccd=$CCCD_A")
        if check_success "$resp" 2>/dev/null; then
            local name_in_resp; name_in_resp=$(echo "$resp" | jq -r ".data.citizenName // .data.hoTenCongDan")
            pass "Chi tiết OK - citizenName='$name_in_resp' (lưu inline trong Application)"
        else
            fail "Chi tiết thất bại"
        fi
    fi

    # 3.8 Đặt lịch KÈM Zalo ID (ZaloAccount chỉ để notify)
    log_test "POST /api/citizen/appointments - CÓ zaloId (chỉ để gửi thông báo)"
    resp=$(api_call "POST" "/api/citizen/appointments" "" "$(cat <<EOF
{
  "procedureId": 2,
  "appointmentDate": "$tomorrow",
  "appointmentTime": "10:18",
  "citizenName": "Trần Thị Zalo",
  "citizenCccd": "$CCCD_B",
  "citizenPhone": "0912222333",
  "zaloId": "zalo_test_$(date +%s)",
  "zaloName": "Tran Thi Zalo"
}
EOF
)")
    if check_success "$resp" 2>/dev/null; then
        local zl; zl=$(echo "$resp" | jq -r ".data.zaloLinked")
        pass "Đặt lịch+Zalo OK - zaloLinked=$zl (Zalo lưu vào ZaloAccount riêng)"
    else
        fail "Đặt lịch+Zalo thất bại: $(echo "$resp" | jq -r '.message // "unknown"' 2>/dev/null)"
    fi

    # 3.9 Hủy lịch
    if [[ -n "$CREATED_ID" && "$CREATED_ID" != "null" ]]; then
        log_test "POST /api/citizen/appointments/$CREATED_ID/cancel"
        local cs; cs=$(api_status "POST" "/api/citizen/appointments/$CREATED_ID/cancel?cccd=$CCCD_A")
        if [[ "$cs" == "200" ]]; then
            pass "Hủy lịch OK"
        else
            fail "Hủy lịch thất bại (HTTP $cs)"
        fi
    fi

    # 3.10 Tra cứu hàng chờ
    if [[ -n "$CREATED_CODE" && "$CREATED_CODE" != "null" ]]; then
        log_test "GET /api/citizen/queue/$CREATED_CODE"
        resp=$(api_call "GET" "/api/citizen/queue/$CREATED_CODE")
        if check_success "$resp" 2>/dev/null; then
            pass "Tra cứu hàng chờ OK"
        else
            fail "Tra cứu hàng chờ thất bại"
        fi
    fi

    # 3.11 Gửi góp ý (citizen inline trong Feedback)
    log_test "POST /api/citizen/reports - Gửi góp ý (CCCD inline)"
    resp=$(api_call "POST" "/api/citizen/reports" "" "$(cat <<EOF
{
  "type": 1,
  "title": "Test góp ý từ script",
  "content": "Nội dung test tự động - $(date '+%Y-%m-%d %H:%M')",
  "citizenCccd": "$CCCD_A",
  "citizenName": "Nguyễn Văn Test"
}
EOF
)")
    if check_success "$resp" 2>/dev/null; then
        pass "Gửi góp ý OK: id=$(echo "$resp" | jq -r '.data.id')"
    else
        fail "Gửi góp ý thất bại: $(echo "$resp" | jq -r '.message // "unknown"' 2>/dev/null)"
    fi
}

# ==============================================================================
# SECTION 4: STAFF HỒ SƠ API
# ==============================================================================
test_staff_hoso() {
    log_section "4. STAFF HỒ SƠ API - Citizen Inline"

    if [[ -z "$STAFF_TOKEN" ]]; then
        skip "Bỏ qua section (chưa có Staff token)"; return
    fi

    local CCCD_TEST; CCCD_TEST=$(rand_cccd)
    local CREATED_ID=""

    # 4.1 Dashboard
    log_test "GET /api/staff/hoso/dashboard"
    local resp; resp=$(api_call "GET" "/api/staff/hoso/dashboard" "$STAFF_TOKEN")
    if check_success "$resp" 2>/dev/null; then
        pass "Dashboard OK: tongHoSo=$(echo "$resp" | jq '.data.tongSoHoSo')"
    else fail "Dashboard thất bại"; fi

    # 4.2 Tạo hồ sơ MỚI - citizen inline, không có conflict check
    log_test "POST /api/staff/hoso - Tạo hồ sơ (citizen INLINE, KHÔNG conflict check)"
    resp=$(api_call "POST" "/api/staff/hoso" "$STAFF_TOKEN" "$(cat <<EOF
{
  "cccd": "$CCCD_TEST",
  "hoTen": "Công Dân Mới Test",
  "soDienThoai": "0902100200",
  "email": "cd@example.com",
  "loaiThuTucId": 1,
  "doUuTien": 0
}
EOF
)")
    if check_success "$resp" 2>/dev/null; then
        CREATED_ID=$(echo "$resp" | jq -r ".data.id")
        local cccd_r; cccd_r=$(echo "$resp" | jq -r ".data.cccd")
        pass "Tạo hồ sơ OK - id=$CREATED_ID, cccd=$cccd_r (lưu inline)"
    else
        fail "Tạo hồ sơ thất bại: $(echo "$resp" | jq -r '.message // "unknown"' 2>/dev/null)"
    fi

    # 4.3 Cùng CCCD, tên khác - KHÔNG được báo 409 CITIZEN_CONFLICT nữa
    log_test "POST /api/staff/hoso - CCCD giống, tên khác -> KHÔNG còn 409 CONFLICT"
    resp=$(api_call "POST" "/api/staff/hoso" "$STAFF_TOKEN" "$(cat <<EOF
{
  "cccd": "$CCCD_TEST",
  "hoTen": "Tên Hoàn Toàn Khác",
  "soDienThoai": "0903111222",
  "loaiThuTucId": 2,
  "doUuTien": 1
}
EOF
)")
    local http_msg; http_msg=$(echo "$resp" | jq -r '.error // .code // ""' 2>/dev/null)
    if echo "$http_msg" | grep -qi "CITIZEN_CONFLICT"; then
        fail "VẪN còn CITIZEN_CONFLICT (409) - chưa refactor xong!"
    elif check_success "$resp" 2>/dev/null; then
        pass "OK - Không còn CITIZEN_CONFLICT. Cùng CCCD có thể tạo nhiều hồ sơ khác tên"
    else
        fail "Thất bại với lý do khác: $(echo "$resp" | jq -r '.message // "unknown"' 2>/dev/null)"
    fi

    # 4.4 Chi tiết hồ sơ - verify citizen inline
    if [[ -n "$CREATED_ID" && "$CREATED_ID" != "null" ]]; then
        log_test "GET /api/staff/hoso/$CREATED_ID - verify citizen inline fields"
        resp=$(api_call "GET" "/api/staff/hoso/$CREATED_ID" "$STAFF_TOKEN")
        if check_success "$resp" 2>/dev/null; then
            local cccd_r; cccd_r=$(echo "$resp" | jq -r ".data.cccd")
            local name_r; name_r=$(echo "$resp" | jq -r ".data.hoTenCongDan")
            pass "Chi tiết OK - cccd='$cccd_r' (inline), tên='$name_r'"
        else fail "Chi tiết thất bại"; fi
    fi

    # 4.5 Cập nhật thông tin inline
    if [[ -n "$CREATED_ID" && "$CREATED_ID" != "null" ]]; then
        log_test "PUT /api/staff/hoso/$CREATED_ID - Cập nhật inline citizen fields"
        resp=$(api_call "PUT" "/api/staff/hoso/$CREATED_ID" "$STAFF_TOKEN" \
            '{"hoTen": "Tên Đã Cập Nhật Script", "soDienThoai": "0909876543"}')
        if check_success "$resp" 2>/dev/null; then
            local new_name; new_name=$(echo "$resp" | jq -r ".data.hoTenCongDan")
            pass "Cập nhật OK - tên mới: '$new_name'"
        else
            fail "Cập nhật thất bại: $(echo "$resp" | jq -r '.message // "unknown"' 2>/dev/null)"
        fi
    fi

    # 4.6 Danh sách hồ sơ
    log_test "GET /api/staff/hoso"
    resp=$(api_call "GET" "/api/staff/hoso" "$STAFF_TOKEN")
    if check_success "$resp" 2>/dev/null; then
        pass "Danh sách OK: $(echo "$resp" | jq '.data | length') hồ sơ"
    else fail "Danh sách hồ sơ thất bại"; fi
}

# ==============================================================================
# SECTION 5: STAFF QUEUE
# ==============================================================================
test_staff_queue() {
    log_section "5. STAFF QUEUE - Hàng chờ"

    if [[ -z "$STAFF_TOKEN" ]]; then
        skip "Bỏ qua (chưa có Staff token)"; return
    fi

    local today; today=$(date +%Y-%m-%d)

    for endpoint in "/api/staff/queue/dashboard" "/api/staff/queue/waiting" "/api/staff/queue/current"; do
        log_test "GET $endpoint"
        local resp; resp=$(api_call "GET" "$endpoint" "$STAFF_TOKEN")
        if check_success "$resp" 2>/dev/null; then
            pass "OK"
        else
            fail "Thất bại: $(echo "$resp" | jq -r '.message // "unknown"' 2>/dev/null)"
        fi
    done

    log_test "GET /api/staff/queue/slots?date=$today"
    local resp; resp=$(api_call "GET" "/api/staff/queue/slots?date=$today" "$STAFF_TOKEN")
    if check_success "$resp" 2>/dev/null; then
        pass "Slots OK"
    else fail "Slots thất bại"; fi
}

# ==============================================================================
# SECTION 6: ADMIN API
# ==============================================================================
test_admin_api() {
    log_section "6. ADMIN API"

    if [[ -z "$ADMIN_TOKEN" ]]; then
        skip "Bỏ qua (chưa có Admin token)"; return
    fi

    for endpoint in "/api/admin/staff" "/api/admin/quays" "/api/admin/chuyenmons" "/api/admin/loaithutucs"; do
        log_test "GET $endpoint"
        local resp; resp=$(api_call "GET" "$endpoint" "$ADMIN_TOKEN")
        if echo "$resp" | jq -e 'type == "array"' >/dev/null 2>&1; then
            pass "OK: $(echo "$resp" | jq 'length') items"
        elif check_success "$resp" 2>/dev/null; then
            pass "OK: $(echo "$resp" | jq '.data | length') items"
        else
            fail "Thất bại: $(echo "$resp" | jq -r '.message // "unknown"' 2>/dev/null)"
        fi
    done

    # Staff không được dùng Admin API
    log_test "Staff dùng Admin API -> 403"
    if [[ -n "$STAFF_TOKEN" ]]; then
        local status; status=$(api_status "GET" "/api/admin/staff" "$STAFF_TOKEN")
        if [[ "$status" == "403" ]]; then
            pass "Staff bị chặn đúng (403)"
        else fail "Staff truy cập Admin API được (HTTP $status) - SECURITY BUG!"; fi
    else skip "Chưa có Staff token"; fi
}

# ==============================================================================
# MAIN
# ==============================================================================
main() {
    echo ""
    echo -e "${BOLD}${CYAN}╔══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BOLD}${CYAN}║   HÀNH CHÍNH CÔNG Q5 - API TEST SUITE v2.0               ║${NC}"
    echo -e "${BOLD}${CYAN}║   Kiểm tra chức năng: Citizen Inline (no FK)             ║${NC}"
    echo -e "${BOLD}${CYAN}║   Target: ${BASE_URL}                            ║${NC}"
    echo -e "${BOLD}${CYAN}╚══════════════════════════════════════════════════════════╝${NC}"
    echo -e "  Thời gian: $(date '+%Y-%m-%d %H:%M:%S')"

    command -v curl &>/dev/null || { echo -e "${RED}LỖI: curl chưa cài${NC}"; exit 1; }
    command -v jq   &>/dev/null || { echo -e "${RED}LỖI: jq chưa cài (apt install jq)${NC}"; exit 1; }

    test_health
    test_auth
    test_citizen_api
    test_staff_hoso
    test_staff_queue
    test_admin_api

    # ── KẾT QUẢ ───────────────────────────────────────────────────────────────
    local total=$((PASS+FAIL+SKIP))
    echo ""
    echo -e "${BOLD}${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BOLD}  KẾT QUẢ: $total tests | ${GREEN}✓ $PASS passed${NC} | ${RED}✗ $FAIL failed${NC} | ${YELLOW}⟳ $SKIP skipped${NC}"
    echo -e "${BOLD}${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

    if [[ $FAIL -eq 0 ]]; then
        echo -e "  ${GREEN}${BOLD}🎉 Tất cả test PASSED!${NC}"
        exit 0
    else
        echo -e "  ${RED}${BOLD}❌ $FAIL test FAILED!${NC}"
        exit 1
    fi
}

main "$@"
