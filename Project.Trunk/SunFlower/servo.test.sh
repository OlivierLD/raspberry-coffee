#!/bin/bash
#
# Device heading from the terminal console
#
CP=./build/libs/SunFlower-1.0-all.jar
JAVA_OPTS=
#
sudo java -cp $CP $JAVA_OPTS orientation.InteractiveServoTester --heading:14 --tilt:15
