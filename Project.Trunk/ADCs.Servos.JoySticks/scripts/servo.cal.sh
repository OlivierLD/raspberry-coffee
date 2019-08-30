#!/bin/bash
CP=../build/libs/ADCs.Servos.Joysticks-1.0-all.jar
#
sudo java -cp $CP servo.StandardServo $*
