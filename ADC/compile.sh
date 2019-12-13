#!/bin/bash
#
# @deprecated
# and the lib directory is not there anymore.
#
mkdir classes
JAVAC_OPTIONS="-sourcepath ./src"
JAVAC_OPTIONS="$JAVAC_OPTIONS -d ./classes"
echo $JAVAC_OPTIONS
CP=./classes
CP=$CP:$PI4J_HOME/lib/pi4j-core.jar
CP=$CP:./lib/jansi-1.9.jar
CP=$CP:./lib/Java-WebSocket-1.3.0.jar
# JAVAC_OPTIONS="-verbose $JAVAC_OPTIONS"
JAVAC_OPTIONS="$JAVAC_OPTIONS -cp $CP -encoding ISO-8859-1"
COMMAND="javac $JAVAC_OPTIONS `find ./src -name '*.java' -print`"
echo Compiling: $COMMAND
$COMMAND
echo Done
