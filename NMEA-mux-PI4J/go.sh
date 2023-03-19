#!/bin/bash
rm nohup.out > /dev/null
echo -e "Dump Serial port"
# nohup ./mux.sh nmea.mux.gps.log.properties &
./mux.sh nmea.mux.gps.log.properties
echo On its way!
