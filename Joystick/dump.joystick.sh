#!/bin/bash
#
# Read a Joystick output
#
CP=./build/libs/Joystick-1.0-all.jar
JAVA_OPTS="-Djoystick.debug=true"
COMMAND="java ${JAVA_OPTS} -cp ${CP} joystick.JoystickReaderV2"
echo -e "Executing ${COMMAND} ..."
echo -e "Enter [Return]"
read a
${COMMAND}
