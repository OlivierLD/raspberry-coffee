#!/bin/bash
#
# Read a Joystick output
#
CP=./build/libs/Serial.IO-1.0-all.jar
COMMAND="java $JAVA_OPTS -cp $CP sample.JoystickReader"
echo -e "Executing $COMMAND ..."
echo -e "Enter [Return]"
read a
$COMMAND
