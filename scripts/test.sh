#!/bin/bash

# Stop the script if any command fails
set -e

# Define the colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🔨 Starting Multi-Service test Process...${NC}\n"

# Array of service directories
SERVICES=(
    "api-gateway"
    "discovery-service"
    "media-service"
    "product-service"
    "user-service"
    "order-service"
)

# Loop through each service and build
for SERVICE in "${SERVICES[@]}"; do
    if [ -d "$SERVICE" ]; then
        echo -e "${BLUE}📦 testing Service:${NC} $SERVICE"
        
        # Move into directory, build, and come back automatically
        (
            cd "$SERVICE"
            # Ensure Maven Wrapper is executable
            chmod +x mvnw
            # Clean and Package
            ./mvnw test
        )
        
        echo -e "${GREEN}✅ Successfully tested $SERVICE${NC}\n"
    else
        echo -e "${RED}❌ Directory $SERVICE not found. Skipping...${NC}\n"
    fi
done

echo -e "${GREEN}🚀 All services tested successfully!${NC}"