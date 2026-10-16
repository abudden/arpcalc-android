#!/bin/bash
# Entry point of the build container: runs the Gradle wrapper with the given
# tasks (default: debug APK plus unit tests).
set -e

if [ $# -eq 0 ]
then
	set -- assembleDebug testDebugUnitTest
fi

exec ./gradlew --no-daemon "$@"
