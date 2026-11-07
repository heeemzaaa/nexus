#!/bin/bash

set -e

SERVICES=(
    "api-gateway"
    "order-service"
    "user-service"
    "product-service"
    "media-service"
)

mkdir -p logs

for SERVICE in "${SERVICES[@]}"; do
    if [ -d "$SERVICE" ]; then
        (
            cd "$SERVICE"
            chmod +x mvnw
            echo "Starting $SERVICE..."
            ./mvnw spring-boot:run > "../logs/${SERVICE}.log" 2>&1
        ) &
    fi
done

echo "All services are starting..."
wait