#!/bin/bash

DIRECTORY=`dirname "$0"`
cd "$DIRECTORY/.."

if [ ! -d ../kotlin-rocket-lib/ ]; then
	echo kotlin-rocket-lib not found
	exit 1
fi

DEPLOY_HASH=`sha256sum misc/deploy.sh`
git pull || exit 3
if [ "$DEPLOY_HASH" != "`sha256sum misc/deploy.sh`" ]; then
	misc/deploy.sh
	exit $?
fi

docker pull debian:trixie-slim || exit 3
docker pull eclipse-temurin:25-jdk || exit 3
docker pull eclipse-temurin:25-jre || exit 3

docker build --no-cache -t kotlin-rocket-bot:latest . || exit 3

if [ "$1" != "--no-systemd" ]; then
	sudo systemctl restart kotlin-rocket-bot || exit 3
fi

