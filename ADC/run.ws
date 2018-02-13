#!/bin/bash
echo Read an ADC, feed a WebSocket
#
#CP=./classes
#CP=$CP:$PI4J_HOME/lib/pi4j-core.jar
#CP=$CP:./lib/Java-WebSocket-1.3.0.jar
CP=./build/libs/ADC-1.0-all.jar
#
sudo java -cp $CP -Dws.uri=ws://localhost:9876/ adc.sample.WebSocketFeeder $*
