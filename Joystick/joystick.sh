#!/bin/bash
#
# Read a Joystick output
#
CP=./build/libs/Joystick-1.0.jar
JAVA_OPTS="-Djoystick.debug=true"
COMMAND="java $JAVA_OPTS -cp $CP sample.WebSocketSample"
echo -e "Executing $COMMAND ..."
echo -e "Enter [Return]"
read a
$COMMAND
