#!/bin/bash
set -euo pipefail

echo "=== GovService Deployment Script ==="

if [ ! -f .env ]; then
  echo "Missing .env file"
  exit 1
fi

source .env

echo "Building AdminStaff..."
cd AdminStaff
npm ci --silent
npm run build
cd ..

echo "Building Client..."
cd client
npm ci --silent
npm run build
cd ..

if [ ! -f nginx/ssl/lan.crt ] || [ ! -f nginx/ssl/lan.key ]; then
  echo "Creating LAN SSL certificate..."
  mkdir -p nginx/ssl
  openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout nginx/ssl/lan.key \
    -out nginx/ssl/lan.crt \
    -subj "/CN=10.191.167.188/O=GovService/C=VN" >/dev/null 2>&1
fi

mkdir -p nginx/logs

echo "Building backend image..."
docker compose -f docker-compose.prod.yml build backend

echo "Starting core services..."
docker compose -f docker-compose.prod.yml up -d postgres backend nginx

echo "Waiting for services..."
sleep 8

BACKEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8081/actuator/health || true)
NGINX_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1/health || true)
ADMIN_STATUS=$(curl -k -s -o /dev/null -w "%{http_code}" https://127.0.0.1/admin/ || true)
API_STATUS=$(curl -k -s -o /dev/null -w "%{http_code}" https://127.0.0.1/api/citizen/specialties || true)

echo "Backend health : ${BACKEND_STATUS}"
echo "Nginx health   : ${NGINX_STATUS}"
echo "Admin UI       : ${ADMIN_STATUS}"
echo "Citizen API    : ${API_STATUS}"

if [ -n "${NGROK_AUTHTOKEN:-}" ] && [ -n "${NGROK_STATIC_DOMAIN:-}" ] && [ "${NGROK_STATIC_DOMAIN}" != "your-static-domain.ngrok-free.app" ]; then
  echo "Starting ngrok..."
  docker compose -f docker-compose.prod.yml up -d ngrok
  sleep 8

  if docker compose -f docker-compose.prod.yml logs ngrok 2>&1 | grep -q "started tunnel"; then
    echo "Ngrok tunnel   : ACTIVE"
    echo "Public API     : https://${NGROK_STATIC_DOMAIN}/api/citizen/"
    echo "Ngrok dashboard: http://127.0.0.1:4040"
  else
    echo "Ngrok tunnel   : NOT READY"
    docker compose -f docker-compose.prod.yml logs ngrok --tail=20
  fi
else
  echo "Ngrok tunnel   : SKIPPED (set NGROK_AUTHTOKEN and NGROK_STATIC_DOMAIN in .env)"
fi

echo ""
echo "=== DEPLOYMENT COMPLETE ==="
echo "Admin/Staff UI : https://$(hostname -I | awk '{print $1}')/admin/"
echo "Citizen API    : https://$(hostname -I | awk '{print $1}')/api/citizen/"
