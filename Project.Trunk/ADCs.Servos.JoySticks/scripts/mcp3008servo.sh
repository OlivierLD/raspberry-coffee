#!/bin/bash
CP=../build/libs/ADCs.Servos.Joysticks-1.0-all.jar
#
# parameters: --adc-channel:5 --servo-port:14
# default     --adc-channel:0 --servo-port:0
#
sudo java -cp $CP raspisamples.servo.ServoAndPotentiometer $*
#
