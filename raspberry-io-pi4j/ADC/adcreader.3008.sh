#!/bin/bash
echo Read an ADC
#
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Dadc.verbose=false"
JAVA_OPTS="$JAVA_OPTS -Ddisplay.digit=false"
CP=./build/libs/ADC-1.0-all.jar
#
REMOTE_DEBUG_FLAGS=
REMOTE_DEBUG_FLAGS="${REMOTE_DEBUG_FLAGS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
#
# Channel [0..1] can be passed as prm. Default is 0
sudo java -cp ${CP} ${JAVA_OPTS} ${REMOTE_DEBUG_FLAGS} analogdigitalconverter.sample.MainMCP3008Sample $*
