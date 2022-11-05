#!/bin/bash
CP=./build/libs/Java-TCP-Python-1.0-all.jar
#
PRMS=
# PRMS="--port:8888"
java -cp ${CP} tcp.server.TCPMultiServer ${PRMS}
