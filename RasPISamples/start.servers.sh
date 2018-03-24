#!/bin/bash
#
cd raspberry-pi4j-samples
echo Starting multiplexer
cd NMEA.multiplexer
nohup ./mux.sh nmea.mux.weather.station.tcp.properties &
echo Starting Snap loop
cd ../RasPISamples
nohup ./snap.loop.sh &
echo Starting NavServer
cd ../RESTNavServer
nohup ./runNavServer &
cd ..
echo Done!
#
