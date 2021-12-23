#!/bin/bash
#
# Reads Water Level and Temperature.
# Feeds the WebSocket server at the address mentioned in
# System variable "ws.uri", default is "ws://localhost:9876".
# If this WS Server is running on Windows, make sure
# the firewall on this machine is turned off.
# To start the server:
# Prompt> cd node; node level.server.js
# GUI will be available at http://machine:9876/level.station/analog.html
#
CP=../build/libs/ADCs-Servos-JoySticks-1.0-all.jar
#
JAVA_OPTIONS=
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dverbose=true"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dthreshold=50"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dws.uri=ws://192.168.1.77:9876/"
# JAVA_OPTIONS="${JAVA_OPTIONS} -client -agentlib:jdwp=transport=dt_socket,server=y,address=4000"
echo Running...
sudo java ${JAVA_OPTIONS} -cp ${CP} joystick.adc.levelreader.samples.LevelAndTemperature
