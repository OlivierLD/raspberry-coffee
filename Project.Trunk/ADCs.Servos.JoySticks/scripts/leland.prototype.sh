#!/bin/bash
CP=../build/libs/ADCs.Servos.Joysticks-1.0.jar
#
echo MCP3008, seven channels
JAVA_OPTIONS=
JAVA_OPTIONS="$JAVA_OPTIONS -Dverbose=true"
JAVA_OPTIONS="$JAVA_OPTIONS -Dwater.threshold=50"
JAVA_OPTIONS="$JAVA_OPTIONS -Doil.threshold=20"
# JAVA_OPTIONS="$JAVA_OPTIONS -client -agentlib:jdwp=transport=dt_socket,server=y,address=4000"
echo Running...
sudo java $JAVA_OPTIONS -cp $CP joystick.adc.levelreader.LelandPrototype
