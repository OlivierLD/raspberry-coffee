#!/bin/bash
CP=../build/libs/ADCs.Servos.Joysticks-1.0.jar
#
sudo java -cp $CP servo.InteractiveServo $*
