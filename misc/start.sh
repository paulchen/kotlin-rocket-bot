#!/bin/bash

/usr/bin/docker run \
    --name kotlin-rocket-bot \
    -v /etc/kotlin-rocket-bot:/config \
    -v /var/cache/kotlin-rocket-bot:/cache \
    -e TZ=Europe/Vienna \
    -e DOCKER_VERSION="$(docker -v)" \
    --net=rocketchat_default \
    -p 127.0.0.1:8081:8082 \
#    -p 127.0.0.1:5005:5005 \
    --add-host=host.docker.internal:host-gateway \
    --log-driver=journald \
    --log-opt tag={{.ImageName}} \
    kotlin-rocket-bot:latest
