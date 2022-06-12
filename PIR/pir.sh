#!/bin/bash
# PI4J_HOME=/opt/pi4j
# CP=./classes
# CP=$CP:$PI4J_HOME/lib/pi4j-core.jar

CP=./build/libs/PIR-1.0-all.jar
JAVA_OPTS=
# JAVA_OPTS=-Dpir.verbose=true 
sudo java -cp ${CP} ${JAVA_OPTS} main.SampleMain
