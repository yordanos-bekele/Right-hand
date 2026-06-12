#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

echo "=== Starting Right Hand Local Environment ==="

# 1. Start Docker services (PostgreSQL, Redis, Kafka)
echo "--> Starting docker containers..."
sudo docker compose up -d

# 2. Build the project and run the API gateway
echo "--> Building and starting the Right Hand API module..."
./mvnw clean install -DskipTests
./mvnw spring-boot:run -pl right-hand-api
