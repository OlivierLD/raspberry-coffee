#!/usr/bin/env bash
#
# Use this to start the weather reader on the Weather Station,
# the one connected to the SDLWeather80422
#
# Use it with ssh:
# ssh pi@192.148.42.13 bash -s < ./start.weather.servers.sh
#
echo "Starting node server"
#
cd node
sudo rm node.log
nohup node weather.server.js > node.log &
cd ..
#
echo "Starting node weather reader"
#
sudo rm weather.station.log
#
nohup ./weather.station.reader.sh > weather.station.log &
ADDR=`ifconfig wlan0 2> /dev/null  | awk '/inet addr:/ {print $2}' | sed 's/addr://'`
echo then from your browser, reach http://$ADDR:9876/data/weather.station/analog.all.html
echo IP is $(hostname -I)
#
echo "Done"
#
# To see the processes:
#
PID=`ps -ef | grep -v grep | grep weatherstation.ws.HomeWeatherStation | awk '{ print $2 }'`
if [[ "$PID" != "" ]]; then
  echo -e "HomeWeatherStation $PID"
else
  echo Found no HomeWeatherStation...
fi
PID=`ps -ef | grep -v grep | grep node-weather | awk '{ print $2 }'`
if [[ "$PID" != "" ]]; then
  echo -e "Node server $PID"
else
  echo Found no node-weather...
fi
#
