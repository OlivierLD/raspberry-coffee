#!/bin/bash
#
CP=.
CP=${CP}:./build/libs/compute.jar
CP=${CP}:./build/classes
#
JAVA_OPTS=
#
COMMAND="java -cp ${CP} ${JAVA_OPTS} client.AskToSpeak"
echo Executing ${COMMAND}
#
${COMMAND} $*

