#!/bin/bash
CP=./build/libs/PurePWM-1.0-all.jar
#
JAVA_OPTIONS=
# JAVA_OPTIONS="$JAVA_OPTIONS -D..."
#
sudo java ${JAVA_OPTIONS} -cp ${CP} sample.FourPWMLeds
