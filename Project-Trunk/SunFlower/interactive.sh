#!/bin/bash
#
# Device heading from the terminal console
#
CP=./build/libs/SunFlower-1.0-all.jar
JAVA_OPTS=
JAVA_OPTS="${JAVA_OPTS} -Dlatitude=37.7489 -Dlongitude=-122.5070 -Declination=14"
JAVA_OPTS="${JAVA_OPTS} -Dorient.verbose=true -Dastro.verbose=true"
JAVA_OPTS="${JAVA_OPTS} -Dtilt.verbose=true"
# JAVA_OPTS="${JAVA_OPTS} -Dmanual.entry=true"
JAVA_OPTS="${JAVA_OPTS} -Dansi.console=false"
#
JAVA_OPTS="${JAVA_OPTS} -Dauto.demo=false"
#
sudo java -cp ${CP} ${JAVA_OPTS} orientation.InteractivePanelOrienter --heading:14 --tilt:15
