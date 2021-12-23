#!/bin/bash
CP=./build/libs/PurePWM-1.0-all.jar
#
JAVA_OPTIONS=
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dwhatever=true"
# Use --pin 4 or -p 4 as script argument
sudo java ${JAVA_OPTIONS} -cp ${CP} sample.SoftPwmExample $*
