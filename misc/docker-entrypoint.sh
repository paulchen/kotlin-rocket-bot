#!/bin/bash

cd /app/kotlin-rocket-bot

trap 'killall -SIGTERM java' SIGTERM

bin/bot &
wait $!
