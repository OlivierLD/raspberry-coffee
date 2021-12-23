#!/bin/bash
# 
SCALA_HOME=/home/pi/.sbt/boot/scala-2.10.3
PI4J_HOME=/opt/pi4j
#
CP=$SCALA_HOME/lib/scala-library.jar
# 
CP=${CP}:${PI4J_HOME}/lib/pi4j-core.jar
CP=${CP}:../../../SevenSegDisplay/build/classes/main
CP=${CP}:../../build/classes/main
# 
sudo scala -classpath "${CP}" sevenseg.Clock
