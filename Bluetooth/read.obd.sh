#!/usr/bin/env bash
CP=./build/libs/Bluetooth-1.0-all.jar
JAVA_OPTS=
DARWIN=$(uname -a | grep Darwin)
if [[ "$DARWIN" != "" ]]
then
	echo Running on Mac
  JAVA_OPTS="$JAVA_OPTS -Djava.library.path=/Library/Java/Extensions"  # for Mac
else
	echo Assuming Linux/Raspberry Pi
  JAVA_OPTS="$JAVA_OPTS -Djava.library.path=/usr/lib/jni"              # RPi
  CP=${CP}:/usr/share/java/RXTXcomm.jar # For Raspberry Pi
  SUDO="sudo "
fi
#
JAVA_OPT="$JAVA_OPTS -Dobd.verbose=true"
# JAVA_OPTS="$JAVA_OPTS -Dbt.serial.port=/dev/ttyS0"
# JAVA_OPTS="$JAVA_OPTS -Dbt.serial.port=/dev/ttyAMA0"
# JAVA_OPTS="$JAVA_OPTS -Dbt.serial.port=/dev/tty.Bluetooth-Incoming-Port"
JAVA_OPTS="$JAVA_OPTS -Dbt.serial.port=/dev/tty.HC-05-DevB"
#
COMMAND="${SUDO}java -cp $CP $JAVA_OPTS obd.SimpleOBDReader"
echo -e "Running ${COMMAND}"
${COMMAND}
