#!/bin/bash
JAVAC_OPTIONS="-sourcepath ./src"
JAVAC_OPTIONS="${JAVAC_OPTIONS} -d ./classes"
echo ${JAVAC_OPTIONS}
CP=./classes
CP=${CP}:${PI4J_HOME}/lib/pi4j-core.jar
# JAVAC_OPTIONS="-verbose ${JAVAC_OPTIONS}"
JAVAC_OPTIONS="${JAVAC_OPTIONS} -cp ${CP}"
COMMAND="javac ${JAVAC_OPTIONS} `find ./src -name '*.java' -print`"
echo Compiling: ${COMMAND}
${COMMAND}
echo Done

