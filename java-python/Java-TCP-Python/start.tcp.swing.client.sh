#!/bin/bash
CP=./build/libs/Java-TCP-Python-1.0-all.jar
#
OPTIONS=
# OPTIONS="-Dtcp.port=7002 -Dtcp.host=192.168.1.106"
#
echo -e "Client for the server TCP_ZDA_server.py"
#
java -cp ${CP} ${OPTIONS} tcp.clients.ZDATCPSwing101
