# kotlin-rocket-bot

[Rocket.Chat](https://rocket.chat/) bot implemented in Kotlin. Utilizes Rocket.Chat's websocket-based [Realtime API](https://developer.rocket.chat/api/realtime-api).

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=paulchen_kotlin-rocket-bot&metric=alert_status)](https://sonarcloud.io/dashboard?id=paulchen_kotlin-rocket-bot)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

This project depends on the library from the project [paulchen/kotlin-rocket-lib](https://github.com/paulchen/kotlin-rocket-lib),
so make sure to check that one out as well. Build `kotlin-rocket-lib` and deploy it
to the local Maven repository by running

`gradlew publishToMavenLocal`

Then, build the local Docker image of `kotlin-rocket-bot` (`kotlin-rocket-bot:latest`) with:

`gradlew docker`

Run the Docker image with:

`docker run -it -e "ROCKETCHAT_HOST=<host>" -e "ROCKETCHAT_USERNAME=<username>" -e ROCKETCHAT_PASSWORD="<password>" kotlin-rocket-bot:latest`

Set the environment variables `ROCKETCHAT_HOST`, `ROCKETCHAT_USERNAME`, and `ROCKETCHAT_PASSWORD` according to your needs.

An additional environment variable named `IGNORED_CHANNELS` is supported, containing a comma-separated list of channel names (without leading `#`) the bot should ignore (intended for testing purposes).

A systemd unit file for launching the bot could look like this:

```[Unit]
Description=kotlin-rocket-bot
After=network.service

[Service]
EnvironmentFile=/etc/environment
User=rocketbot
Restart=always
TimeoutStartSec=0
ExecStartPre=-/usr/bin/docker stop kotlin-rocket-bot
ExecStartPre=-/usr/bin/docker rm kotlin-rocket-bot

ExecStart=/usr/bin/docker run \
    --name kotlin-rocket-bot \
    -e ROCKETCHAT_HOST=<host> \
    -e ROCKETCHAT_USERNAME=<username> \
    -e ROCKETCHAT_PASSWORD=<password> \
    -e IGNORED_CHANNELS=general \
    -p 127.0.0.1:8081:8080 \
    kotlin-rocket-bot:latest

ExecStop=-/usr/bin/docker stop kotlin-rocket-bot
ExecStop=-/usr/bin/docker rm kotlin-rocket-bot

[Install]
WantedBy=multi-user.target
```

The above systemd unit will expose the container's port 8080 to localhost:8081.
This port features a webservice intended to be called by the Icinga check script `misc/check_bot.sh`.
This script expects the port of the webservice in the environment variable `WEBSERVICE_PORT`:

`WEBSERVICE_PORT=8081 misc/check_bot.sh`
