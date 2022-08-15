#!/bin/bash
echo Read serial port, parse the RMC String
echo Usage $0 [BaudRate] \(default 9600\)
echo Try 2400, 4800, 9600, 19200, 38400, 57600, 115200, ...
if [[ "${PI4J_HOME}" = "" ]]; then
  PI4J_HOME=/opt/pi4j
fi
CP=./classes
CP=${CP}:${PI4J_HOME}/lib/pi4j-core.jar
CP=${CP}:./lib/almanactools.jar
CP=${CP}:./lib/geomutil.jar
CP=${CP}:./lib/nauticalalmanac.jar
CP=${CP}:./lib/nmeaparser.jar
CP=${CP}:./lib/coreutilities.jar
JAVA_OPTIONS=
# Default serial port is /dev/ttyAMA0
JAVA_OPTIONS="${JAVA_OPTIONS} -Dport.name=/dev/ttyUSB0"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dverbose=true"
#sudo java -cp ${CP} ${JAVA_OPTIONS} nmea.CustomRMCReader $*
sudo java -cp ${CP} ${JAVA_OPTIONS} nmea.CustomGGAReader $*
