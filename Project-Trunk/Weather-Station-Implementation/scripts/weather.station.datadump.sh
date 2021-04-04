#!/bin/bash
#
CP=../build/libs/Weather-Station-Implementation-1.0-all.jar
#
JAVA_OPTIONS=
JAVA_OPTIONS="$JAVA_OPTIONS -Dws.wspeed.coeff=1.0"
# JAVA_OPTIONS="$JAVA_OPTIONS -Ddebounce.time.millisec=30"
# data.logger is that class dealing with the read data
JAVA_OPTIONS="$JAVA_OPTIONS -Dsdl.weather.station.verbose=true"
JAVA_OPTIONS="$JAVA_OPTIONS -Ddata.logger=weatherstation.logger.DummyLogger"
# JAVA_OPTIONS="$JAVA_OPTIONS -client -agentlib:jdwp=transport=dt_socket,server=y,address=1044"
#
#echo Make sure the server is started \(node/weather.server.js\)
## ADDR=`hostname`
#ADDR=`ifconfig wlan0 2> /dev/null  | awk '/inet addr:/ {print $2}' | sed 's/addr://'`
#echo then from your browser, reach http://$ADDR:9876/data/weather.station/analog.html
#
sudo java $JAVA_OPTIONS -cp $CP weatherstation.ws.HomeWeatherStation
