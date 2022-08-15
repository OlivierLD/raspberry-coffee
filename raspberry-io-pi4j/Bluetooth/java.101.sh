#!/usr/bin/env bash
SUDO=
DARWIN=$(uname -a | grep Darwin)
#
CP=./build/libs/Bluetooth-1.0-all.jar
#
JAVA_OPTS=
if [[ "$DARWIN" != "" ]]; then
	echo Running on Mac
  JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=/Library/Java/Extensions"  # for Mac
  CP=${CP}:./libs/RXTXcomm.jar          # for Mac
else
	echo Assuming Linux/Raspberry Pi
  JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=/usr/lib/jni"              # RPi
  CP=${CP}:/usr/share/java/RXTXcomm.jar # For Raspberry Pi
  SUDO="sudo "
fi
#
JAVA_OPTS="${JAVA_OPTS} -Dbaud.rate=9600"
############################
# For Raspberry Pi
# JAVA_OPTS="${JAVA_OPTS} -Dserial.port=/dev/ttyS0"
# JAVA_OPTS="${JAVA_OPTS} -Dserial.port=/dev/rfcomm0"
############################
# For Mac
# JAVA_OPTS="${JAVA_OPTS} -Dserial.port=/dev/tty.Bluetooth-Incoming-Port"
JAVA_OPTS="${JAVA_OPTS} -Dserial.port=/dev/tty.HC-05-DevB"
############################
COMMAND="${SUDO}java -cp ${CP} ${JAVA_OPTS} bt.BT101"
${COMMAND}
