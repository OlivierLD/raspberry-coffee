#!/bin/bash
# PI4J_HOME=/opt/pi4j
# CP=./classes
# CP=$CP:$PI4J_HOME/lib/pi4j-core.jar
CP=./build/libs/RasPISamples-1.0-all.jar
#
JAVA_OPTIONS=
# JAVA_OPTIONS="$JAVA_OPTIONS -Dverbose=true"
# For remote debugging:
# JAVA_OPTIONS="$JAVA_OPTIONS -client -agentlib:jdwp=transport=dt_socket,server=y,address=4000"
# For remote JVM Monitoring
# JAVA_OPTIONS="$JAVA_OPTIONS -Dcom.sun.management.jmxremote.port=1234 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=raspberrypi-boat"
#
JAVA_OPTIONS="$JAVA_OPTIONS -Dbutton.verbose=true"
#
# Physical pin numbers.
PRMS="--button:16 --shift:11"
#
echo Running...
sudo java $JAVA_OPTIONS -cp $CP breadboard.button.v2.SampleMainTwoButtons $PRMS
