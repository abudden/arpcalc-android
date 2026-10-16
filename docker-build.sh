#!/bin/sh
# Build ARPCalc in Docker, so the Android SDK doesn't need installing locally.
#
#   ./docker-build.sh                    debug APK + unit tests
#   ./docker-build.sh assembleRelease    any other Gradle tasks
#
# Runs as the current user (also when invoked via sudo) so build outputs
# aren't owned by root.
cd "$(dirname "$0")" || exit 1
uid=${SUDO_UID:-$(id -u)}
gid=${SUDO_GID:-$(id -g)}
exec docker compose run --rm --build --user "$uid:$gid" build "$@"
