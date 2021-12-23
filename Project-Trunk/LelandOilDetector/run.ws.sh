#!/bin/bash
echo Reads an ADC, feeds a WebSocket server, drives a relay, sends and receives SMSs.
#
if [ "${PI4J_HOME}" = "" ]
then
  PI4J_HOME=/opt/pi4j
fi
#
CP=./classes
CP=${CP}:${PI4J_HOME}/lib/pi4j-core.jar
CP=${CP}:./lib/java_websocket.jar
CP=${CP}:./lib/jansi-1.9.jar
CP=${CP}:./lib/json.jar
CP=${CP}:../ADC/classes
CP=${CP}:../FONA/classes
CP=${CP}:../HC-SR04/classes
#
# See props.propeties
#
JAVA_OPTS=
JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=../HC-SR04/C"
JAVA_OPTS="${JAVA_OPTS} -Ddebug=true -Dansi.console=true"
# JAVA_OPTS="${JAVA_OPTS} -client -agentlib:jdwp=transport=dt_socket,server=y,address=1044"
##
COMMAND="java ${JAVA_OPTS} -cp ${CP} adc.levelreader.main.LelandPrototype $*"
# COMMAND="java ${JAVA_OPTS} -cp ${CP} adc.utils.EscapeSeq"
echo Running ${COMMAND}
#
sudo ${COMMAND}
