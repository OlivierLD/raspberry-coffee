#!/bin/bash
if [[ "${PI4J_HOME}" = "" ]]; then
  PI4J_HOME=/opt/pi4j
fi
echo Running...
java -cp classes:${PI4J_HOME}/lib/pi4j-core.jar:../RasPISamples/lib/jansi-1.9.jar mindwave.samples.pi.ClientTwo
echo Done!
