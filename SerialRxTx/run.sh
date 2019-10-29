#!/bin/bash
#
CP=./build/libs/SerialRxTx-1.0-all.jar
CP=${CP}:/usr/share/java/RXTXcomm.jar
#
SERIAL_PRMS="-Dserial.port=/dev/ttyUSB0"
SERIAL_PRMS="$SERIAL_PRMS -Dbaud.rate=115200"
#
NATIVE_LIB="-Djava.library.path=/usr/lib/jni"
#
VERBOSE="-Dverbose=false"
#
java ${NATIVE_LIB} ${SERIAL_PRMS} ${VERBOSE} -cp ${CP} console.SerialConsoleCLI
