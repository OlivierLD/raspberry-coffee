#!/bin/bash
CP=./build/libs/ADCs-1.0-all.jar
#
sudo java -cp $CP joystick.PanTiltWebSocket
