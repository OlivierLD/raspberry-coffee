#!/bin/bash
CP=./build/libs/TemperatureMonitor-1.0-all.jar
#
REMOTE_DEBUG_FLAGS=
# Make sure suspend is set to 'y'
# JDK 9 and up
# REMOTE_DEBUG_FLAGS="${REMOTE_DEBUG_FLAGS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
# JDK 5-8
# REMOTE_DEBUG_FLAGS="${REMOTE_DEBUG_FLAGS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005"
#
java -cp ${CP} ${REMOTE_DEBUG_FLAGS} monitor.SwingTemperatureMonitor $*
