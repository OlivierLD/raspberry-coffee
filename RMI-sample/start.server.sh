#!/bin/bash
#
CP=.
# CP=${CP}:./build/libs/compute.jar
CP=${CP}:./build/libs/RMI.sample-1.0.jar 
CP=${CP}:./build/classes
#
HOSTNAME=$(hostname)
if [[ "$1" != "" ]]; then
  HOSTNAME=$1
fi  
JAVA_OPTS=
# JAVA_OPTS="${JAVA_OPTS} -verbose"
JAVA_OPTS="${JAVA_OPTS} -Djava.rmi.server.hostname=${HOSTNAME}"
#
COMMAND="java -cp ${CP} ${JAVA_OPTS} engine.ComputeEngine"
echo Executing ${COMMAND}
#
${COMMAND}

