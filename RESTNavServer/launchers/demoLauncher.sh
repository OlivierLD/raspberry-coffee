#!/bin/bash
# Describes the different scenarios
# Uses runNavServer.sh
# 
# Parameters --no-rmc-time --no-date : see in runNavServer.sh
#
LAUNCH_BROWSER=N
WITH_PROXY=N
USER_OPTION=
WITH_NOHUP=
#
NAV_SERVER_EXTRA_OPTIONS=
#
if [[ $# -gt 0 ]]
then
	for prm in $*
	do
	  echo "Processing $prm ..."
	  if [[ ${prm} == "--browser:"* ]]
	  then
	    LAUNCH_BROWSER=${prm#*:}
	  elif [[ ${prm} == "--nohup:"* ]]
	  then
	    WITH_NOHUP=${prm#*:}
	  elif [[ ${prm} == "--proxy:"* ]]
	  then
	    WITH_PROXY=${prm#*:}
	    if [[ "$WITH_PROXY" == "Y" ]] || [[ "$WITH_PROXY" == "y" ]]
	    then
	      NAV_SERVER_EXTRA_OPTIONS="$NAV_SERVER_EXTRA_OPTIONS --proxy"
	    fi
	  elif [[ ${prm} == "--option:"* ]]
	  then
	    USER_OPTION=${prm#*:}
	  else
	    echo "Unsupported parameter $prm"
	  fi
	done
fi
#
function openBrowser() {
  if [[ `uname -a` == *Linux* ]]
  then
    sensible-browser "$1"
  else
    open "$1"  # Darwin
  fi
}
#
GO=true
#
# Banner done with https://manytools.org/hacker-tools/ascii-banner/, 'Slant Relief'
#
cat banner.txt
sleep 1
#
while [[ "$GO" == "true" ]]
do
	clear
	echo -e ">> Note ⚠️ : Optional Script Parameters : "
	echo -e "    starting the server, like $0 --browser:[N]|Y --proxy:[N]|Y --option:1 --nohup:[N]|Y"
	echo -e "    --option:X will not prompt the user for his choice, it will go directly for it."
	echo -e "    --nohup:Y will launch some commands with nohup (see the script for details)"
	echo -e "+-----------------------------------------------------------------------------------------+"
	echo -e "|               N A V   S E R V E R   -   D E M O   L A U N C H E R  🚀                   |"
	echo -e "+-----------------------------------------------------------------------------------------+"
	echo -e "|  P. Launch proxy CLI, to visualize HTTP & REST traffic 🔎                               |"
	echo -e "| PG. Launch proxy GUI, to visualize HTTP & REST traffic 🕵️‍                                |"
	echo -e "+------------------------------------+----------------------------------------------------+"
	echo -e "|  J. JConsole (JVM Monitoring) 📡   |  JV. JVisualVM 📡                                  |"
	echo -e "|                                    | - Note: for remote monitoring, jstatd must be      |"
	echo -e "|                                    |         running on the remote machine.             |"
	echo -e "|                                    |     Enter 'JVH' for some help.                     |"
	echo -e "+------------------------------------+----------------------------------------------------+"
	echo -e "| >> Hint: use './killns.sh' to stop any running NavServer 💣                             |"
	echo -e "| >> Hint: use './killproxy.sh' to stop any running Proxy Server 💣                       |"
	echo -e "+-----------------------------------------------------------------------------------------+"
	echo -e "|  1. Time simulated by a ZDA generator; HTTP Server, rich Web UI. Does not require a GPS |"
	echo -e "|  2. Interactive Time (user-set), HTTP Server, rich Web UI. Does not require a GPS       |"
	echo -e "|  3. Home Weather Station data                                                           |"
	echo -e "|  4. With GPS and NMEA data, waits for the RMC sentence to be active to begin logging    |"
	echo -e "|                     (Check your GPS connection setting in nmea.mux.gps.properties file) |"
	echo -e "|  5. Like option '1', but with 'Sun Flower' option                                       |"
	echo -e "|  6. Replay logged kayak data                                                            |"
	echo -e "|  7. Replay logged driving data (in Google Maps)                                         |"
	echo -e "|  8. Replay logged kayak data, ANSI console display                                      |"
	echo -e "|  9. Replay logged sailing data (Bora-Bora - Tongareva), ANSI console display            |"
	echo -e "|  9b. Replay logged sailing data (China Camp - Oyster Point), ANSI console display       |"
	echo -e "|            (there is ome current in that one, it's in the SF Bay)                       |"
	echo -e "| 10. Full Nav Server Home Page. NMEA, Tides, Weather Wizard, Almanacs, etc. Data replay. |"
	echo -e "|     - See or modify nmea.mux.properties for details.                                    |"
	echo -e "| 10b. Full Nav Server Home Page. NMEA, Tides, Weather Wizard, Almanacs, etc.             |"
	echo -e "|     - Replaying data from China Camp to Oyster Point.                                   |"
	echo -e "| 11. Same as 10, with proxy.                                                             |"
	echo -e "|     - See or modify nmea.mux.properties for details.                                    |"
	echo -e "| 12. With 2 input serial ports.                                                          |"
	echo -e "|     - See or modify nmea.mux.2.serial.yaml for details.                                 |"
	echo -e "| 13. AIS Tests.                                                                          |"
	echo -e "+------------------------------------+----------------------------------------------------+"
	echo -e "| 20. Get Data Cache (curl)                                                               |"
	echo -e "+------------------------------------+----------------------------------------------------+"
	echo -e "|  S. Show NavServer process(es) ⚙️   | SP. Show proxy process(es) ⚙️                       |"
	echo -e "|  Q. Quit ❎                        |                                                    |"
	echo -e "+------------------------------------+----------------------------------------------------+"
	if [[ "${USER_OPTION}" != "" ]]
	then
	  echo -e "------------------------------"
	  echo -e ">> Using option ${USER_OPTION}"
	  echo -e "------------------------------"
	  option=${USER_OPTION}
	  USER_OPTION=
	else
  	echo -en " ==> You choose: "
	  read option
	fi
	case "$option" in
	  "PG" | "pg")
	    export HTTP_PROXY_PORT=9876
	    java -cp ../build/libs/RESTNavServer-1.0-all.jar -Dhttp.port=${HTTP_PROXY_PORT} utils.proxyguisample.ProxyGUI &
	    echo -e "Make sure you use a proxy from your browser(s): Host: this machine, Port: $HTTP_PROXY_PORT"
	    echo -en "Hit [Return]"
	    read a
	    ;;
	  "P" | "p")
	    export HTTP_PROXY_PORT=9876
			JAVA_OPTIONS=
			JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.verbose=true"
			JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.verbose.dump=true"
			JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.client.verbose=true"
			#
			# JAVA_OPTIONS="$JAVA_OPTIONS -Djava.util.logging.config.file=logging.properties"
			#
			java -cp ../build/libs/RESTNavServer-1.0-all.jar -Dhttp.port=${HTTP_PROXY_PORT} ${JAVA_OPTIONS} http.HTTPServer &
	    echo -e "Make sure you use a proxy from your browser(s): Host: this machine, Port: $HTTP_PROXY_PORT"
	    echo -en "Hit [Return]"
	    read a
	    ;;
	  "J" | "j")
	    jconsole &
	    ;;
	  "JV" | "jv")
	    jvisualvm &
	    ;;
	  "JVH" | "jvh")
	    echo "More here soon..."
	    echo -en "Hit [Return]"
	    read a
	    ;;
	  "1")
  	  PROP_FILE=nmea.mux.no.gps.yaml
  	  NOHUP=""
  	  if [[ "${WITH_NOHUP}" == "Y" ]]
  	  then
  	    NOHUP="nohup "
  	    echo -e ">> Will use nohup"
  	  fi
	    echo -e "Launching Nav Server with $PROP_FILE"
	    ${NOHUP}./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "$LAUNCH_BROWSER" == "Y" ]]
	    then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:9999/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
	    fi
	    GO=false
	    ;;
	  "2")
	    PROP_FILE=nmea.mux.interactive.time.properties
	    echo -e "Launching Nav Server with $PROP_FILE"
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "$LAUNCH_BROWSER" == "Y" ]]
	    then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:9999/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
	    fi
	    GO=false
	    ;;
	  "3")
	    PROP_FILE=nmea.mux.home.properties
	    echo -e "Launching Nav Server with $PROP_FILE"
	    ./runNavServer.sh --mux:${PROP_FILE} ${NAV_SERVER_EXTRA_OPTIONS} &
	#   sleep 5 # Wait for the server to be operational
	#   openBrowser "http://localhost:9999/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
	    GO=false
	    ;;
	  "4")
	    PROP_FILE=nmea.mux.gps.properties
	    echo -e "Launching Nav Server with $PROP_FILE"
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "$LAUNCH_BROWSER" == "Y" ]]
	    then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5   # Wait for the server to be operational
		    openBrowser "http://localhost:9999/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y"
	    fi
	    GO=false
	    ;;
	  "5")
	    PROP_FILE=nmea.mux.no.gps.properties
	    echo -e "Launching Nav Server with $PROP_FILE"
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date --sun-flower ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "$LAUNCH_BROWSER" == "Y" ]]
	    then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    # openBrowser "http://localhost:9999/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
		    openBrowser "http://localhost:9999/web/sunflower/sun.data.html"
	    fi
	    GO=false
	    ;;
	  "6")
	    PROP_FILE=nmea.mux.kayak.log.properties
	    echo -e "Launching Nav Server with $PROP_FILE"
	    ./runNavServer.sh --mux:${PROP_FILE} --no-rmc-time --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "$LAUNCH_BROWSER" == "Y" ]]
	    then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:9999/web/index.html"
	    fi
	    GO=false
	    ;;
	  "7")
	    PROP_FILE=nmea.mux.driving.log.properties
	    echo -e "Launching Nav Server with $PROP_FILE"
	    ./runNavServer.sh --mux:${PROP_FILE} --no-rmc-time --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "$LAUNCH_BROWSER" == "Y" ]]
	    then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    # openBrowser "http://localhost:9999/web/googlemaps.driving.html"
		    openBrowser "http://localhost:9999/web/leaflet.driving.html"
	    fi
	    GO=false
	    ;;
	  "8")
	    PROP_FILE=nmea.mux.kayak.cc.yaml
	    echo -e "Launching Nav Server with $PROP_FILE"
	    export INFRA_VERBOSE=false
	    # Get date and time from the file
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "$LAUNCH_BROWSER" == "Y" ]]
	    then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:9999/web/index.html"
	    fi
	    GO=false
	    ;;
	  "9")
	    PROP_FILE=nmea.mux.bora.cc.yaml
	    echo -e "Launching Nav Server with $PROP_FILE"
	    export INFRA_VERBOSE=false
	    # Get date and time from the file
	    ./runNavServer.sh --mux:${PROP_FILE} ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "$LAUNCH_BROWSER" == "Y" ]]
	    then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:9999/web/index.html"
	    fi
	    GO=false
	    ;;
	  "9b")
	    PROP_FILE=nmea.mux.cc.op.yaml
	    echo -e "Launching Nav Server with $PROP_FILE"
	    export INFRA_VERBOSE=false
	    # Get date and time from the file
	    ./runNavServer.sh --mux:${PROP_FILE} ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "$LAUNCH_BROWSER" == "Y" ]]
	    then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:9999/web/index.html"
	    fi
	    GO=false
	    ;;
	  "10")
	    PROP_FILE=nmea.mux.properties
	    echo -e "Launching Nav Server with $PROP_FILE"
	    # NAV_SERVER_EXTRA_OPTIONS="${NAV_SERVER_EXTRA_OPTIONS} --delta-t:AUTO:2010-11"
	    NAV_SERVER_EXTRA_OPTIONS="${NAV_SERVER_EXTRA_OPTIONS} --delta-t:AUTO"
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "$LAUNCH_BROWSER" == "Y" ]]
	    then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:9999/web/index.html"
	    fi
	    GO=false
	    ;;
	  "10b")
	    PROP_FILE=nmea.mux.op-cc.yaml
	    echo -e "Launching Nav Server with $PROP_FILE"
	    # NAV_SERVER_EXTRA_OPTIONS="${NAV_SERVER_EXTRA_OPTIONS} --delta-t:AUTO:2012-06"
	    NAV_SERVER_EXTRA_OPTIONS="${NAV_SERVER_EXTRA_OPTIONS} --delta-t:AUTO"
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "$LAUNCH_BROWSER" == "Y" ]]
	    then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:9999/web/index.html"
	    fi
	    GO=false
	    ;;
	  "11")
	    PROP_FILE=nmea.mux.properties
	    echo -e "Launching Nav Server with $PROP_FILE"
	    ./runNavServer.sh --proxy --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "$LAUNCH_BROWSER" == "Y" ]]
	    then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:9999/web/index.html"
	    fi
	    GO=false
	    ;;
	  "12")
  	  PROP_FILE=nmea.mux.2.serial.yaml
	    echo -e "Launching Nav Server with $PROP_FILE"
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "$LAUNCH_BROWSER" == "Y" ]]
	    then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:9999/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
	    fi
	    GO=false
	    ;;
	  "13")
  	  # PROP_FILE=nmea.mux.ais.test.yaml
  	  # PROP_FILE=nmea.mux.ais.test.2.yaml
  	  PROP_FILE=nmea.mux.gps.ais.yaml
	    echo -e "Launching Nav Server with $PROP_FILE"
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "$LAUNCH_BROWSER" == "Y" ]]
	    then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:9999/web/nmea/admin.html"
	    fi
	    GO=false
	    ;;
	  "20")
	    curl -X GET localhost:9999/mux/cache 
		echo -e "\nHit [Return]"
		read resp
	    ;;	
	  "S" | "s")
	    echo -e "Nav Server processes:"
	    ps -ef | grep navrest.NavServer | grep -v grep
	    ps -ef | grep navrest.NavServer | grep -v grep | grep -v killns | awk '{ print $2 }' > km
			NB_L=`cat km | wc -l`
			if [[ ${NB_L} == 0 ]]
			then
			  echo -e "No NavServer process found."
			else
			  echo -e "----------- NavServer HTTP Ports ---------"
				for pid in `cat km`
				do
				  netstat -vanp tcp | grep ${pid} | grep LISTEN
				done
			  echo -e "------------------------------------------"
				rm km
			fi
	    echo -en "Hit [return]"
	    read ret
	    ;;
	  "SP" | "sp")
	    echo -e "Proxy processes:"
	    ps -ef | grep ProxyGUI | grep -v grep
	    ps -ef | grep HTTPServer | grep -v grep
	    #
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
echo -e "Bye now. See you ✋"
#
