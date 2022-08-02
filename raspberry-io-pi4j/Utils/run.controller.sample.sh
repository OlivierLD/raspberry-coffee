#!/bin/bash
CP=./build/libs/Utils-1.0-all.jar
#
# For remote debugging
REMOTE_DEBUG_FLAGS=
# REMOTE_DEBUG_FLAGS="${REMOTE_DEBUG_FLAGS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
#
JAVA_OPTIONS=
JAVA_OPTIONS="-Dbutton.verbose=true"
# java -cp ${CP} ${REMOTE_DEBUG_FLAGS} ${JAVA_OPTIONS} utils.gpio.PushButtonControllerSample
COMMAND="java -cp ${CP} ${REMOTE_DEBUG_FLAGS} ${JAVA_OPTIONS} utils.gpio.TwoPushButtonControllerSample"
echo -e "Executing ${COMMAND}"
${COMMAND}
#
