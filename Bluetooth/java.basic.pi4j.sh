#!/usr/bin/env bash
#
CP=./build/libs/Bluetooth-1.0-all.jar
#
JAVA_OPTS=
echo Assuming Linux/Raspberry Pi
JAVA_OPTS="$JAVA_OPTS -Djava.library.path=/usr/lib/jni"              # RPi
CP=${CP}:/usr/share/java/RXTXcomm.jar # For Raspberry Pi
#
JAVA_OPTS="$JAVA_OPTS -Dbt.verbose=true"
JAVA_OPTS="$JAVA_OPTS -Dbaud.rate=9600"
############################
# JAVA_OPTS="$JAVA_OPTS -Dserial.port=/dev/ttyS0"
JAVA_OPTS="$JAVA_OPTS -Dport.name=/dev/rfcomm0"
############################
COMMAND="${SUDO}java -cp $CP $JAVA_OPTS basic.SimpleSerialPI4JCommunication"
${COMMAND}
