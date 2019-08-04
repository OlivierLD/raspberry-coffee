#!/bin/bash
echo Interactive RGB led
CP=../build/libs/Led.PWM.etc-1.0.jar
sudo java -cp $CP rgbled.RGBLed
