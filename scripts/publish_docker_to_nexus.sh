#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname -- "$SCRIPT_DIR")"
PUSH_REGISTRY="${NEXUS_DOCKER_PUSH_REGISTRY:-localhost:8082}"
PULL_REGISTRY="${NEXUS_DOCKER_PULL_REGISTRY:-localhost:8083}"
IMAGE_TAG="${IMAGE_TAG:-$(git -C "$PROJECT_ROOT" rev-parse --short HEAD)}"
# Optional second tag: the release version shared with the Maven artifacts (e.g. 1.0.42)
VERSION="${VERSION:-}"

if [[ -z "${NEXUS_USERNAME:-}" || -z "${NEXUS_PASSWORD:-}" ]]; then
    printf 'NEXUS_USERNAME and NEXUS_PASSWORD must be set.\n' >&2
    exit 1
fi

cleanup() {
    docker logout "$PUSH_REGISTRY" >/dev/null 2>&1 || true
}
trap cleanup EXIT

SERVICES=(
    discovery-service
    api-gateway
    user-service
    product-service
    media-service
    order-service
    angular-app
)

cd "$PROJECT_ROOT"

printf '%s' "$NEXUS_PASSWORD" | docker login "$PUSH_REGISTRY" \
    --username "$NEXUS_USERNAME" --password-stdin

docker compose -f docker-compose.yaml build "${SERVICES[@]}"

for SERVICE in "${SERVICES[@]}"; do
    SOURCE_IMAGE="$PULL_REGISTRY/cartify/$SERVICE:$IMAGE_TAG"
    TARGET_IMAGE="$PUSH_REGISTRY/cartify/$SERVICE:$IMAGE_TAG"
    docker image tag "$SOURCE_IMAGE" "$TARGET_IMAGE"
    docker image push "$TARGET_IMAGE"

    if [[ -n "$VERSION" ]]; then
        VERSION_IMAGE="$PUSH_REGISTRY/cartify/$SERVICE:$VERSION"
        docker image tag "$SOURCE_IMAGE" "$VERSION_IMAGE"
        docker image push "$VERSION_IMAGE"
    fi
done

printf 'Published application images with tag(s) %s%s to %s.\n' "$IMAGE_TAG" "${VERSION:+ and $VERSION}" "$PUSH_REGISTRY"