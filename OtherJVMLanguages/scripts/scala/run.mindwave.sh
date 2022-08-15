#!/bin/bash
if [[ "${PI4J_HOME}" = "" ]]; then
  PI4J_HOME=/opt/pi4j
fi
# 
CP=${PI4J_HOME}/lib/pi4j-core.jar
CP=${CP}:../../../MindWave/build/classes/main
CP=${CP}:../../build/classes/main
# 
echo Compiling
scalac -verbose -sourcepath src -cp ${CP} -d ../../build/classes/main ../../src/scala/MindWaveClient.scala
#
echo Running. Make sure the MindWave dongle is connected.
scala -verbose -cp "${CP}" MindWaveClient
echo Done!
