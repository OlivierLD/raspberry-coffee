#!/bin/bash
#
# Used to run the TCP Clients.
#
CP=./build/libs/NMEA-Mux-Other-Clients-1.0-all.jar
OPTIONS=
# OPTIONS="-Dverbose=true -Dtcp.host=localhost -Dtcp.port=7001"
#
clear
echo -e "1: Raw TCP Client "
echo -e "2: Basic Swing UI "
echo -e "3: Swing Compass "
echo -en "You choose > "
read RESP
CLASS=
case "${RESP}" in
	  "1")
	    CLASS=clients.tcp.raw.RawNMEATCPClient
	    ;;
	  "2")
	    CLASS=clients.tcp.swing.NMEATCPSwing101
	    ;;
	  "3")
	    CLASS=clients.tcp.swing.NMEATCPSwingHeading
	    ;;
	  *)
	    CLASS=
	    echo -e "Option ${RESP} not implemented"
	    ;;
esac

# java -cp ${CP} ${OPTIONS} clients.tcp.RawNMEATCPClient
# java -cp ${CP} ${OPTIONS} clients.tcp.NMEATCPSwing101
if [[ "${CLASS}" != "" ]]; then
  java -cp ${CP} ${OPTIONS} ${CLASS}
fi
