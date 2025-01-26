#!/bin/bash

cd /app/bot-latest

trap 'killall -SIGTERM java' SIGTERM

bin/bot &
wait $!
