#!/usr/bin/env bash
# Joystick
#
# TODO add a camera -> SEE RESTCAM PROJECT
#
CP=../build/libs/ADCs.Servos.Joysticks-1.0-all.jar
JAVA_OPT="-Dverbose=true"
JAVA_OPT="$JAVA_OPT -Djoystick.verbose=true"
#
sudo java -cp $CP $JAVA_OPT joystick.PanTiltJoyStick -ud:8 -lr:9 -adcLR:2 -adcUD:3

