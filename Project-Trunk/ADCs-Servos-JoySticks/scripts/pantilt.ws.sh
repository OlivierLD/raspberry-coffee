#!/bin/bash
CP=../build/libs/ADCs-Servos-JoySticks-1.0-all.jar
#
sudo java -cp $CP joystick.PanTiltWebSocket
