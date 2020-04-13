#!/usr/bin/env bash
CP=./build/libs/Monitor.Battery-1.0-all.jar
OPT=
OPT="$OPT -Dverbose=true"
COMMAND="sudo java -cp $CP $OPT battery.ws.WSPublisher"
echo "Running $COMMAND"
$COMMAND
