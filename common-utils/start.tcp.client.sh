#!/bin/bash
CP=./build/libs/common-utils-1.0-all.jar
#
PRMS=
# PRMS="--port:8888 --host:192.168.1.106"
java -cp ${CP} utils.samples.tcp.clients.SimpleTCPClient ${PRMS}
