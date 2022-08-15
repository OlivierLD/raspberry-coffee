#!/bin/bash
JAVAC_OPTIONS="-sourcepath ./src/main/java"
JAVAC_OPTIONS="${JAVAC_OPTIONS} -d ./classes"
echo ${JAVAC_OPTIONS}
CP=./classes
if [[ "${PI4J_HOME}" = "" ]]; then
  PI4J_HOME=/opt/pi4j
fi
CP=${CP}:${PI4J_HOME}/lib/pi4j-core.jar
CP=${CP}:./lib/almanactools.jar
CP=${CP}:./lib/geomutil.jar
CP=${CP}:./lib/nauticalalmanac.jar
CP=${CP}:./lib/nmeaparser.jar
# JAVAC_OPTIONS="-verbose ${JAVAC_OPTIONS}"
JAVAC_OPTIONS="${JAVAC_OPTIONS} -cp ${CP}"
COMMAND="javac ${JAVAC_OPTIONS} ./src/nmea/*.java ./src/readserialport/*.java"
echo Compiling: ${COMMAND}
${COMMAND}
echo Done
