#!/bin/bash
CP=./build/libs/Utils-1.0-all.jar
#
# For remote debugging
REMOTE_DEBUG_FLAGS=
# REMOTE_DEBUG_FLAGS="$REMOTE_DEBUG_FLAGS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
#
# java -cp ${CP} ${REMOTE_DEBUG_FLAGS} -Dbutton.verbose=true utils.gpio.PushButtonControllerSample
java -cp ${CP} ${REMOTE_DEBUG_FLAGS} -Dbutton.verbose=true utils.gpio.TwoPushButtonControllerSample
#