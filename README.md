# kotlin-rocket-bot

[Rocket.Chat](https://rocket.chat/) bot implemented in Kotlin.

Build local Docker image (`kotlin-rocket-bot:latest`) with:

`gradlew docker`

Run with:

`docker run -it -e "ROCKETCHAT_HOST=<host>" -e "ROCKETCHAT_USERNAME=<username>" -e ROCKETCHAT_PASSWORD="<password>" kotlin-rocket-bot:latest`

Set the environment variables `ROCKETCHAT_HOST`, `ROCKETCHAT_USERNAME`, and `ROCKETCHAT_PASSWORD` according to your needs.

An additional environment variable named `IGNORED_CHANNELS` is supported, containing a comma-separated list of channel names (without leading `#`) the bot should ignore (intended for testing purposes).

A systemd unit file for launching the bot could look like this:

```[Unit]
Description=kotlin-rocket-bot
After=network.servoce

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
    kotlin-rocket-bot:latest

ExecStop=-/usr/bin/docker stop kotlin-rocket-bot
ExecStop=-/usr/bin/docker rm kotlin-rocket-bot

[Install]
WantedBy=multi-user.target```

