#!/bin/bash

# Script para build e up com controle de testes e modo detached
# Uso: ./start.sh [--skip-tests|-skt] [-d|--detached]

SKIP_TESTS="false"
DETACHED=""

# Processa todos os argumentos
for arg in "$@"; do
    case $arg in
        --skip-tests|-skt)
            SKIP_TESTS="true"
            ;;
        -d|--detached)
            DETACHED="-d"
            ;;
    esac
done

# Mostra configuração
if [ "$SKIP_TESTS" = "true" ]; then
    echo "🚀 Building and starting WITHOUT tests..."
else
    echo "🧪 Building and starting WITH tests..."
fi

if [ "$DETACHED" = "-d" ]; then
    echo "🔧 Running in DETACHED mode (background)"
else
    echo "👀 Running in FOREGROUND mode (with logs)"
fi

# Para containers existentes
docker compose down

# Build e up
docker compose build --build-arg SKIP_TESTS=$SKIP_TESTS
docker compose up $DETACHED

if [ "$DETACHED" = "-d" ]; then
    echo "✅ Application started in background!"
    echo "🌐 Access: http://localhost:8080"
    echo "📋 Check logs: docker compose logs -f"
else
    echo "✅ Application stopped!"
fi