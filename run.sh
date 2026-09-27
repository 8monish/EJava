#!/usr/bin/env bash
# ==============================================================================
# Aura Grid / Spring Boot Localhost Runner
# Usage:
#   ./run.sh         -> Compiles and runs with Maven (http://localhost:8080)
#   ./run.sh jar     -> Fast start with pre-built JAR (instant launch)
#   ./run.sh build   -> Clean package JAR and run
# ==============================================================================

set -e

# Always run from the project root
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

echo "=========================================================="
echo "⚡ Starting Spring Boot App on http://localhost:8080"
echo "=========================================================="

# 1. Ensure MariaDB database service is active
if ! systemctl is-active --quiet mariadb 2>/dev/null; then
  echo "📦 MariaDB is not active. Attempting to start..."
  sudo systemctl start mariadb 2>/dev/null || echo "⚠️ Please ensure MariaDB is running."
fi

# 2. Check and free port 8080 if already occupied by an old process
OCCUPIED_PID=$(ss -tulpn 2>/dev/null | grep ':8080 ' | awk -F'pid=' '{print $2}' | cut -d',' -f1 || true)
if [ -n "$OCCUPIED_PID" ]; then
  echo "🔄 Port 8080 is in use. Stopping old process (PID: $OCCUPIED_PID)..."
  kill -9 "$OCCUPIED_PID" 2>/dev/null || true
  sleep 1
fi

MODE="${1:-dev}"

case "$MODE" in
  jar)
    if [ ! -f "target/springbootapp-0.0.1-SNAPSHOT.jar" ]; then
      echo "📦 Target JAR not found. Building first..."
      ./mvnw clean package -DskipTests
    fi
    echo "🚀 Running target JAR..."
    echo "📍 Access your app at: http://localhost:8080"
    echo "💡 Press Ctrl+C to stop."
    echo "----------------------------------------------------------"
    exec java -jar target/springbootapp-0.0.1-SNAPSHOT.jar
    ;;

  build)
    echo "📦 Building fresh JAR package..."
    ./mvnw clean package -DskipTests
    echo "🚀 Starting application..."
    echo "📍 Access your app at: http://localhost:8080"
    echo "💡 Press Ctrl+C to stop."
    echo "----------------------------------------------------------"
    exec java -jar target/springbootapp-0.0.1-SNAPSHOT.jar
    ;;

  dev|*)
    echo "🚀 Launching via Maven spring-boot:run..."
    echo "📍 Access your app at: http://localhost:8080"
    echo "💡 Press Ctrl+C to stop."
    echo "----------------------------------------------------------"
    exec ./mvnw spring-boot:run
    ;;
esac
