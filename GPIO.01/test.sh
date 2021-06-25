#!/bin/bash
echo Speed Test
CP=./classes:/home/pi/pi4j/pi4j-distribution/target/distro-contents/lib/pi4j-core.jar
sudo java -cp $CP gpio01.SpeedTest

