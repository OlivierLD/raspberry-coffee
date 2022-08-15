#!/bin/bash
CP=../build/libs/ADCs-Servos-JoySticks-1.0-all.jar
#
SERVO_NUM=$1
#
if [[ "${SERVO_NUM}" == "" ]]; then
  echo -en "Enter servo # [0..15] > "
  read SERVO_NUM
fi
#
sudo java -cp ${CP} -Dverbose=true servo.StandardServoTest ${SERVO_NUM}
