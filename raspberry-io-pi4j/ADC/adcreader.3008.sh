#!/bin/bash
echo -e "Read an ADC, MCP3008"
#
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Dadc.verbose=false"
JAVA_OPTS="$JAVA_OPTS -Ddisplay.digit=false"
CP=./build/libs/ADC-1.0-all.jar
#
REMOTE_DEBUG_FLAGS=
# Make sure you have suspend=y below, for this kind of app
REMOTE_DEBUG_FLAGS="${REMOTE_DEBUG_FLAGS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
#
if [[ "$(which gpio)" != "" ]]; then
  # Show in and out pins:
  echo -e "Output of gpio readall:"
  gpio readall
fi
# Channel [0..1] can be passed as prm. Default is 0
COMMAND="sudo java -cp ${CP} ${JAVA_OPTS} ${REMOTE_DEBUG_FLAGS} analogdigitalconverter.sample.MainMCP3008Sample $*"
# COMMAND="java -cp ${CP} ${JAVA_OPTS} ${REMOTE_DEBUG_FLAGS} analogdigitalconverter.sample.MainMCP3008Sample $*"
echo -e "------------------------------------------------------------------------------"
echo -e "Running ${COMMAND}"
echo -e "------------------------------------------------------------------------------"
${COMMAND}
