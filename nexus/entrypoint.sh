#!/bin/sh
# Builds the Nexus secret-encryption key file from environment variables (.env),
# then starts Nexus. Runs as the "nexus" user.
set -e

if [ -n "$NEXUS_SECRET_KEY" ]; then
  KEY_FILE=/tmp/nexus-secrets.json
  umask 077
  cat > "$KEY_FILE" <<EOF
{
  "active": "${NEXUS_SECRET_KEY_ID:-default-key}",
  "keys": [
    { "id": "${NEXUS_SECRET_KEY_ID:-default-key}", "key": "${NEXUS_SECRET_KEY}" }
  ]
}
EOF
  export NEXUS_SECRETS_KEY_FILE="$KEY_FILE"
  # do not leak the raw key to the Nexus process environment
  unset NEXUS_SECRET_KEY
else
  echo "WARNING: NEXUS_SECRET_KEY not set, Nexus will use its default encryption key" >&2
fi

exec "$@"
