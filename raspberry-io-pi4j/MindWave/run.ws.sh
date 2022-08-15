#!/bin/bash
if [[ "${PI4J_HOME}" == "" ]]; then
  PI4J_HOME=/opt/pi4j
fi
echo Running...
CP=classes
CP=${CP}:${PI4J_HOME}/lib/pi4j-core.jar
CP=${CP}:lib/json.jar
CP=${CP}:lib/jansi-1.9jar
CP=${CP}:lib/java_websocket.jar
#
OPT=-Dws.uri=ws://192.168.1.77:9876/
#
java -cp ${CP} $OPT mindwave.samples.pi.ws.WebSocketFeeder
echo Done!
