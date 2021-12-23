#!/bin/bash
if [ "${PI4J_HOME}" = "" ]
then
  PI4J_HOME=/opt/pi4j
fi
#
JAVA_OPTS=
# JAVA_OPTS="${JAVA_OPTS} -client -agentlib:jdwp=transport=dt_socket,server=y,address=1044"
# JAVA_OPTS="${JAVA_OPTS} -Dverbose=true"
CP=./build/classes/main
CP=${CP}:${PI4J_HOME}/lib/pi4j-core.jar
export LD_LIBRARY_PATH=./C
# ls -l $LD_LIBRARY_PATH/*.so
JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=$LD_LIBRARY_PATH"
#
COMMAND="sudo java ${JAVA_OPTS} -cp ${CP} olivsound.Theremin"
echo Executing [${COMMAND}]
${COMMAND}
#
