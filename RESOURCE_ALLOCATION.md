# =============================================================
# CHIẾN LƯỢC PHÂN BỔ TÀI NGUYÊN - PRODUCTION
# Ubuntu Server + Docker + Nginx
# Intel Core i5 Gen 12 + 16GB RAM
# =============================================================

## 📊 TỔNG QUAN KIẾN TRÚC

```
┌─────────────────────────────────────────────────────────────────┐
│                         INTERNET                                 │
└───────────────┬─────────────────────────────────────────────────┘
                │
   ┌────────────┴────────────┐
   │                         │
   ▼                         ▼
┌─────────────────┐    ┌─────────────────────────────────────────┐
│  ZALO SERVERS   │    │          UBUNTU SERVER (16GB)           │
│  (Của Zalo)     │    │          192.168.x.x (Nội bộ)           │
│                 │    │          your-api.com (Public)          │
│  📱 Client      │    ├─────────────────────────────────────────┤
│  (Zalo Mini App)│    │                                         │
│                 │    │  🌐 Nginx (128MB)                       │
│  - Code chạy    │    │     ├── /admin → AdminStaff (static)    │
│    trên Zalo    │    │     └── /api   → Backend (proxy)        │
│  - KHÔNG TỐN    │    │                                         │
│    RAM server   │───▶│  ☕ Backend Spring Boot (4GB)           │
│                 │API │     └── Xử lý API requests              │
└─────────────────┘    │                                         │
                       │  🐘 PostgreSQL (4GB)                    │
   INTERNAL NETWORK    │     └── Database                        │
   (Mạng nội bộ)       │                                         │
        │              │  🖥️ Ubuntu OS + Docker (~3GB)           │
        │              │                                         │
        ▼              │  💾 Buffer/Cache (~5GB)                 │
   ┌─────────────┐     │                                         │
   │ Staff/Admin │     └─────────────────────────────────────────┘
   │ Computers   │
   │             │──▶ http://192.168.x.x/admin
   └─────────────┘
```

---

## 🔢 PHÂN BỔ CHI TIẾT (16GB RAM)

| Component | RAM | CPU | Public? | Ghi chú |
|-----------|-----|-----|---------|---------|
| **Client (Zalo)** | **0 MB** | 0 | ✅ | Zalo host, KHÔNG tốn RAM |
| **PostgreSQL** | 4 GB | 2 cores | ❌ | Database chính |
| **Backend API** | 4 GB | 3 cores | ✅ | `-Xmx3g` heap + overhead |
| **Nginx** | 128 MB | 0.5 core | ✅/❌ | Proxy + Static files |
| **AdminStaff** | ~50 MB | - | ❌ | Static files (Nginx serve) |
| **Ubuntu + Docker** | 3 GB | 1.5 cores | - | System overhead |
| **Buffer/Cache** | ~5 GB | - | - | Linux file cache |

**Tổng: ~16GB** (tối ưu, không lãng phí)

---

## 🔧 PHÂN BỔ CPU CHI TIẾT

### Intel i5 Gen 12 - Thông số CPU

| Model | P-Cores | E-Cores | Threads | Kiến trúc |
|-------|---------|---------|---------|-----------|
| i5-12400/12500 | 6 | 0 | 12 | 6C/12T |
| i5-12600K | 6 | 4 | 16 | 10C/16T |

### Phân bổ cho 6 Core / 12 Thread (i5-12400/12500)

```
┌────────────────────────────────────────────────────────────┐
│                    6 CORES / 12 THREADS                    │
├────────────────────────────────────────────────────────────┤
│  Core 0-1  │  Core 2-3  │  Core 4  │  Core 5  │            │
│  (4 thrd)  │  (4 thrd)  │ (2 thrd) │ (2 thrd) │            │
├────────────┼────────────┼──────────┼──────────┤            │
│ PostgreSQL │  Backend   │  Nginx   │  Ubuntu  │            │
│   2 cores  │  3 cores   │ 0.5 core │ 0.5 core │            │
│  (33%)     │  (50%)     │  (8%)    │  (8%)    │            │
└────────────────────────────────────────────────────────────┘
```

| Component | CPU Cores | % CPU | Threads | Docker Limit |
|-----------|-----------|-------|---------|--------------|
| **PostgreSQL** | 2 | 33% | 4 | `cpus: '2.0'` |
| **Backend (Spring)** | 3 | 50% | 6 | `cpus: '3.0'` |
| **Nginx** | 0.5 | 8% | 1 | `cpus: '0.5'` |
| **Ubuntu OS** | 0.5 | 8% | 1 | Reserved |
| **Tổng** | 6 | 100% | 12 | |

### Phân bổ cho 10 Core / 16 Thread (i5-12600K)

```
┌─────────────────────────────────────────────────────────────────────────┐
│              10 CORES / 16 THREADS (6P + 4E)                           │
├─────────────────────────────────────────────────────────────────────────┤
│  P-Core 0-1  │  P-Core 2-4  │  E-Core 0-1  │  E-Core 2-3  │            │
│   (4 thrd)   │   (6 thrd)   │   (2 thrd)   │   (2 thrd)   │            │
├──────────────┼──────────────┼──────────────┼──────────────┤            │
│  PostgreSQL  │   Backend    │    Nginx     │   Ubuntu     │            │
│   2 cores    │   4 cores    │   1 core     │   1 core     │            │
│    (20%)     │    (40%)     │   (10%)      │   (10%)      │            │
└─────────────────────────────────────────────────────────────────────────┘
```

| Component | CPU Cores | % CPU | Docker Limit |
|-----------|-----------|-------|--------------|
| **PostgreSQL** | 2 | 20% | `cpus: '2.0'` |
| **Backend (Spring)** | 4 | 40% | `cpus: '4.0'` |
| **Nginx** | 1 | 10% | `cpus: '1.0'` |
| **Ubuntu OS** | 2 | 20% | Reserved |
| **Headroom** | 1 | 10% | Buffer |
| **Tổng** | 10 | 100% | |

---

## ⚙️ CẤU HÌNH CPU TRONG DOCKER

```yaml
# docker-compose.prod.yml
services:
  postgres:
    deploy:
      resources:
        limits:
          cpus: '2.0'      # 2 cores cho database
          
  backend:
    deploy:
      resources:
        limits:
          cpus: '3.0'      # 3 cores (hoặc 4 cho i5-12600K)
          
  nginx:
    deploy:
      resources:
        limits:
          cpus: '0.5'      # 0.5 core (hoặc 1 cho i5-12600K)
```

---

## 🐘 CẤU HÌNH CPU POSTGRESQL

```conf
# postgresql.conf
# Parallel Query Settings cho i5 Gen 12

# 6-core CPU:
max_parallel_workers_per_gather = 2   # Workers per query
max_parallel_workers = 4              # Total parallel workers
max_worker_processes = 8              # Total worker processes

# 10-core CPU (i5-12600K):
max_parallel_workers_per_gather = 3
max_parallel_workers = 6
max_worker_processes = 10
```

---

## ☕ CẤU HÌNH CPU SPRING BOOT

```properties
# application.properties hoặc JAVA_OPTS

# Thread pool cho request handling
server.tomcat.threads.max=200       # Max threads
server.tomcat.threads.min-spare=20  # Min threads ready

# Async task executor
spring.task.execution.pool.core-size=4
spring.task.execution.pool.max-size=8
spring.task.execution.pool.queue-capacity=100
```

---

## 🚀 HƯỚNG DẪN DEPLOY

### Bước 1: Cài đặt Ubuntu Server
```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo apt install docker-compose-plugin -y

# Add user to docker group
sudo usermod -aG docker $USER
```

### Bước 2: Clone và Build
```bash
# Clone project
cd /opt
sudo git clone <your-repo> hanhchinhcong
cd hanhchinhcong

# Build frontend apps
cd client && npm install && npm run build
cd ../AdminStaff && npm install && npm run build
cd ..

# Create environment file
cp .env.example .env
nano .env  # Edit với DB password thật
```

### Bước 3: Deploy với Docker
```bash
# Start all services
docker compose -f docker-compose.prod.yml up -d

# Check logs
docker compose -f docker-compose.prod.yml logs -f

# Check resource usage
docker stats
```

### Bước 4: Deploy Client lên Vercel (Miễn phí)
```bash
# Install Vercel CLI
npm i -g vercel

# Deploy
cd client
vercel --prod
```

---

## 🔧 CẤU HÌNH POSTGRESQL CHO DOCKER

File: `config/postgresql.conf` (cho Docker)
```conf
# Memory (reduced for Docker)
shared_buffers = 2GB
effective_cache_size = 6GB
work_mem = 32MB
maintenance_work_mem = 256MB

# Connections
max_connections = 100

# Parallel Query
max_parallel_workers_per_gather = 2
max_parallel_workers = 4

# Logging
log_min_duration_statement = 1000
```

---

## 🌐 NETWORK TOPOLOGY

```
                    INTERNET
                        │
                        ▼
    ┌───────────────────────────────────┐
    │           ROUTER/FIREWALL          │
    │  Port Forwarding:                  │
    │  - 80  → Ubuntu:80  (HTTP)         │
    │  - 443 → Ubuntu:443 (HTTPS)        │
    └───────────────────────────────────┘
                        │
                        ▼
    ┌───────────────────────────────────┐
    │         UBUNTU SERVER              │
    │         192.168.1.100              │
    ├───────────────────────────────────┤
    │  Docker Network (bridge)           │
    │  ┌─────────────────────────────┐  │
    │  │ nginx    → :80, :443        │  │
    │  │ backend  → :8081 (internal) │  │
    │  │ postgres → :5432 (internal) │  │
    │  └─────────────────────────────┘  │
    └───────────────────────────────────┘
    
    INTERNAL NETWORK (Staff/Admin access)
    ├── 192.168.1.x → http://192.168.1.100/admin
    
    PUBLIC (Citizen access)
    └── https://your-domain.com (Vercel) → calls API at https://api.your-domain.com
```

---

## 📋 ENVIRONMENT FILE (.env)

```bash
# Database
DB_PASSWORD=your_secure_password_here
POSTGRES_USER=postgres

# PgAdmin (optional)
PGADMIN_EMAIL=admin@local.dev
PGADMIN_PASSWORD=admin123

# JWT
JWT_SECRET=your_jwt_secret_key_here

# Domain (for CORS)
ALLOWED_ORIGINS=https://your-domain.com,http://192.168.1.100
```

---

## 🔒 SECURITY CHECKLIST

- [ ] Đổi DB_PASSWORD mặc định
- [ ] Cấu hình firewall (ufw)
- [ ] Chỉ mở port 80, 443 ra ngoài
- [ ] Setup SSL với Let's Encrypt
- [ ] Restrict /admin và /api/admin cho internal network
- [ ] Enable fail2ban
- [ ] Regular backup schedule

---

## 📊 MONITORING

```bash
# Resource usage
docker stats

# Logs
docker compose -f docker-compose.prod.yml logs -f backend
docker compose -f docker-compose.prod.yml logs -f postgres

# PostgreSQL connections
docker exec hanhchinhcong-postgres psql -U postgres -c "SELECT count(*) FROM pg_stat_activity;"
```

---

*Document updated: 2026-02-05*
