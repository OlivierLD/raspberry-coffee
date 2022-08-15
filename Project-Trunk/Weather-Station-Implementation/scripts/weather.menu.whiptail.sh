#!/bin/bash
#
# whiptail on Mac OS: brew install newt
#
clear
#
echo -e "Instead of this one, use the script in the node folder..."
exit 1
#
while [ 1 ]; do
  CHOICE=$(
   whiptail --title "Weather Station" --menu "Choose option" 16 100 9 \
	"1" "Start Node.js server."   \
	"2" "Start Weather Station reader."  \
	"3" "Show processes." \
	"9" "Quit"  3>&2 2>&1 1>&3
  )

  result=$(whoami)
  case $CHOICE in
	"1")
		cd ../node
		if [[ -f node.log ]]; then
      rm node.log
    fi
    nohup node weather.server.js > node.log &
    cd ..
		result="Log is node/node.log."
		;;
	"2")
	  MESSAGE="Make sure you have started the WebSocket server (Option 1)."
	  if [[ -f weather.station.log ]]; then
      rm weather.station.log
    fi
    # ./weather.station.reader.sh > weather.station.log
    nohup ./weather.station.reader.sh > weather.station.log &
    # ADDR=`ifconfig wlan0 2> /dev/null  | awk '/inet addr:/ {print $2}' | sed 's/addr://'`
    ADDR=`hostname -I`
    MESSAGE="$MESSAGE\nthen from your browser, reach http://$ADDR:9876/data/weather.station/analog.all.html"
    MESSAGE="$MESSAGE\nIP is $(hostname -I)"
    MESSAGE="$MESSAGE\nLog is in weather.station.log."
		result=$MESSAGE
		;;
	"3")
		MESSAGE=
	  PID=`ps -ef | grep -v grep | grep weatherstation.ws.HomeWeatherStation | awk '{ print $2 }'`
    if [[ "$PID" != "" ]]; then
      MESSAGE="HomeWeatherStation $PID"
    else
      MESSAGE="Found no HomeWeatherStation..."
    fi
    PID=`ps -ef | grep -v grep | grep node-weather | awk '{ print $2 }'`
    if [[ "$PID" != "" ]]; then
      MESSAGE="$MESSAGE\nNode server $PID"
    else
      MESSAGE="$MESSAGE\nFound no node-weather..."
    fi
    result=$MESSAGE
		read -r result < result
    ;;
	"9")
		exit
    ;;
  esac
  whiptail --msgbox "$result" 20 78
done
exit
