#!/bin/bash
#
# Read a Joystick output
#
CP=./build/libs/Joystick-1.0-all.jar
JAVA_OPTS="-Djoystick.debug=true"
COMMAND="java $JAVA_OPTS -cp $CP joystick.JoystickReader"
echo -e "Executing $COMMAND ..."
echo -e "Enter [Return]"
read a
$COMMAND
