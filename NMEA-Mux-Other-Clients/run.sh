#!/bin/bash
#
# Used to run the Java TCP Clients.
#
CP=./build/libs/NMEA-Mux-Other-Clients-1.0-all.jar
#
OPTIONS=
VERBOSE=false
TCP_HOST=localhost
TCP_PORT=7001
# OPTIONS="-Dverbose=true -Dtcp.host=192.168.1.106 -Dtcp.port=7001"
#
KEEP_WORKING=true
while [[ "${KEEP_WORKING}" == "true" ]]; do
  clear
  echo -e "verbose: ${VERBOSE}"
  echo -e "TCP Host: ${TCP_HOST}, port: ${TCP_PORT}"
  echo -e "+------------------------+"
  echo -e "|   Java/Swing clients   |"
  echo -e "+------------------------+"
  echo -e "| 1: Raw TCP Client      |"
  echo -e "| 2: Basic Swing UI      |"
  echo -e "| 3: Swing Compass       |"
  echo -e "| 4: Multiple Displays   |"
  echo -e "+------------------------+"
  echo -e "| C: Change options (TCP)|"
  echo -e "| Q: Quit                |"
  echo -e "+------------------------+"
  echo -en "You choose > "
  read RESP
  CLASS=
  case "${RESP}" in
      "1")
        CLASS=clients.tcp.raw.RawNMEATCPClient
        KEEP_WORKING=false
        ;;
      "2")
        CLASS=clients.tcp.swing.NMEATCPSwing101
        KEEP_WORKING=false
        ;;
      "3")
        CLASS=clients.tcp.swing.NMEATCPSwingHeading
        KEEP_WORKING=false
        ;;
      "4")
        CLASS=clients.tcp.swing.NMEATCPSwingMultiDisplay
        KEEP_WORKING=false
        ;;
      "C" | "c")
        echo -en "Verbose (default ${VERBOSE}) > "
        read OPT
        if [[ "${OPT}" != "" ]]; then
          VERBOSE=${OPT}
        fi
        echo -en "TCP Host (default ${TCP_HOST}) > "
        read OPT
        if [[ "${OPT}" != "" ]]; then
          TCP_HOST=${OPT}
        fi
        echo -en "TCP port (default ${TCP_PORT}) > "
        read OPT
        if [[ "${OPT}" != "" ]]; then
          TCP_PORT=${OPT}
        fi
        ;;
      "Q" | "q")
        KEEP_WORKING=false
        ;;
      *)
        CLASS=
        echo -e "Option ${RESP} not implemented"
        KEEP_WORKING=false
        ;;
  esac
done
#
OPTIONS="-Dverbose=${VERBOSE} -Dtcp.host=${TCP_HOST} -Dtcp.port=${TCP_PORT}"
if [[ "${CLASS}" != "" ]]; then
  COMMAND="java -cp ${CP} ${OPTIONS} ${CLASS}"
  echo -e "Executing ${COMMAND}"
  ${COMMAND}
else
  if [[ ! ${RESP} =~ ^(Q|q)$ ]]; then
    echo -e "Try again"
  fi
fi
