# 🚀 KẾ HOẠCH TRIỂN KHAI TOÀN HỆ THỐNG
## Hệ thống Dịch vụ Công - Internal LAN + Public API + Zalo Mini App

---

## 📐 KIẾN TRÚC TỔNG QUAN

```
┌─────────────────────────────────────────────────────────────────┐
│  MẠNG NỘI BỘ (LAN)                                              │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  Docker Compose Stack                                    │    │
│  │                                                          │    │
│  │  [Nginx :80/:443]                                        │    │
│  │     /admin/*    → AdminStaff Vue dist (static)          │    │
│  │     /api/*      → Backend Spring Boot :8081 (proxy)     │    │
│  │     /           → Citizen React dist (optional/local)   │    │
│  │                                                          │    │
│  │  [Spring Boot :8081]                                     │    │
│  │     /api/citizen/**   → PUBLIC (CORS: Vercel + LAN)     │    │
│  │     /api/staff/**     → LAN-ONLY (CORS: LAN origin)     │    │
│  │     /api/admin/**     → LAN-ONLY (CORS: LAN origin)     │    │
│  │                                                          │    │
│  │  [PostgreSQL :5433]                                      │    │
│  │     Internal only, không expose ra ngoài                │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                   │
│  Cán bộ/Admin truy cập:  http://10.x.x.x/admin/                │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  INTERNET PUBLIC                                                  │
│                                                                   │
│  [Cloudflare Tunnel] (KHUYÊN DÙNG - không cần public IP)        │
│     https://api.yourdomain.com  →  Nginx LAN :80/api/citizen/** │
│                                                                   │
│  HOẶC Port Forwarding Router:                                    │
│     Router WAN :443  →  Nginx LAN :443                          │
│     (cần domain + SSL cert Let's Encrypt)                        │
│                                                                   │
│  [Vercel] (Zalo Mini App - React)                               │
│     https://your-zalo-app.vercel.app                            │
│     VITE_API_BASE_URL=https://api.yourdomain.com/api/citizen    │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📋 PHASE 0: CHUẨN BỊ MÔI TRƯỜNG

### 0.1 Yêu cầu tối thiểu trên máy chủ LAN

```bash
# Kiểm tra và cài đặt nếu chưa có
docker --version          # >= 24.x
docker compose version    # >= 2.x (plugin, không phải docker-compose v1)
node --version            # >= 18.x (để build frontend)
git --version

# Cấu trúc thư mục dự án sau khi clone
project-root/
├── backend/
├── client/               # React - Zalo Mini App
├── AdminStaff/           # Vue - LAN Admin/Staff
├── nginx/
│   ├── nginx.conf
│   ├── ssl/              # Tạo mới thư mục này
│   └── logs/
└── docker-compose.prod.yml
```

### 0.2 Tạo file .env gốc (project root)

```bash
# File: .env (KHÔNG commit vào git)
# Sao chép từ .env.example rồi điền giá trị thực

# --- DATABASE ---
POSTGRES_DB=govservice_db
POSTGRES_USER=govservice_user
POSTGRES_PASSWORD=CHANGE_ME_STRONG_PASSWORD_HERE
POSTGRES_PORT=5433

# --- BACKEND ---
BACKEND_PORT=8081
JWT_SECRET=CHANGE_ME_64_CHAR_HEX_STRING
JWT_EXPIRATION_MS=86400000

# --- CORS - điền đúng origin Vercel sau khi deploy ---
CORS_ALLOWED_ORIGINS=http://10.191.167.188,http://localhost:5173,https://your-zalo-app.vercel.app

# --- CLOUDFLARE TUNNEL (điền sau) ---
CF_TUNNEL_TOKEN=
```

---

## 📋 PHASE 1: FIX CODE TRƯỚC KHI BUILD

### 1.1 Fix backend/src/main/resources/application.properties

**Vấn đề:** Hardcoded values. **Fix:** Đọc từ environment variables.

```properties
# application.properties - THAY TOÀN BỘ phần liên quan bằng:

spring.datasource.url=jdbc:postgresql://postgres:5433/${POSTGRES_DB:govservice_db}
spring.datasource.username=${POSTGRES_USER:govservice_user}
spring.datasource.password=${POSTGRES_PASSWORD}

app.jwt.secret=${JWT_SECRET}
app.jwt.expiration-ms=${JWT_EXPIRATION_MS:86400000}

# CORS: đọc từ env, phân cách bằng dấu phẩy
app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS}
```

### 1.2 Fix backend/src/main/java/.../config/CorsConfig.java

**Vấn đề:** Hardcoded LAN IP `10.191.167.188`. **Fix:**

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // Đọc từ application.properties -> đọc từ env
    @Value("${app.cors.allowed-origins}")
    private String allowedOriginsRaw;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOriginsRaw.split(",");

        registry.addMapping("/api/citizen/**")
            .allowedOrigins(origins)          // Citizen: cả LAN lẫn Vercel
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);

        // Staff/Admin: chỉ LAN - lọc ra origin LAN
        String[] lanOrigins = Arrays.stream(origins)
            .filter(o -> o.startsWith("http://10.") || o.startsWith("http://192.168."))
            .toArray(String[]::new);

        registry.addMapping("/api/staff/**")
            .allowedOrigins(lanOrigins)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);

        registry.addMapping("/api/admin/**")
            .allowedOrigins(lanOrigins)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

### 1.3 Fix client/src/services/citizenApi.ts

**Vấn đề:** Hardcoded `http://localhost:8081/api/citizen`. **Fix:**

```typescript
// client/src/services/citizenApi.ts

// Đọc từ Vite env variable - sẽ được inject lúc build
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/citizen';

// Thêm wrapper an toàn - kiểm tra content-type trước khi parse JSON
async function safeFetch(url: string, options?: RequestInit) {
  const res = await fetch(url, options);
  const contentType = res.headers.get('content-type') ?? '';

  if (!contentType.includes('application/json')) {
    // Server trả về HTML hoặc empty - throw rõ ràng thay vì parse error
    throw new Error(`API error ${res.status}: unexpected response format`);
  }

  const data = await res.json();

  if (!data.success) {
    throw new Error(data.message ?? data.code ?? 'API error');
  }

  return data;
}

export { API_BASE_URL, safeFetch };
```

### 1.4 Fix AdminStaff/src/services/api.ts

**Vấn đề:** Hardcoded IP. **Fix:**

```typescript
// AdminStaff/src/services/api.ts

// Trong môi trường LAN, Vite dev proxy hoặc Nginx xử lý routing
// Production: gọi qua Nginx (cùng origin), không cần base URL tuyệt đối
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

export default API_BASE_URL;
```

### 1.5 Fix client/.env.production (Zalo Mini App)

```bash
# File: client/.env.production
# URL này sẽ được thay bằng domain public (Cloudflare Tunnel hoặc VPS)
VITE_API_BASE_URL=https://api.yourdomain.com/api/citizen
```

### 1.6 Fix AdminStaff/.env.production (LAN)

```bash
# File: AdminStaff/.env.production
# Production LAN: gọi qua Nginx cùng host, không cần full URL
VITE_API_BASE_URL=/api
```

### 1.7 Fix AdminStaff/vite.config.ts

```typescript
// AdminStaff/vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  base: '/admin/',  // QUAN TRỌNG: match với Nginx location /admin/
  build: {
    outDir: 'dist',
    emptyOutDir: true,
  },
  server: {
    proxy: {
      '/api': {
        // Dev mode: proxy tới backend. Dùng biến env để không hardcode IP
        target: process.env.VITE_DEV_BACKEND ?? 'http://127.0.0.1:8081',
        changeOrigin: true,
      }
    }
  }
})
```

### 1.8 Fix client/vite.config.ts

```typescript
// client/vite.config.ts
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  base: '/',
  build: {
    outDir: 'dist',
    emptyOutDir: true,
  },
  server: {
    proxy: {
      '/api': {
        target: process.env.VITE_DEV_BACKEND ?? 'http://127.0.0.1:8081',
        changeOrigin: true,
      }
    }
  }
})
```

---

## 📋 PHASE 2: BUILD FRONTEND

### 2.1 Build AdminStaff (Vue - LAN)

```bash
cd AdminStaff
npm install

# Build với env production LAN
npm run build
# Output: AdminStaff/dist/   <-- đây là thư mục sẽ mount vào Docker

# Kiểm tra nhanh
ls -la dist/
# Phải thấy: index.html, assets/
```

### 2.2 Build client (React - Zalo Mini App)

```bash
cd client
npm install

# Build với env production (trỏ tới public API)
npm run build
# Output: client/dist/

ls -la dist/
```

---

## 📋 PHASE 3: CẤU HÌNH NGINX

### 3.1 Tạo SSL tự ký cho LAN (nếu chưa có domain)

```bash
# Chạy trên máy chủ LAN
mkdir -p nginx/ssl

# Tự ký cho LAN (dùng cho HTTPS nội bộ)
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout nginx/ssl/lan.key \
  -out nginx/ssl/lan.crt \
  -subj "/CN=10.191.167.188/O=GovService/C=VN"
```

### 3.2 Viết lại hoàn toàn nginx/nginx.conf

```nginx
# nginx/nginx.conf

worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    # Log format chuẩn
    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent"';

    access_log /var/log/nginx/access.log main;
    sendfile on;
    keepalive_timeout 65;
    gzip on;
    gzip_types text/plain text/css application/json application/javascript;

    # ----------------------------------------------------------------
    # SERVER 1: HTTP -> HTTPS redirect (LAN)
    # ----------------------------------------------------------------
    server {
        listen 80;
        server_name _;

        # Health check endpoint - KHÔNG redirect (dùng cho Docker healthcheck)
        location /health {
            return 200 'ok';
            add_header Content-Type text/plain;
        }

        # Mọi request khác redirect HTTPS
        location / {
            return 301 https://$host$request_uri;
        }
    }

    # ----------------------------------------------------------------
    # SERVER 2: HTTPS LAN (Admin + Staff + API proxy)
    # ----------------------------------------------------------------
    server {
        listen 443 ssl;
        server_name _;

        ssl_certificate     /etc/nginx/ssl/lan.crt;
        ssl_certificate_key /etc/nginx/ssl/lan.key;
        ssl_protocols       TLSv1.2 TLSv1.3;
        ssl_ciphers         HIGH:!aNULL:!MD5;

        # ---- Admin/Staff Vue App ----
        # Trỏ tới thư mục build của AdminStaff
        location /admin/ {
            alias /usr/share/nginx/html/admin/;
            try_files $uri $uri/ /admin/index.html;
            
            # Cache assets lâu, không cache HTML
            location ~* \.(js|css|png|jpg|ico|svg|woff2)$ {
                expires 1y;
                add_header Cache-Control "public, immutable";
            }
        }

        # ---- API Proxy -> Spring Boot ----
        # Citizen API: accessible cả LAN và từ Cloudflare Tunnel
        location /api/citizen/ {
            proxy_pass         http://backend:8081/api/citizen/;
            proxy_set_header   Host $host;
            proxy_set_header   X-Real-IP $remote_addr;
            proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header   X-Forwarded-Proto $scheme;
            proxy_read_timeout 60s;
            
            # CORS headers cho Zalo Mini App (Vercel origin)
            # Để Spring Boot xử lý CORS, không set ở đây để tránh duplicate
        }

        # Staff/Admin API: chỉ LAN
        location /api/staff/ {
            # Chặn request không từ LAN
            allow 10.0.0.0/8;
            allow 192.168.0.0/16;
            allow 172.16.0.0/12;
            allow 127.0.0.1;
            deny all;

            proxy_pass         http://backend:8081/api/staff/;
            proxy_set_header   Host $host;
            proxy_set_header   X-Real-IP $remote_addr;
            proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header   X-Forwarded-Proto $scheme;
        }

        location /api/admin/ {
            allow 10.0.0.0/8;
            allow 192.168.0.0/16;
            allow 172.16.0.0/12;
            allow 127.0.0.1;
            deny all;

            proxy_pass         http://backend:8081/api/admin/;
            proxy_set_header   Host $host;
            proxy_set_header   X-Real-IP $remote_addr;
            proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header   X-Forwarded-Proto $scheme;
        }

        # Root: redirect tới /admin/
        location = / {
            return 302 /admin/;
        }

        # Trang lỗi
        error_page 403 /403.html;
        error_page 404 /404.html;
        error_page 500 502 503 504 /50x.html;
        location ~ /[0-9]+\.html {
            root /usr/share/nginx/html;
            internal;
        }
    }
}
```

---

## 📋 PHASE 4: DOCKER COMPOSE

### 4.1 Viết lại docker-compose.prod.yml

```yaml
# docker-compose.prod.yml

version: '3.9'

services:

  # ── PostgreSQL ────────────────────────────────────────────────────
  postgres:
    image: postgres:15-alpine
    container_name: govservice_postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB:       ${POSTGRES_DB}
      POSTGRES_USER:     ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      # Mount init scripts nếu cần
      # - ./backend/src/main/resources/db/migration:/docker-entrypoint-initdb.d
    ports:
      # CHỈ bind localhost - không expose ra LAN
      - "127.0.0.1:${POSTGRES_PORT:-5433}:5432"
    networks:
      - govservice_net
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}"]
      interval: 10s
      timeout: 5s
      retries: 5

  # ── Spring Boot Backend ───────────────────────────────────────────
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: govservice_backend
    restart: unless-stopped
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL:      jdbc:postgresql://postgres:5432/${POSTGRES_DB}
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD}
      JWT_SECRET:                 ${JWT_SECRET}
      JWT_EXPIRATION_MS:          ${JWT_EXPIRATION_MS:-86400000}
      CORS_ALLOWED_ORIGINS:       ${CORS_ALLOWED_ORIGINS}
      SPRING_PROFILES_ACTIVE:     prod
    # KHÔNG expose port ra ngoài Docker - Nginx proxy thông qua Docker network
    expose:
      - "8081"
    networks:
      - govservice_net
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:8081/actuator/health || exit 1"]
      interval: 15s
      timeout: 10s
      retries: 5

  # ── Nginx ──────────────────────────────────────────────────────────
  nginx:
    image: nginx:1.25-alpine
    container_name: govservice_nginx
    restart: unless-stopped
    depends_on:
      backend:
        condition: service_healthy
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/ssl:/etc/nginx/ssl:ro
      - ./nginx/logs:/var/log/nginx
      # !! QUAN TRỌNG: Mount thư mục build của AdminStaff
      - ./AdminStaff/dist:/usr/share/nginx/html/admin:ro
      # Optional: mount Citizen dist nếu muốn serve local
      # - ./client/dist:/usr/share/nginx/html/client:ro
    networks:
      - govservice_net
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost/health || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 3

  # ── Cloudflare Tunnel (Public API cho Zalo Mini App) ───────────────
  # Chỉ bật khi đã có CF_TUNNEL_TOKEN
  cloudflared:
    image: cloudflare/cloudflared:latest
    container_name: govservice_tunnel
    restart: unless-stopped
    command: tunnel --no-autoupdate run
    environment:
      TUNNEL_TOKEN: ${CF_TUNNEL_TOKEN}
    depends_on:
      - nginx
    networks:
      - govservice_net
    profiles:
      # Chỉ start khi chạy: docker compose --profile tunnel up
      - tunnel

networks:
  govservice_net:
    driver: bridge

volumes:
  postgres_data:
    driver: local
```

### 4.2 Tạo backend/Dockerfile (nếu chưa có)

```dockerfile
# backend/Dockerfile

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
# Cache dependencies trước
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: chạy bằng non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
```

---

## 📋 PHASE 5: EXPOSE PUBLIC API - CLOUDFLARE TUNNEL

### Tại sao dùng Cloudflare Tunnel thay vì port forwarding?

| Tiêu chí | Port Forwarding | Cloudflare Tunnel |
|---|---|---|
| Cần public IP tĩnh | ✅ Có | ❌ Không cần |
| Cần mở firewall router | ✅ Có | ❌ Không cần |
| SSL/HTTPS tự động | ❌ Tự làm | ✅ Tự động |
| Bảo vệ DDoS | ❌ Không | ✅ Có |
| Miễn phí | ✅ | ✅ |

### 5.1 Thiết lập Cloudflare Tunnel

```bash
# 1. Đăng ký tài khoản Cloudflare (miễn phí)
#    https://dash.cloudflare.com

# 2. Thêm domain của bạn vào Cloudflare (hoặc dùng subdomain miễn phí .trycloudflare.com)

# 3. Tạo tunnel:
#    Cloudflare Dashboard -> Zero Trust -> Networks -> Tunnels
#    -> Create a tunnel -> Cloudflared -> đặt tên: "govservice-api"

# 4. Copy tunnel token (bắt đầu bằng eyJ...)
# 5. Thêm vào file .env:
#    CF_TUNNEL_TOKEN=eyJ...

# 6. Cấu hình Public Hostname trong Tunnel:
#    Subdomain: api
#    Domain: yourdomain.com  (hoặc để trống nếu dùng .trycloudflare.com)
#    Service: http://nginx:80
#    Path: /api/citizen/   <- CHỈ expose citizen API
```

### 5.2 Cấu hình Cloudflare để chỉ expose /api/citizen

Trong Cloudflare Tunnel > Access > Policies, thêm rule:
- Path: `/api/staff/*` → Block
- Path: `/api/admin/*` → Block
- Path: `/api/citizen/*` → Allow (everyone)

Hoặc cấu hình trong `~/.cloudflared/config.yml`:

```yaml
# Thêm vào Docker volume nếu muốn config file thay vì token
tunnel: YOUR_TUNNEL_ID
credentials-file: /etc/cloudflared/credentials.json

ingress:
  # Chỉ cho phép /api/citizen
  - hostname: api.yourdomain.com
    path: /api/citizen/
    service: http://nginx:80
  # Từ chối tất cả còn lại
  - service: http_status:404
```

---

## 📋 PHASE 6: DEPLOY ZALO MINI APP LÊN VERCEL

### 6.1 Chuẩn bị Vercel

```bash
# Cài Vercel CLI
npm install -g vercel

# Đăng nhập
vercel login
```

### 6.2 Tạo vercel.json trong thư mục client/

```json
{
  "version": 2,
  "name": "govservice-zalo-miniapp",
  "builds": [
    {
      "src": "package.json",
      "use": "@vercel/static-build",
      "config": { "distDir": "dist" }
    }
  ],
  "routes": [
    {
      "src": "/(.*)",
      "dest": "/index.html"
    }
  ],
  "env": {
    "VITE_API_BASE_URL": "@govservice_api_url"
  }
}
```

### 6.3 Thiết lập Environment Variables trên Vercel Dashboard

```
Vercel Dashboard -> Project -> Settings -> Environment Variables

Thêm:
  Name:  VITE_API_BASE_URL
  Value: https://api.yourdomain.com/api/citizen
  Environments: Production, Preview
```

### 6.4 Deploy lên Vercel

```bash
cd client

# Deploy lần đầu (interactive)
vercel --prod

# Lấy URL sau deploy: https://govservice-zalo-miniapp.vercel.app
# Cập nhật .env ở máy chủ LAN:
#   CORS_ALLOWED_ORIGINS=...,https://govservice-zalo-miniapp.vercel.app
```

### 6.5 Cập nhật CORS sau khi có Vercel URL

```bash
# Trên máy chủ LAN, cập nhật .env
CORS_ALLOWED_ORIGINS=http://10.191.167.188,https://govservice-zalo-miniapp.vercel.app

# Restart backend container để nhận CORS mới
docker compose -f docker-compose.prod.yml restart backend
```

### 6.6 Đăng ký Zalo Mini App (Zalo Developer Portal)

```
1. Truy cập: https://developers.zalo.me
2. Tạo Mini App mới
3. Cấu hình:
   - App URL: https://govservice-zalo-miniapp.vercel.app
   - Whitelist domain: api.yourdomain.com
4. Lấy App ID -> cập nhật vào client/.env.production:
   VITE_ZALO_APP_ID=YOUR_ZALO_APP_ID
```

---

## 📋 PHASE 7: SCRIPT CHẠY TOÀN HỆ THỐNG

### 7.1 deploy.sh - Script deploy đầy đủ

```bash
#!/bin/bash
# deploy.sh - Chạy ở project root
set -euo pipefail

echo "=== GovService Deployment Script ==="

# ---- Bước 1: Kiểm tra file .env ----
if [ ! -f .env ]; then
  echo "❌ Thiếu file .env. Tạo từ .env.example:"
  cp .env.example .env
  echo "✏️  Hãy điền giá trị vào .env rồi chạy lại"
  exit 1
fi
source .env

# ---- Bước 2: Build AdminStaff (Vue) ----
echo "📦 Building AdminStaff..."
cd AdminStaff
npm ci --silent
npm run build
cd ..
echo "✅ AdminStaff built -> AdminStaff/dist/"

# ---- Bước 3: Build Client (React - cho preview local) ----
echo "📦 Building Client..."
cd client
npm ci --silent
npm run build
cd ..
echo "✅ Client built -> client/dist/"

# ---- Bước 4: Tạo SSL nếu chưa có ----
if [ ! -f nginx/ssl/lan.crt ]; then
  echo "🔐 Tạo SSL certificate cho LAN..."
  mkdir -p nginx/ssl
  openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout nginx/ssl/lan.key \
    -out nginx/ssl/lan.crt \
    -subj "/CN=LAN/O=GovService/C=VN" 2>/dev/null
  echo "✅ SSL certificate created"
fi

# ---- Bước 5: Tạo thư mục logs ----
mkdir -p nginx/logs

# ---- Bước 6: Pull images và build ----
echo "🐳 Building Docker images..."
docker compose -f docker-compose.prod.yml build --no-cache backend

# ---- Bước 7: Start services ----
echo "🚀 Starting services..."
docker compose -f docker-compose.prod.yml up -d postgres

echo "⏳ Waiting for PostgreSQL..."
sleep 10

docker compose -f docker-compose.prod.yml up -d backend nginx

# ---- Bước 8: Health checks ----
echo "🔍 Health checks..."
sleep 5

# Check backend
BACKEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8081/actuator/health 2>/dev/null || echo "000")
if [ "$BACKEND_STATUS" = "200" ]; then
  echo "✅ Backend: OK"
else
  echo "❌ Backend: FAILED (HTTP $BACKEND_STATUS)"
  docker compose -f docker-compose.prod.yml logs backend --tail=50
  exit 1
fi

# Check Nginx
NGINX_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1/health 2>/dev/null || echo "000")
if [ "$NGINX_STATUS" = "200" ]; then
  echo "✅ Nginx: OK"
else
  echo "❌ Nginx: FAILED (HTTP $NGINX_STATUS)"
  docker compose -f docker-compose.prod.yml logs nginx --tail=20
  exit 1
fi

# Check API endpoint
API_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1/api/citizen/specialties 2>/dev/null || echo "000")
echo "📡 API /api/citizen/specialties: HTTP $API_STATUS"

# ---- Bước 9: Bật Tunnel nếu có token ----
if [ -n "${CF_TUNNEL_TOKEN:-}" ]; then
  echo "🌐 Starting Cloudflare Tunnel..."
  docker compose -f docker-compose.prod.yml --profile tunnel up -d cloudflared
  echo "✅ Tunnel started"
else
  echo "ℹ️  Skipping Cloudflare Tunnel (CF_TUNNEL_TOKEN not set)"
fi

echo ""
echo "=== DEPLOYMENT COMPLETE ==="
echo "Admin/Staff UI: https://$(hostname -I | awk '{print $1}')/admin/"
echo "API Health:     http://$(hostname -I | awk '{print $1}')/api/citizen/specialties"
echo ""
```

### 7.2 update.sh - Script cập nhật khi có thay đổi code

```bash
#!/bin/bash
# update.sh - Cập nhật không downtime

set -euo pipefail
source .env

COMPONENT=${1:-all}  # Tham số: backend | admin | all

if [ "$COMPONENT" = "admin" ] || [ "$COMPONENT" = "all" ]; then
  echo "📦 Rebuilding AdminStaff..."
  cd AdminStaff && npm ci --silent && npm run build && cd ..
  # Nginx tự phục vụ file mới (không cần restart)
  docker compose -f docker-compose.prod.yml exec nginx nginx -s reload
  echo "✅ AdminStaff updated"
fi

if [ "$COMPONENT" = "backend" ] || [ "$COMPONENT" = "all" ]; then
  echo "📦 Rebuilding Backend..."
  docker compose -f docker-compose.prod.yml build --no-cache backend
  # Rolling restart: build xong rồi mới stop
  docker compose -f docker-compose.prod.yml up -d --no-deps backend
  echo "✅ Backend updated"
fi
```

---

## 📋 PHASE 8: CHECKLIST KIỂM TRA SAU DEPLOY

### 8.1 LAN Checks

```bash
# Chạy từ máy chủ
curl -k https://127.0.0.1/health                                    # → ok
curl -k https://127.0.0.1/admin/                                    # → HTML Vue app
curl http://127.0.0.1:8081/api/citizen/specialties                  # → JSON {success:true}
curl -k https://127.0.0.1/api/citizen/specialties                   # → JSON {success:true}
curl -k https://127.0.0.1/api/staff/hoso  # Từ LAN: 200 hoặc 401   # → Không phải 403
curl -k https://10.191.167.219/api/staff/hoso  # Từ máy khác LAN   # → 200 hoặc 401

# Kiểm tra Admin UI từ máy khác trong LAN
# Mở trình duyệt: https://10.191.167.188/admin/
```

### 8.2 Public API Checks (sau khi có Cloudflare Tunnel)

```bash
# Từ bất kỳ đâu trên internet
curl https://api.yourdomain.com/api/citizen/specialties             # → JSON
curl https://api.yourdomain.com/api/staff/hoso                      # → 404 (blocked)

# CORS check từ Vercel origin
curl -H "Origin: https://govservice-zalo-miniapp.vercel.app" \
     -H "Access-Control-Request-Method: POST" \
     -X OPTIONS \
     https://api.yourdomain.com/api/citizen/appointments            # → 200 + CORS headers
```

### 8.3 Zalo Mini App Checks

```bash
# Từ Vercel preview URL
curl https://govservice-zalo-miniapp.vercel.app                     # → HTML React app
```

---

## 📋 PHASE 9: MONITORING & VẬN HÀNH

### 9.1 Xem logs

```bash
# Tất cả services
docker compose -f docker-compose.prod.yml logs -f

# Chỉ backend
docker compose -f docker-compose.prod.yml logs -f backend

# Nginx access log
tail -f nginx/logs/access.log

# Nginx error log
tail -f nginx/logs/error.log
```

### 9.2 Restart khi cần

```bash
# Restart toàn bộ
docker compose -f docker-compose.prod.yml restart

# Restart chỉ backend
docker compose -f docker-compose.prod.yml restart backend

# Stop toàn bộ (giữ data)
docker compose -f docker-compose.prod.yml stop

# Stop + xóa container (giữ volume data)
docker compose -f docker-compose.prod.yml down
```

### 9.3 Backup database

```bash
# Backup thủ công
docker compose -f docker-compose.prod.yml exec postgres \
  pg_dump -U $POSTGRES_USER $POSTGRES_DB > backup_$(date +%Y%m%d_%H%M%S).sql

# Restore
docker compose -f docker-compose.prod.yml exec -T postgres \
  psql -U $POSTGRES_USER $POSTGRES_DB < backup_20260101_120000.sql
```

---

## 🗂️ TÓM TẮT LUỒNG DEPLOY THEO THỨ TỰ

```
Bước 1: Fix code (Phase 1)
  └─ Xóa hardcode IP, dùng env variables

Bước 2: Build frontends (Phase 2)
  ├─ AdminStaff/dist/  ← dùng cho Nginx LAN
  └─ client/dist/      ← deploy lên Vercel

Bước 3: Cấu hình Nginx (Phase 3)
  └─ nginx/nginx.conf hoàn chỉnh

Bước 4: Cấu hình Docker Compose (Phase 4)
  └─ docker-compose.prod.yml mount đúng volumes

Bước 5: Chạy deploy.sh (Phase 7)
  └─ Tự động build + start + health check

Bước 6: Thiết lập Cloudflare Tunnel (Phase 5)
  └─ Public API cho Zalo Mini App

Bước 7: Deploy Vercel (Phase 6)
  ├─ vercel --prod trong client/
  └─ Set VITE_API_BASE_URL = CF Tunnel URL

Bước 8: Cập nhật CORS (Phase 6.5)
  └─ Thêm Vercel URL vào CORS_ALLOWED_ORIGINS + restart backend

Bước 9: Đăng ký Zalo Mini App (Phase 6.6)
  └─ Whitelist domain Cloudflare Tunnel trên Zalo Developer Portal

Bước 10: Verify tất cả (Phase 8)
```

---

## ⚠️ CÁC LỖI THƯỜNG GẶP VÀ CÁCH XỬ LÝ

| Lỗi | Nguyên nhân | Cách fix |
|---|---|---|
| `GET /admin/` → 403 | `AdminStaff/dist/` rỗng hoặc không mount | Chạy `npm run build` trong AdminStaff/, kiểm tra volume trong compose |
| `GET /` → redirect loop | Nginx redirect 80→443→80 | Kiểm tra `nginx.conf`, đảm bảo server 443 không có redirect tiếp |
| JSON parse error ở citizen | `await response.json()` khi server trả HTML | Áp dụng `safeFetch` wrapper ở Phase 1.3 |
| CORS blocked từ Vercel | Vercel URL chưa trong allowed origins | Cập nhật `.env` → `CORS_ALLOWED_ORIGINS` → restart backend |
| Backend 503 | PostgreSQL chưa healthy | Đợi thêm hoặc kiểm tra `docker compose logs postgres` |
| Cloudflare Tunnel disconnected | Token hết hạn hoặc sai | Vào CF Dashboard tạo token mới |
| Rate limit 429 trên citizen API | `RateLimitInterceptor` kick in | Giảm tần suất gọi API, hoặc tăng limit trong config |
