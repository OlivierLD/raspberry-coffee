#!/bin/bash
CP=./build/libs/Led.PWM.etc-1.0-all.jar
#
sudo $JAVA_HOME/bin/java -cp $CP tests.Real4PWMLedV2
