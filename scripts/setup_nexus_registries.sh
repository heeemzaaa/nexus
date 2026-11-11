#!/usr/bin/env bash
# Installs jenkins/nexus-registries.conf into the user's Podman config so the
# local Nexus Docker registries (localhost:8082 / localhost:8083) are reached
# over plain HTTP. Run it on the host that serves the Podman socket used by Jenkins.

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname -- "$SCRIPT_DIR")"
SOURCE="$PROJECT_ROOT/jenkins/nexus-registries.conf"
TARGET_DIR="${XDG_CONFIG_HOME:-$HOME/.config}/containers/registries.conf.d"
TARGET="$TARGET_DIR/50-nexus.conf"

if [[ ! -r "$SOURCE" ]]; then
    printf 'Registry config not found: %s\n' "$SOURCE" >&2
    exit 1
fi

mkdir -p "$TARGET_DIR"
cp "$SOURCE" "$TARGET"
printf 'Installed %s\n' "$TARGET"
