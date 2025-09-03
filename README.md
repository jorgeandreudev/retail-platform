# Retail Platform (Portfolio Project)

This repository contains a **retail inventory management platform** built as a **microservices architecture** with Java 21 and Spring Boot 3.  
It is designed as a portfolio project to showcase best practices for modern backend development.

## Tech Stack
- **Java 21** (virtual threads, pattern matching, records where useful)
- **Spring Boot 3.5**
- **Hexagonal architecture (Ports & Adapters)**
- **API-first design** with OpenAPI 3
- **PostgreSQL** (via Docker Compose)
- **Kafka** (event-driven architecture, coming soon)
- **MongoDB** (for non-relational features, coming soon)
- **Testcontainers** for integration testing
- **GitHub Actions** for CI/CD
- **SonarCloud** for static analysis (to be added)

## Project Structure

retail-platform/
contracts/ # OpenAPI contracts for each microservice
products/
products-v1.yaml
common/
components.yaml
products-service/ # Microservice for product catalog
orders-service/ # Microservice for orders (WIP)
notifications-service/ # Microservice for notifications (WIP)
docker/ # Docker Compose for infra (Postgres, Kafka, Mongo, etc.)
.github/workflows/ # CI/CD pipelines

## ️ Getting Started

### Prerequisites
- Java 21
- Docker Desktop (for PostgreSQL, Kafka, MongoDB, etc.)
- Maven 3.9+

### Run services locally

bash
# Start infrastructure
docker compose -f docker/docker-compose.yml up -d

# Build all modules
mvn clean install

# Run products service
mvn -pl products-service spring-boot:run

# API Documentation

Once running, visit:

Swagger UI: http://localhost:8081/swagger-ui
OpenAPI spec: http://localhost:8081/openapi/products-v1.yaml

