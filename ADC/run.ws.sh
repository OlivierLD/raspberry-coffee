#!/bin/bash
echo Read an ADC, feed a WebSocket
#
CP=./build/libs/ADC-1.0-all.jar
#
sudo java -cp $CP -Dws.uri=ws://localhost:9876/ adc.sample.WebSocketFeeder $*
