#!/bin/bash
#
# Read a Joystick output
#
CP=./build/libs/Serial.IO-1.0-all.jar
# COMMAND="java $JAVA_OPTS -cp $CP sample.JoystickReader"
COMMAND="java $JAVA_OPTS -cp $CP sample.JoystickReaderV2"
echo -e "Executing $COMMAND ..."
echo -e "Enter [Return]"
read a
$COMMAND
