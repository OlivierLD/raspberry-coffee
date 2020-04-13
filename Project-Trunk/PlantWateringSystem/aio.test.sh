#!/usr/bin/env bash
#
CP=build/libs/PlantWateringSystem-1.0-all.jar
#
JAVA_OPTIONS="-Daio.key=54c2767878ca793f2e3cae1c45d62aa7ae9f8056 -Daio.verbose=true"
COMMAND="java $JAVA_OPTIONS -cp $CP loggers.iot.AdafruitIOClient"
echo -e "Running $COMMAND"
$COMMAND
#
