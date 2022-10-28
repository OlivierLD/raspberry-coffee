#!/bin/bash
#
# Used to run the GUI Components tests.
#
CP=./build/libs/NMEA-Mux-Other-Clients-1.0-all.jar
#
OPTIONS=
# OPTIONS="-Dverbose=true -Dtcp.host=localhost -Dtcp.port=7001"
#
clear
echo -e "1: Clock Test "
echo -e "2: Heading Test "
echo -e "3: Speed Test "
echo -e "4: Direction Test "
echo -e "5: Jumbo Test "
echo -en "You choose > "
read RESP
CLASS=
case "${RESP}" in
	  "1")
	    CLASS=clients.components.ClockTest
	    ;;
	  "2")
	    CLASS=clients.components.HeadingTest
	    ;;
	  "3")
	    CLASS=clients.components.SpeedTest
	    ;;
	  "4")
	    CLASS=clients.components.DirectionTest
	    ;;
	  "5")
	    CLASS=clients.components.JumboTest
	    ;;
	  *)
	    CLASS=
	    echo -e "Option ${RESP} not implemented"
	    ;;
esac
#
if [[ "${CLASS}" != "" ]]; then
  java -cp ${CP} ${OPTIONS} ${CLASS}
else
  echo -e "Try again"
fi
