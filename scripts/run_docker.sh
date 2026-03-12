#!/usr/bin/env bash
# ==============================================================================
# run_docker.sh - Script khởi động Docker cho Hành Chính Công Q5
#
# Cách dùng:
#   ./scripts/run_docker.sh          # Khởi động (dev mode)
#   ./scripts/run_docker.sh stop     # Dừng
#   ./scripts/run_docker.sh rebuild  # Rebuild image rồi start
#   ./scripts/run_docker.sh logs     # Xem logs
#   ./scripts/run_docker.sh status   # Kiểm tra trạng thái
#   ./scripts/run_docker.sh test     # Start rồi chạy test
#
# Yêu cầu: docker, docker compose v2
# ==============================================================================

set -euo pipefail

# ─── CONFIG ──────────────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.dev.yml"
PROJECT_NAME="cholon"

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
BLUE='\033[0;34m'; CYAN='\033[0;36m'; BOLD='\033[1m'; NC='\033[0m'

info()    { echo -e "${CYAN}[INFO]${NC} $*"; }
success() { echo -e "${GREEN}[OK]${NC}   $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC} $*"; }
error()   { echo -e "${RED}[ERR]${NC}  $*"; }
header()  { echo -e "\n${BOLD}${BLUE}▶ $*${NC}"; }

# ─── CHECKS ──────────────────────────────────────────────────────────────────
check_deps() {
    command -v docker &>/dev/null || { error "Docker chưa cài! Xem: https://docs.docker.com/get-docker/"; exit 1; }
    docker compose version &>/dev/null || { error "Docker Compose v2 chưa cài!"; exit 1; }
    success "Docker: $(docker --version | cut -d' ' -f3 | tr -d ',')"
    success "Compose: $(docker compose version --short)"
}

# ─── COMPOSE WRAPPER ─────────────────────────────────────────────────────────
dc() {
    docker compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" "$@"
}

# ─── COMMANDS ────────────────────────────────────────────────────────────────
cmd_start() {
    header "Khởi động hệ thống (dev mode)"
    info "Compose file: $COMPOSE_FILE"
    info "Project root: $PROJECT_ROOT"

    dc up -d --build

    echo ""
    info "Chờ backend sẵn sàng..."
    local max_wait=120
    local waited=0
    while (( waited < max_wait )); do
        if curl -sf http://localhost:8081/actuator/health &>/dev/null; then
            echo ""
            success "Backend sẵn sàng!"
            break
        fi
        echo -n "."
        sleep 3
        waited=$((waited + 3))
    done

    echo ""
    if (( waited >= max_wait )); then
        warn "Backend chưa sẵn sàng sau ${max_wait}s - kiểm tra logs: ./scripts/run_docker.sh logs"
    fi

    cmd_status
}

cmd_stop() {
    header "Dừng hệ thống"
    dc down
    success "Đã dừng tất cả containers"
}

cmd_rebuild() {
    header "Rebuild Docker image"
    info "Dừng containers cũ..."
    dc down
    info "Build lại image..."
    dc build --no-cache backend
    info "Khởi động lại..."
    cmd_start
}

cmd_logs() {
    header "Xem logs"
    local service="${2:-}"
    if [[ -n "$service" ]]; then
        dc logs -f "$service"
    else
        dc logs -f --tail=100
    fi
}

cmd_status() {
    header "Trạng thái hệ thống"
    dc ps

    echo ""
    info "Kiểm tra endpoints:"

    # Health check
    if curl -sf http://localhost:8081/actuator/health &>/dev/null; then
        local health; health=$(curl -s http://localhost:8081/actuator/health | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('status','?'))" 2>/dev/null || echo "UP")
        success "Backend   : http://localhost:8081  [status: $health]"
    else
        error "Backend   : http://localhost:8081  [KHÔNG PHẢN HỒI]"
    fi

    # DB check
    if docker exec cholon_db pg_isready -U postgres -q 2>/dev/null; then
        success "Database  : localhost:5432  [PostgreSQL OK]"
    else
        warn "Database  : localhost:5432  [không kiểm tra được]"
    fi

    echo ""
    echo -e "${BOLD}  API Endpoints:${NC}"
    echo "   Auth    : POST http://localhost:8081/api/auth/login"
    echo "   Citizen : GET  http://localhost:8081/api/citizen/procedures"
    echo "   Staff   : GET  http://localhost:8081/api/staff/hoso (cần JWT)"
    echo "   Admin   : GET  http://localhost:8081/api/admin/staff (cần JWT Admin)"
    echo "   Health  : GET  http://localhost:8081/actuator/health"
    echo ""
    echo -e "  ${BOLD}Tài khoản mặc định:${NC}"
    echo "   Admin: ADMIN / admin123"
    echo "   Staff: NV001, NV002, NV003 / 123456"
}

cmd_test() {
    header "Chạy test API"

    # Nếu chưa start, start trước
    if ! curl -sf http://localhost:8081/actuator/health &>/dev/null; then
        warn "Server chưa chạy, đang khởi động..."
        cmd_start
    fi

    local test_script="$SCRIPT_DIR/test_api.sh"
    if [[ -f "$test_script" ]]; then
        bash "$test_script"
    else
        error "Không tìm thấy test_api.sh tại: $test_script"
        exit 1
    fi
}

cmd_shell() {
    header "Mở shell trong backend container"
    docker exec -it cholon_backend sh
}

cmd_psql() {
    header "Mở psql trong postgres container"
    docker exec -it cholon_db psql -U postgres -d cholon_db
}

cmd_reset_db() {
    header "Reset database (XÓA TẤT CẢ DATA)"
    read -p "Bạn chắc chắn muốn xóa toàn bộ database? (y/N): " confirm
    if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
        info "Đã hủy"
        return
    fi
    dc down -v
    success "Đã xóa volumes. Chạy 'start' để tạo lại database sạch"
}

# ─── MAIN ────────────────────────────────────────────────────────────────────
echo ""
echo -e "${BOLD}${CYAN}╔═══════════════════════════════════════════════════╗${NC}"
echo -e "${BOLD}${CYAN}║  Hành Chính Công Q5 - Docker Manager             ║${NC}"
echo -e "${BOLD}${CYAN}╚═══════════════════════════════════════════════════╝${NC}"

check_deps

CMD="${1:-start}"
case "$CMD" in
    start)    cmd_start ;;
    stop)     cmd_stop ;;
    restart)  cmd_stop; cmd_start ;;
    rebuild)  cmd_rebuild ;;
    logs)     cmd_logs "$@" ;;
    status)   cmd_status ;;
    test)     cmd_test ;;
    shell)    cmd_shell ;;
    psql|db)  cmd_psql ;;
    reset-db) cmd_reset_db ;;
    *)
        echo ""
        echo "Sử dụng: $0 {start|stop|restart|rebuild|logs|status|test|shell|psql|reset-db}"
        echo ""
        echo "  start     - Khởi động hệ thống (default)"
        echo "  stop      - Dừng tất cả containers"
        echo "  restart   - Dừng rồi start lại"
        echo "  rebuild   - Build lại Docker image rồi start"
        echo "  logs      - Xem logs realtime"
        echo "  status    - Kiểm tra trạng thái các service"
        echo "  test      - Chạy test_api.sh"
        echo "  shell     - Mở shell trong backend container"
        echo "  psql      - Mở psql console"
        echo "  reset-db  - Xóa database và volumes"
        echo ""
        exit 1
        ;;
esac
