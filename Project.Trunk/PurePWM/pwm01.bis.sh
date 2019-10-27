#!/bin/bash
CP=./build/libs/PurePWM-1.0-all.jar
#
JAVA_OPTIONS=
JAVA_OPTIONS="$JAVA_OPTIONS -Dservo.pin=12"
#
sudo java $JAVA_OPTIONS -cp $CP pwm.Pwm01
