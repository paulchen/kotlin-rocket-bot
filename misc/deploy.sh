#!/bin/bash

DIRECTORY=`dirname "$0"`
cd "$DIRECTORY/.."

if [ ! -d ../kotlin-rocket-lib/ ]; then
	echo kotlin-rocket-lib not found
	exit 1
fi

export JAVA_HOME=/usr/lib/jvm/java-21-openjdk/

if [ ! -d "$JAVA_HOME" ]; then
	echo "$JAVA_HOME not found"
	exit 2
fi

DEPLOY_HASH=`sha256sum misc/deploy.sh`
git pull || exit 3
if [ "$DEPLOY_HASH" != "`sha256sum misc/deploy.sh`" ]; then
	misc/deploy.sh
	exit $?
fi

cd ../kotlin-rocket-lib

git pull || exit 3

LIB_VERSION=`grep ^version build.gradle.kts |sed -e 's/^[^"]*"//;s/"$//'`

./gradlew publishToMavenLocal || exit 3

cd ../kotlin-rocket-bot

LIB_DEPENDENCY_VERSION=`grep kotlin-rocket-lib bot/build.gradle.kts |sed -e 's/^.*://;s/".*$//'`

if [ "$LIB_VERSION" != "$LIB_DEPENDENCY_VERSION" ]; then
	echo "Wrong dependency version $LIB_DEPENDENCY_VERSION for kotlin-rocket-lib (should be $LIB_VERSION)"
	exit 4
fi

docker pull debian:bookworm-slim || exit 3
docker pull eclipse-temurin:21-jdk || exit 3

./gradlew clean build || exit 3

docker build --no-cache -t kotlin-rocket-bot:latest . || exit 3

if [ "$1" != "--no-systemd" ]; then
	sudo systemctl restart kotlin-rocket-bot || exit 3
fi

