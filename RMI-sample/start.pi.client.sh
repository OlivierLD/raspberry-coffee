#!/bin/bash
#
echo -e "Usage is:"
echo -e "${0} <server-name> <port-number> <nb-decimal-for-PI>"
echo -e "--"
#
CP=.
# CP=${CP}:./build/libs/compute.jar
CP=${CP}:./build/libs/RMI.sample-1.0.jar
CP=${CP}:./build/classes
#
JAVA_OPTS=
#
COMMAND="java -cp ${CP} ${JAVA_OPTS} client.ComputePi"
echo Executing ${COMMAND}
#
${COMMAND} $*

