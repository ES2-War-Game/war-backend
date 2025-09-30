#!/bin/bash

# Script para build com controle de testes
# Uso: ./build.sh [--skip-tests|-skt]

SKIP_TESTS="false"

# Verifica se foi passado argumento para pular testes
if [[ "$1" == "--skip-tests" || "$1" == "-skt" ]]; then
    SKIP_TESTS="true"
    echo "🚀 Building WITHOUT tests..."
else
    echo "🧪 Building WITH tests..."
fi

# Executa o build com o argumento apropriado
docker compose build --build-arg SKIP_TESTS=$SKIP_TESTS

echo "✅ Build completed!"