#!/bin/bash
# Interactive script to launch the RESTNavServer in several configurations.
# For demo and examples purpose.
# Describes different scenarios.
# Uses runNavServer.sh
# 
# For parameters --no-rmc-time --no-date : see in runNavServer.sh
#
HTTP_PORT=9999
#
LAUNCH_BROWSER=N
WITH_PROXY=N
USER_OPTION=
WITH_NOHUP=
export CMD_VERBOSE=N
# Program parameters
NAV_SERVER_EXTRA_OPTIONS=
#
if [[ $# -gt 0 ]]; then
	for prm in $*; do
	  echo "Processing ${prm} ..."
	  if [[ ${prm} == "--browser:"* ]]; then
	    LAUNCH_BROWSER=${prm#*:}
	  elif [[ ${prm} == "--http-port:"* ]]; then
	    HTTP_PORT=${prm#*:}
	  elif [[ ${prm} == "--nohup:"* ]]; then
	    WITH_NOHUP=${prm#*:}
	  elif [[ ${prm} == "--cmd-verbose:"* ]]; then
	    export CMD_VERBOSE=${prm#*:}
	  elif [[ ${prm} == "--proxy:"* ]]; then
	    WITH_PROXY=${prm#*:}
	    if [[ "${WITH_PROXY}" == "Y" ]] || [[ "${WITH_PROXY}" == "y" ]]; then
	      NAV_SERVER_EXTRA_OPTIONS="${NAV_SERVER_EXTRA_OPTIONS} --proxy"
	    fi
	  elif [[ ${prm} == "--option:"* ]]; then
	    USER_OPTION=${prm#*:}
	  else
	    echo "Unsupported parameter ${prm}"
	  fi
	done
fi
#
function openBrowser() {
  if [[ $(uname -s) == *Linux* ]]; then
    sensible-browser "$1"
  else
    open "$1"  # Darwin
  fi
}
#
function displayHelp() {
  echo -e "--------------------------------"
  echo -e "Option $1, property file is $2"
  cat $2
  echo -e "--------------------------------"
}
#
GO=true
#
# Banner done with https://manytools.org/hacker-tools/ascii-banner/, 'Slant Relief'
#
cat banner.txt
sleep 1
#
NAV_SERVER_EXTRA_OPTIONS="${NAV_SERVER_EXTRA_OPTIONS} --http-port:${HTTP_PORT}"
#
while [[ "${GO}" == "true" ]]; do
	clear
	echo -e ">> Note ⚠️ : Optional Script Parameters : "
	echo -e "    starting the server, like $0 --browser:[N]|Y --proxy:[N]|Y --option:1 --nohup:[N]|Y --http-port:9999 --cmd-verbose:[N]|Y"
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
	echo -e "|  1a. Time from a TCP ZDA generator (port 7002); HTTP Server, rich Web UI.               |"
	echo -e "|      Does not require a GPS                                                             |"
	echo -e "|  2. Interactive Time (user-set), HTTP Server, rich Web UI. Does not require a GPS       |"
	echo -e "|  3. Home Weather Station data                                                           |"
	echo -e "|  4. With GPS and NMEA data, waits for the RMC sentence to be active to begin logging    |"
	echo -e "|                     (Check your GPS connection setting in nmea.mux.gps.properties file) |"
	echo -e "|  5. Like option '1', but with 'Sun Flower' option                                       |"
	echo -e "|  6. Replay logged kayak data (Drakes Estero)                                            |"
	echo -e "|  6b. Replay logged kayak data (Ria d'Etel)                                              |"
	echo -e "|  7. Replay logged driving data (with a Maps)                                            |"
	echo -e "|  8. Replay logged kayak data, ANSI console display                                      |"
	echo -e "|  9. Replay logged sailing data (Bora-Bora - Tongareva), ANSI console display            |"
	echo -e "|  9b. Replay logged sailing data (China Camp - Oyster Point), ANSI console display       |"
	echo -e "|            (there is some current in that one, it's in the SF Bay)                      |"
	echo -e "|  9c. Replay logged sailing data (Nuku-Hiva - Rangiroa), ANSI console display            |"
	echo -e "|            (Big file)                                                                   |"
	echo -e "|  9d. Replay logged sailing data (Oyster Point), heading back in.                        |"
	echo -e "|  9e. Replay logged sailing data (Bora-Bora - Tongareva), forwarders TCP, WS, GPSd       |"
	echo -e "|            (requires a NodeJS WebSocket server to be running)                           |"
	echo -e "| 10. Full Nav Server Home Page. NMEA, Tides, Weather Wizard, Almanacs, etc. Data replay. |"
	echo -e "|     - See or modify nmea.mux.properties for details.                                    |"
	echo -e "| 11. Same as 10, with proxy.                                                             |"
	echo -e "|     - See or modify nmea.mux.properties for details.                                    |"
	echo -e "| 12. With 2 input serial ports.                                                          |"
	echo -e "|     - See or modify nmea.mux.2.serial.yaml for details. Or try option H:12              |"
	echo -e "| 13. AIS Tests.                                                                          |"
	echo -e "+-----------------------------------------------------------------------------------------+"
	echo -e "| 20. Get Data Cache (curl)                                                               |"
	echo -e "| 20b. Get REST operations list (curl)                                                    |"
	echo -e "+------------------------------------+----------------------------------------------------+"
	echo -e "| 21. Sample Python TCP Client                                                            |"
	echo -e "+------------------------------------+----------------------------------------------------+"
	echo -e "|  S. Show NavServer process(es) ⚙️   | SP. Show proxy process(es) ⚙️                       |"
	echo -e "|  K. Kill all running Multiplexers  |                                                    |"
	echo -e "+------------------------------------+----------------------------------------------------+"
	echo -e "|  >> To get help on option X, type H:X (like H:11, H:20b, etc)                           |"
	echo -e "+------------------------------------+----------------------------------------------------+"
	echo -e "|  Q. Quit ❎                        |                                                    |"
	echo -e "+------------------------------------+----------------------------------------------------+"
	if [[ "${USER_OPTION}" != "" ]]; then
	  echo -e "------------------------------"
	  echo -e ">> Using option ${USER_OPTION}"
	  echo -e "------------------------------"
	  option=${USER_OPTION}
	  USER_OPTION=
	else
  	echo -en " ==> You choose: "
	  read option
	fi
	case "${option}" in
	  "PG" | "pg")
	    export HTTP_PROXY_PORT=9876
	    java -cp ../build/libs/RESTNavServer-1.0-all.jar -Dhttp.port=${HTTP_PROXY_PORT} utils.proxyguisample.ProxyGUI &
	    echo -e "Make sure you use a proxy from your browser(s): Host: this machine, Port: ${HTTP_PROXY_PORT}"
	    echo -en "Hit [Return]"
	    read a
	    ;;
	  "P" | "p")
	    export HTTP_PROXY_PORT=9876
			JAVA_OPTIONS=
			JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.verbose=true"
			JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.verbose.dump=true"
			JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.client.verbose=true"
			#
			# JAVA_OPTIONS="${JAVA_OPTIONS} -Djava.util.logging.config.file=logging.properties"
			#
			java -cp ../build/libs/RESTNavServer-1.0-all.jar -Dhttp.port=${HTTP_PROXY_PORT} ${JAVA_OPTIONS} http.HTTPServer &
	    echo -e "Make sure you use a proxy from your browser(s): Host: this machine, Port: ${HTTP_PROXY_PORT}"
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
	  H:*)   # Help on options below
	    # echo "Start with H: ${option}"
	    HELP_ON=${option#*:}
	    echo -e "Required help on option ${HELP_ON}"
	    case "${HELP_ON}" in
	      "1")
	        PROP_FILE=nmea.mux.no.gps.yaml
	        displayHelp ${HELP_ON} ${PROP_FILE}
	        ;;
	      "1a")
	        PROP_FILE=nmea.mux.tcp.zda.yaml
	        displayHelp ${HELP_ON} ${PROP_FILE}
	        ;;
	      "2")
	        PROP_FILE=nmea.mux.interactive.time.properties
	        displayHelp ${HELP_ON} ${PROP_FILE}
	        ;;
	      "3")
	        PROP_FILE=nmea.mux.home.properties
	        displayHelp ${HELP_ON} ${PROP_FILE}
	        ;;
	      "4")
	        PROP_FILE=nmea.mux.gps.properties
	      	displayHelp ${HELP_ON} ${PROP_FILE}
	        ;;
	      "5")
	        PROP_FILE=nmea.mux.no.gps.properties
	      	displayHelp ${HELP_ON} ${PROP_FILE}
	        ;;
	      "6")
	        PROP_FILE=nmea.mux.kayak.log.properties
	      	displayHelp ${HELP_ON} ${PROP_FILE}
	        ;;
	      "6b")
	        PROP_FILE=nmea.mux.kayak.etel.yaml
	      	displayHelp ${HELP_ON} ${PROP_FILE}
	        ;;
	      "7")
	        PROP_FILE=nmea.mux.driving.log.properties
	      	displayHelp ${HELP_ON} ${PROP_FILE}
	        ;;
	      "8")
	        PROP_FILE=nmea.mux.kayak.cc.yaml
	      	displayHelp ${HELP_ON} ${PROP_FILE}
	        ;;
	      "9")
	        PROP_FILE=nmea.mux.bora.cc.yaml
	      	displayHelp ${HELP_ON} ${PROP_FILE}
	        ;;
	      "9b")
	        PROP_FILE=nmea.mux.cc.op.yaml
	      	displayHelp ${HELP_ON} ${PROP_FILE}
	        ;;
	      "9c")
	        PROP_FILE=nmea.mux.nh.r.yaml
	      	displayHelp ${HELP_ON} ${PROP_FILE}
	        ;;
	      "9d")
	        PROP_FILE=nmea.mux.heading.yaml
	      	displayHelp ${HELP_ON} ${PROP_FILE}
	        ;;
	      "9e")
	        PROP_FILE=nmea.mux.bora.fwd.yaml
	      	displayHelp ${HELP_ON} ${PROP_FILE}
	        ;;
	      "10")
	        PROP_FILE=nmea.mux.properties
	      	displayHelp ${HELP_ON} ${PROP_FILE}
	        ;;
	      "11")
	        PROP_FILE=nmea.mux.properties
	      	displayHelp ${HELP_ON} ${PROP_FILE}
	        ;;
	      "12")
	        PROP_FILE=nmea.mux.2.serial.yaml
	      	displayHelp ${HELP_ON} ${PROP_FILE}
	        ;;
	      "13")
	        PROP_FILE=nmea.mux.gps.ais.yaml
	      	displayHelp ${HELP_ON} ${PROP_FILE}
	        ;;
	      "20")
	      	echo -e "Uses a 'curl' to display the current data cache, using REST"
	      	COMMAND="curl -X GET localhost:${HTTP_PORT}/mux/cache"
	      	echo -e "Command is "
	      	echo -e "\t${COMMAND}"
	        ;;
	      "20b")
	      	echo -e "Uses a 'curl' to display the REST operations list, using REST"
	        COMMAND="curl -X GET http://localhost:9999/oplist"
	      	echo -e "Command is "
	      	echo -e "\t${COMMAND}"
	        ;;
	      "21")
	        echo -e "--------------------------------"
	        echo -e "Requires a MUX to be running, "
	        echo -e "Starts a Python sample client."
	        echo -e "--------------------------------"
	        ;;
	      *)
	        echo -e "No help implemented (yet) for option ${HELP_ON}"
	        ;;
	    esac
	    echo -en "Hit [Return]"
	    read a
	    ;;
	  "1")
  	  PROP_FILE=nmea.mux.no.gps.yaml
  	  NOHUP=""
  	  if [[ "${WITH_NOHUP}" == "Y" ]] || [[ "${WITH_NOHUP}" == "N" ]]; then
  	    if [[ "${WITH_NOHUP}" == "Y" ]]; then
  	      NOHUP="nohup "
  	      echo -e ">> Will use nohup"
  	    else
  	      NOHUP=""
  	      echo -e ">> Will not use nohup"
  	    fi
  	  else
  	    # Ask if nohup, just in this case
  	    echo -en " ==> Use nohup (y|n) ? > "
  	    read REPLY
        if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
          NOHUP="nohup "
          echo -e ">> Will use nohup"
        fi
  	  fi
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    # QUESTION: a 'screen' option ?
	    # screen -S navserver -dm "sleep 5; ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS}"
	    # echo -e "A screen session 'navserver' was started"
	    #
	    # bash -c "exec -a ProcessName Command"
	    if [[ "${CMD_VERBOSE}" == "Y" ]]; then
	      echo -e "Running command: [${NOHUP}./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &]"
	    fi
	    ${NOHUP}./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5  # Wait (5s) for the server to be operational
		    openBrowser "http://localhost:${HTTP_PORT}/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
	    fi
	    echo -e "Also try: curl -X GET http://localhost:${HTTP_PORT}/mux/cache | jq"
	    GO=false
	    ;;
	  "1a")
  	  PROP_FILE=nmea.mux.tcp.zda.yaml
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    # QUESTION: a 'screen' option ?
	    # screen -S navserver -dm "sleep 5; ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS}"
	    # echo -e "A screen session 'navserver' was started"
	    #
	    # bash -c "exec -a ProcessName Command"
	    if [[ "${CMD_VERBOSE}" == "Y" ]]; then
	      echo -e "Running command: [./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &]"
	    fi
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5  # Wait (5s) for the server to be operational
		    openBrowser "http://localhost:${HTTP_PORT}/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
	    fi
	    echo -e "Also try: curl -X GET http://localhost:${HTTP_PORT}/mux/cache | jq"
	    GO=false
	    ;;
	  "2")
	    PROP_FILE=nmea.mux.interactive.time.properties
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:${HTTP_PORT}/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
	    fi
	    GO=false
	    ;;
	  "3")
	    PROP_FILE=nmea.mux.home.properties
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    ./runNavServer.sh --mux:${PROP_FILE} ${NAV_SERVER_EXTRA_OPTIONS} &
	#   sleep 5 # Wait for the server to be operational
	#   openBrowser "http://localhost:${HTTP_PORT}/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
	    GO=false
	    ;;
	  "4")
	    PROP_FILE=nmea.mux.gps.properties
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5   # Wait for the server to be operational
		    openBrowser "http://localhost:${HTTP_PORT}/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y"
	    fi
	    GO=false
	    ;;
	  "5")
	    PROP_FILE=nmea.mux.no.gps.properties
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date --sun-flower ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    # openBrowser "http://localhost:${HTTP_PORT}/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
		    openBrowser "http://localhost:${HTTP_PORT}/web/sunflower/sun.data.html"
	    fi
	    GO=false
	    ;;
	  "6")
	    PROP_FILE=nmea.mux.kayak.log.properties
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    ./runNavServer.sh --mux:${PROP_FILE} --no-rmc-time --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:${HTTP_PORT}/web/index.html"
	    fi
	    GO=false
	    ;;
	  "6b")
	    PROP_FILE=nmea.mux.kayak.etel.yaml
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    ./runNavServer.sh --mux:${PROP_FILE} --no-rmc-time --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:${HTTP_PORT}/web/index.html"
	    fi
	    GO=false
	    ;;
	  "7")
	    PROP_FILE=nmea.mux.driving.log.properties
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    ./runNavServer.sh --mux:${PROP_FILE} --no-rmc-time --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    # openBrowser "http://localhost:${HTTP_PORT}/web/googlemaps.driving.html"
		    openBrowser "http://localhost:${HTTP_PORT}/web/leaflet.driving.html"
	    fi
	    GO=false
	    ;;
	  "8")
	    PROP_FILE=nmea.mux.kayak.cc.yaml
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    export INFRA_VERBOSE=false
	    # Get date and time from the file
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:${HTTP_PORT}/web/index.html"
	    fi
	    GO=false
	    ;;
	  "9")
	    PROP_FILE=nmea.mux.bora.cc.yaml
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    export INFRA_VERBOSE=false
	    # Get date and time from the file
	    ./runNavServer.sh --mux:${PROP_FILE} ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:${HTTP_PORT}/web/index.html"
	    fi
	    GO=false
	    ;;
	  "9b")
	    PROP_FILE=nmea.mux.cc.op.yaml
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    export INFRA_VERBOSE=false
	    # Get date and time from the file
	    ./runNavServer.sh --mux:${PROP_FILE} ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:${HTTP_PORT}/web/index.html"
	    fi
	    GO=false
	    ;;
	  "9c")
	    PROP_FILE=nmea.mux.nh.r.yaml
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    export INFRA_VERBOSE=false
	    # Get date and time from the file
	    ./runNavServer.sh --mux:${PROP_FILE} ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:${HTTP_PORT}/web/index.html"
	    fi
	    GO=false
	    ;;
	  "9d")
	    PROP_FILE=nmea.mux.heading.yaml
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    export INFRA_VERBOSE=false
	    # Get date and time from the file
	    ./runNavServer.sh --mux:${PROP_FILE} ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:${HTTP_PORT}/web/index.html"
	    fi
	    GO=false
	    ;;
	  "9e")
	    PROP_FILE=nmea.mux.bora.fwd.yaml
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    export INFRA_VERBOSE=false
	    # Get date and time from the file
	    ./runNavServer.sh --mux:${PROP_FILE} ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:${HTTP_PORT}/web/index.html"
	    fi
	    GO=false
	    ;;
	  "10")
	    PROP_FILE=nmea.mux.properties
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    # NAV_SERVER_EXTRA_OPTIONS="${NAV_SERVER_EXTRA_OPTIONS} --delta-t:AUTO:2010-11"
	    NAV_SERVER_EXTRA_OPTIONS="${NAV_SERVER_EXTRA_OPTIONS} --delta-t:AUTO"
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:${HTTP_PORT}/web/index.html"
	    fi
	    GO=false
	    ;;
	  "11")
	    PROP_FILE=nmea.mux.properties
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    ./runNavServer.sh --proxy --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:${HTTP_PORT}/web/index.html"
	    fi
	    GO=false
	    ;;
	  "12")
  	  PROP_FILE=nmea.mux.2.serial.yaml
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:${HTTP_PORT}/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
	    fi
	    GO=false
	    ;;
	  "13")
  	  # PROP_FILE=nmea.mux.ais.test.yaml
  	  # PROP_FILE=nmea.mux.ais.test.2.yaml
  	  PROP_FILE=nmea.mux.gps.ais.yaml
	    echo -e "Launching Nav Server with ${PROP_FILE}"
	    ./runNavServer.sh --mux:${PROP_FILE} --no-date ${NAV_SERVER_EXTRA_OPTIONS} &
	    if [[ "${LAUNCH_BROWSER}" == "Y" ]]; then
		    echo -e ">>> Waiting for the server to start..."
		    sleep 5 # Wait for the server to be operational
		    openBrowser "http://localhost:${HTTP_PORT}/web/nmea/admin.html"
	    fi
	    GO=false
	    ;;
	  "20")
	    COMMAND="curl -X GET localhost:${HTTP_PORT}/mux/cache"
	    if [[ "$(which jq)" != "" ]]; then
	      ${COMMAND} | jq
	    else
	      ${COMMAND}
	    fi
      echo -e "\nHit [Return]"
      read resp
	    ;;
	  "20b")
	    COMMAND="curl -X GET http://localhost:9999/oplist"
	    if [[ "$(which jq)" != "" ]]; then
	      ${COMMAND} | jq
	    else
	      ${COMMAND}
	    fi
      echo -e "\nHit [Return]"
      read resp
	    ;;
	  "21")
	    echo -e "This requires a Multiplexer to be running, and forwarding data on a TCP Port"
	    echo -en " ==> Enter Multiplexer machine name or IP (default 'localhost'): "
      read MACHINE_NAME
      if [[ "${MACHINE_NAME}" == "" ]]; then
        MACHINE_NAME=localhost
        echo -e "Defaulting machine name to ${MACHINE_NAME}"
      fi
	    echo -en " ==> Enter Multiplexer TCP port (default 7001): "
      read TCP_PORT
      if [[ "${TCP_PORT}" == "" ]]; then
        TCP_PORT=7001
        echo -e "Defaulting TCP port to ${TCP_PORT}"
      fi
	    echo -en " ==> With verbose option (default false): "
      read VERBOSE
      if [[ "${VERBOSE}" == "" ]]; then
        VERBOSE=false
        echo -e "Defaulting verbose to ${VERBOSE}"
      fi
      if [[ ${VERBOSE} =~ ^(yes|y|Y)$ ]]; then
        VERBOSE=true
      fi
      #
      pushd other-clients/python
	    COMMAND="python3 tcp_mux_client.py --machine-name:${MACHINE_NAME} --port:${TCP_PORT} --verbose:${VERBOSE}"
	    ${COMMAND}
	    popd
      echo -e "\nHit [Return]"
      read resp
	    ;;
	# Others...
	    # ;;
	  "S" | "s")
	    echo -e "Nav Server processes:"
	    ps -ef | grep navrest.NavServer | grep -v grep
	    ps -ef | grep navrest.NavServer | grep -v grep | grep -v killns | awk '{ print $2 }' > km
			NB_L=$(cat km | wc -l)
			if [[ ${NB_L} == 0 ]]; then
			  echo -e "No NavServer process found."
			else
			  echo -e "----------- NavServer HTTP Ports ---------"
        if [[ $(uname -a) == *Linux* ]]; then
            # Could use sudo below
            netstat -tunap | grep ${HTTP_PORT}
				else
          for pid in $(cat km); do
            netstat -vanp tcp | grep ${pid} | grep LISTEN
          done
				fi
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
	  "K" | "k")
	    ./killns.sh
	    #
	    sleep 5  # Wait for the kill to be completed.
	    #
	    echo -en "Hit [return]"
	    read ret
	    ;;
	  "Q" | "q")
	    GO=false
	    ;;
	  *)
	    echo -e "What? Unknown option [${option}]"
	    echo -en "Hit [return]"
	    read ret
	    ;;
	esac
done
#
#
echo -e "Bye now. See you ✋"
#
