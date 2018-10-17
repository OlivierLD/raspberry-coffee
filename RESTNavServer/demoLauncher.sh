#!/bin/bash
# Describes the different scenarios
# Uses runNavServer.sh
#
echo -e "+-----------------------------------------------------------------------------------------+"
echo -e "+------------------------------ D E M O   L A U N C H E R  🚀 ----------------------------+"
echo -e "+-----------------------------------------------------------------------------------------+"
echo -e "|  1. Time simulated by a ZDA generator, HTTP Server, rich Web UI. Does not require a GPS |"
echo -e "|  2. Home Weather Station data                                                           |"
echo -e "|  3. With GPS, waits for the RMC sentence to be active                                   |"
echo -e "|  ... TODO: more.                                                                        |"
echo -e "| 10. Full Nav Server Home Page                                                           |"
echo -e "+-----------------------------------------------------------------------------------------+"
echo -en " ==> You choose: "
read option
echo -e " >> Hint: use 'killns.sh' to stop any running NavServer"
case "$option" in
  "1")
    PROP_FILE=nmea.mux.no.gps.properties
    echo -e "Launching Nav Server with $PROP_FILE"
    ./runNavServer.sh --mux:$PROP_FILE --no-date &
    sleep 5 # Wait for the server to be operational
    open "http://localhost:9999/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
    ;;
  "2")
    PROP_FILE=nmea.mux.home.properties
    echo -e "Launching Nav Server with $PROP_FILE"
    ./runNavServer.sh --mux:$PROP_FILE &
#   sleep 5 # Wait for the server to be operational
#   open "http://localhost:9999/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y&boat-data=n"
    ;;
  "3")
    PROP_FILE=nmea.mux.gps.properties
    echo -e "Launching Nav Server with $PROP_FILE"
    ./runNavServer.sh --mux:$PROP_FILE &
    sleep 5 # Wait for the server to be operational
    open "http://localhost:9999/web/webcomponents/console.gps.html?style=flat-gray&bg=black&border=y"
    ;;
  "10")
    PROP_FILE=nmea.mux.properties
    echo -e "Launching Nav Server with $PROP_FILE"
    ./runNavServer.sh --mux:$PROP_FILE &
    sleep 5 # Wait for the server to be operational
    open "http://localhost:9999/web/index.html"
    ;;
  *)
    echo -e "What? Unknown option [$option]"
    ;;
esac
#
#
echo -e "Bye now ✋"
#
