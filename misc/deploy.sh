#!/bin/bash

DIRECTORY=`dirname "$0"`
cd "$DIRECTORY/.."

if [ ! -d ../kotlin-rocket-lib/ ]; then
	echo kotlin-rocket-lib not found
	exit 1
fi

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/

if [ ! -d "$JAVA_HOME" ]; then
	echo "$JAVA_HOME not found"
	exit 2
fi

cd ../kotlin-rocket-lib

git pull || exit 3

./gradlew publishToMavenLocal || exit 3

cd ../kotlin-rocket-bot

git pull || exit 3

docker pull debian:bullseye-slim || exit 3
docker pull eclipse-temurin:17-jdk || exit 3

./gradlew docker || exit 3

sudo systemctl restart kotlin-rocket-bot || exit 3

