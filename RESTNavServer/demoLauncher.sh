#!/bin/bash
# Describes the different scenarios
# Uses runNavServer.sh
#
function openBrowser() {
  if [[ `uname -a` == *Linux* ]]
  then
    sensible-browser "$1"
  else
    open "$1"
  fi
}
#
GO=true
while [ "$GO" == "true" ]
do
	clear
	echo -e "+-----------------------------------------------------------------------------------------+"
	echo -e "+------------------------------ D E M O   L A U N C H E R  🚀 ----------------------------+"
	echo -e "+-----------------------------------------------------------------------------------------+"
	echo -e "|  1. Time simulated by a ZDA generator, HTTP Server, rich Web UI. Does not require a GPS |"
	echo -e "|  2. Home Weather Station data                                                           |"
	echo -e "|  3. With GPS, waits for the RMC sentence to be active                                   |"
	echo -e "|  ... TODO: more.                                                                        |"
	echo -e "| 10. Full Nav Server Home Page. NMEA, Tides, Weather Wizard, Almanachs, etc              |"
	echo -e "+-----------------------------------------------------------------------------------------+"
	echo -e "|  S. Show NavServer process(es)                                                          |"
	echo -e "|  Q. Quit                                                                                |"
	echo -e "+-----------------------------------------------------------------------------------------+"
	echo -en " ==> You choose: "
	read option
	echo -e " >> Hint: use 'killns.sh' to stop any running NavServer"
	case "$option" in
	  "1")
	    PROP_FILE=nmea.mux.no.gps.properties
	    echo -e "Launching Nav Server with $PROP_FILE"
	    ./runNavServer.sh --mux:$PROP_FILE --no-date &
	    echo -e "Waiting for the server to start..."
	    sleep 5 # Wait for the server to be operational
	    openBrowser "http://localhost:9999/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
	    GO=false
	    ;;
	  "2")
	    PROP_FILE=nmea.mux.home.properties
	    echo -e "Launching Nav Server with $PROP_FILE"
	    ./runNavServer.sh --mux:$PROP_FILE &
	#   sleep 5 # Wait for the server to be operational
	#   openBrowser "http://localhost:9999/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
	    GO=false
	    ;;
	  "3")
	    PROP_FILE=nmea.mux.gps.properties
	    echo -e "Launching Nav Server with $PROP_FILE"
	    ./runNavServer.sh --mux:$PROP_FILE &
	    echo -e "Waiting for the server to start..."
	    sleep 5 # Wait for the server to be operational
	    openBrowser "http://localhost:9999/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y"
	    GO=false
	    ;;
	  "10")
	    PROP_FILE=nmea.mux.properties
	    echo -e "Launching Nav Server with $PROP_FILE"
	    ./runNavServer.sh --mux:$PROP_FILE &
	    echo -e "Waiting for the server to start..."
	    sleep 5 # Wait for the server to be operational
	    openBrowser "http://localhost:9999/web/index.html"
	    GO=false
	    ;;
	  "S" | "s")
	    echo -e "Nav Server processes:"
	    ps -ef | grep NavServer | grep -v grep
	    ps -ef | grep NavServer | grep -v grep | grep -v killns | awk '{ print $2 }' > km
			NB_L=`cat km | wc -l`
			if [ $NB_L == 0 ]
			then
			  echo -e "No NavServer process found."
			else
			  echo -e "----------- NavServer HTTP Ports ---------"
				for pid in `cat km`
				do
				  netstat -vanp tcp | grep $pid | grep LISTEN
				done
			  echo -e "------------------------------------------"
				rm km
			fi
	    echo -en "Hit [return]"
	    read ret
	    ;;
	  "Q" | "q")
	    GO=false
	    ;;
	  *)
	    echo -e "What? Unknown option [$option]"
	    echo -en "Hit [return]"
	    read ret
	    ;;
	esac
done
#
#
echo -e "Bye now ✋"
#
