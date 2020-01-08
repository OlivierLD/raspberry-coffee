#!/bin/bash
#
# Read a dAISy HAT
#
CP=./build/libs/dAISy-1.0-all.jar
CP=${CP}:/usr/share/java/RXTXcomm.jar
#
SERIAL_PORT=/dev/ttyS0
BAUD_RATE=38400
VERBOSE=true  # Verbose => DualDump
#
JAVA_OPTS="-Dserial.port=$SERIAL_PORT -Dbaud.rate=$BAUD_RATE -Dserial.verbose=$VERBOSE"
#
SUDO=
# DARWIN=`uname -a | grep Darwin`
DARWIN=$(uname -a | grep Darwin)
#
if [[ "$DARWIN" != "" ]]
then
	echo Running on Mac
  JAVA_OPTS="$JAVA_OPTS -Djava.library.path=/Library/Java/Extensions"  # for Mac
else
	echo Assuming Linux/Raspberry Pi
  JAVA_OPTS="$JAVA_OPTS -Djava.library.path=/usr/lib/jni"              # RPi
  SUDO="sudo "
fi
#
COMMAND="${SUDO}java $JAVA_OPTS -cp $CP ais.sample.AISReaderSample"
echo -e "Executing $COMMAND ..."
echo -e "Enter [Return]"
read a
${COMMAND}
