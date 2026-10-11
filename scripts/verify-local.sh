#!/usr/bin/env bash
set -euo pipefail

: "${DB_USERNAME:=root}"
: "${DB_PASSWORD:?Set DB_PASSWORD before running this script}"
: "${JWT_SECRET:?Set JWT_SECRET before running this script}"

printf '\n== Backend compile/test ==\n'
(cd backend && mvn clean test)
printf '\n== Frontend build ==\n'
(cd frontend && npm ci && npm run build)
printf '\nAll local build checks passed.\n'
