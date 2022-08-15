#!/bin/bash
#
# Stop the processes using kill.servers.sh
#
STARTED=`ps -ef | grep EmailWatcher | grep -v grep | awk '{ print $2 }'`
if [[ "$STARTED" == "" ]]; then
  cd raspberry-coffee
  echo Starting multiplexer
  cd NMEA-multiplexer
  nohup ./mux.sh nmea.mux.weather.station.tcp.properties &
  echo Starting Snap loop
  cd ../RasPISamples
  nohup ./snap.loop.sh &
  nohup ./email.watcher.sh -send:google -receive:google &
  echo Starting NavServer
  cd ../RESTNavServer
  nohup ./runNavServer &
  cd ..
  echo Done!
else
  echo -e "Processes already started".
fi
#
