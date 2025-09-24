# War Backend

Backend do jogo War desenvolvido em Spring Boot com PostgreSQL.

## Pré-requisitos

- Docker
- Docker Compose

## Como rodar o projeto

### 1. Primeira execução

```bash
# Clone o repositório
git clone https://github.com/ES2-War-Game/war-backend.git
cd war-backend

# Suba os containers
docker compose up -d
```

### 2. Execuções subsequentes

```bash
# Para iniciar os containers existentes
docker compose up -d

# Para parar os containers
docker compose down
```

### 3. Visualizar logs

```bash
# Ver logs de todos os serviços
docker compose logs

# Ver logs apenas da aplicação
docker compose logs app

# Ver logs apenas do banco de dados
docker compose logs db

# Seguir logs em tempo real
docker compose logs -f
```

## Atualizando código Java

**⚠️ IMPORTANTE:** Quando você fizer alterações no código Java, o Docker não irá automaticamente reconstruir a imagem. Isso acontece porque o Docker usa um sistema de cache de camadas para otimizar os builds.

### Por que isso acontece?

O Docker cria camadas (layers) para cada instrução no Dockerfile. Quando você executa `docker compose up`, ele reutiliza as camadas existentes se não detectar mudanças nos arquivos que foram copiados para aquela camada específica. Como o código Java é copiado e compilado dentro do container, as mudanças locais não são automaticamente detectadas.

### Como atualizar após mudanças no código:

```bash
# 1. Pare os containers
docker compose down

# 2. Reconstrua a imagem sem usar cache
docker compose build --no-cache

# 3. Suba os containers novamente
docker compose up -d
```

### Alternativa mais rápida (recomendada para desenvolvimento):

```bash
# Reconstrói apenas se houver mudanças e sobe os containers
docker compose up --build -d
```

### Script para desenvolvimento

Você pode criar um script para automatizar o processo de atualização:

```bash
#!/bin/bash
echo "Parando containers..."
docker compose down

echo "Reconstruindo imagem..."
docker compose build --no-cache

echo "Subindo containers..."
docker compose up -d

echo "Aplicação rodando em http://localhost:8080"
```

## Conectando ao banco de dados

A aplicação usa PostgreSQL rodando em container. Você pode conectar usando qualquer cliente de banco:

**Configurações de conexão:**
- **Host:** localhost
- **Porta:** 5432
- **Database:** wargame
- **Username:** wargame  
- **Password:** wargame

### Clientes recomendados:
- DBeaver
- pgAdmin
- DataGrip
- VSCode com extensão PostgreSQL

## Estrutura do projeto

```
war-backend/
├── src/
│   ├── main/
│   │   ├── java/com/war/game/war_backend/
│   │   │   ├── config/          # Configurações (CORS, Segurança, Inicializadores)
│   │   │   ├── controller/      # Controllers REST
│   │   │   ├── model/          # Entidades JPA
│   │   │   ├── repository/     # Repositórios JPA
│   │   │   ├── security/       # Configurações de segurança e JWT
│   │   │   └── services/       # Lógica de negócio
│   │   └── resources/
│   │       ├── application.properties
│   │       └── data.sql
│   └── test/
├── docker-compose.yml          # Configuração dos containers
├── Dockerfile                  # Imagem da aplicação
└── db/
    └── init.sql               # Script de inicialização do banco
```

## Endpoints principais

- `POST /auth/register` - Registrar novo usuário
- `POST /auth/login` - Fazer login
- `GET /players` - Listar jogadores (requer autenticação)

## Desenvolvimento

### Logs úteis

```bash
# Ver logs da aplicação Spring Boot
docker compose logs app -f

# Ver logs do banco de dados
docker compose logs db -f

# Verificar status dos containers
docker compose ps

# Verificar recursos utilizados
docker stats
```

### Limpeza completa (use com cuidado)

```bash
# Remove containers, redes, volumes e imagens
docker compose down --volumes --remove-orphans
docker system prune -a --volumes
```

**⚠️ Atenção:** O comando acima remove TODOS os dados do banco, imagens Docker não utilizadas e libera espaço em disco.

## Troubleshooting

### Problema: "Port already in use"
```bash
# Verifique qual processo está usando a porta 8080
sudo lsof -i :8080

# Ou mate o processo
sudo kill -9 $(sudo lsof -t -i:8080)
```

### Problema: Banco de dados não conecta
```bash
# Verifique se o container do banco está rodando
docker compose ps

# Verifique os logs do banco
docker compose logs db
```

### Problema: Aplicação não reflete mudanças de código
Siga os passos da seção "Atualizando código Java" acima.

## Tecnologias utilizadas

- **Spring Boot 3.5.5**
- **Spring Security 6**
- **Spring Data JPA**
- **PostgreSQL 15**
- **JWT para autenticação**
- **Docker & Docker Compose**
- **Maven**