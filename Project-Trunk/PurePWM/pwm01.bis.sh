#!/bin/bash
CP=./build/libs/PurePWM-1.0-all.jar
#
PIN=12
if [[ $# -gt 0 ]]
then
  PIN=$1
fi
echo -e "Using physical pin #$PIN"
#
JAVA_OPTIONS=
JAVA_OPTIONS="${JAVA_OPTIONS} -Dservo.pin=$PIN"
#
sudo java ${JAVA_OPTIONS} -cp ${CP} pwm.Pwm01
