#!/bin/bash

echo "Starting Docker Compose services..."
docker compose up -d

# Wait until the postgres-primary service is reported as 'healthy' by Docker Compose
echo "Waiting for PostgreSQL database (postgres-primary) to become healthy..."
MAX_ATTEMPTS=15 # Increased max attempts for robustness (15 * 10s = 2.5 minutes)
ATTEMPT=0
while [ "$(docker compose inspect -f '{{.State.Health.Status}}' postgres-primary 2>/dev/null)" != "healthy" ]; do
  if [ $ATTEMPT -ge $MAX_ATTEMPTS ]; then
    echo "Error: PostgreSQL service did not become healthy within the expected time."
    docker compose logs postgres-primary # Show logs for debugging
    exit 1
  fi
  echo "PostgreSQL not yet healthy. Waiting 10 seconds..."
  sleep 10
  ATTEMPT=$((ATTEMPT+1))
done
echo "PostgreSQL (postgres-primary) is healthy!"

# Wait for Consul to be ready on localhost:8500
echo "Waiting for Consul to be ready..."
./wait-for-it.sh localhost:8500 -t 60 -- echo "Consul is up and running!"

# Now, run your Spring Boot application
echo "Starting Spring Boot application..."
cd jlr-user-service
mvn spring-boot:run