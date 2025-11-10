docker compose -f docker/docker-compose.yml up -d
Write-Host "Infra up. Postgres: localhost:5433, Kafka: localhost:19092, pgAdmin: http://localhost:5050"
