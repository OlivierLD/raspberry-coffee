#!/bin/bash
#
# Read a GPS
#
CP=$(ls ./build/libs/*.jar)
#
CP=${CP}:/usr/share/java/RXTXcomm.jar
echo Make sure the GPS is connected through its USB socket.
#
SERIAL_PORT=/dev/ttyUSB0 # RPi
# SERIAL_PORT=/dev/tty.Bluetooth-Incoming-Port # Mac
# SERIAL_PORT=/dev/tty.usbserial # Mac
BAUD_RATE=4800
#
JAVA_OPTS="-Dserial.port=${SERIAL_PORT} -Dbaud.rate=${BAUD_RATE}"
#
SUDO=
# DARWIN=`uname -a | grep Darwin`
DARWIN=$(uname -a | grep Darwin)  # TODO Use uname -s
#
if [[ "${DARWIN}" != "" ]]; then
	echo Running on Mac
  JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=/Library/Java/Extensions"  # for Mac
else
	echo Assuming Linux/Raspberry Pi
  JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=/usr/lib/jni"              # RPi
  SUDO="sudo "
fi
#
# Sentence filter? like RMC,GLL,GSA
if [[ "$1" != "" ]]; then
  JAVA_OPTS="${JAVA_OPTS} -Dfilters=$1"
fi
#
COMMAND="${SUDO}java ${JAVA_OPTS} -cp ${CP} sample.GPSReader"
echo -e "Executing ${COMMAND} ..."
echo -e "Enter [Return]"
read a
${COMMAND}
