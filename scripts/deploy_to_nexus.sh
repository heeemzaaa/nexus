#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname -- "$SCRIPT_DIR")"
MAVEN_SETTINGS_ARGS=()
NEXUS_HOST="${NEXUS_HOST:-localhost}"

if [[ -n "${MAVEN_SETTINGS:-}" ]]; then
    if [[ "$MAVEN_SETTINGS" != /* ]]; then
        MAVEN_SETTINGS="$PWD/$MAVEN_SETTINGS"
    fi
    if [[ ! -r "$MAVEN_SETTINGS" ]]; then
        printf 'Maven settings file is not readable: %s\n' "$MAVEN_SETTINGS" >&2
        exit 1
    fi
    MAVEN_SETTINGS_ARGS=(-s "$MAVEN_SETTINGS")
fi

SERVICES=(
    "api-gateway"
    "discovery-service"
    "media-service"
    "product-service"
    "user-service"
    "order-service"
)

cd "$PROJECT_ROOT"

for SERVICE in "${SERVICES[@]}"; do
    if [[ ! -x "$SERVICE/mvnw" ]]; then
        printf 'Maven wrapper is missing or not executable: %s/mvnw\n' "$SERVICE" >&2
        exit 1
    fi

    printf 'Deploying %s to Nexus...\n' "$SERVICE"
    (
        cd "$SERVICE"
        ./mvnw "${MAVEN_SETTINGS_ARGS[@]}" deploy \
            -DskipTests \
            -DNEXUS_HOST="$NEXUS_HOST" \
            ${REVISION:+-Drevision="$REVISION"}
    )
done

printf 'All Maven artifacts were deployed to Nexus.\n'