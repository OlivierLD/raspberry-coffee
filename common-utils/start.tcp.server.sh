#!/bin/bash
CP=./build/libs/common-utils-1.0-all.jar
#
PRMS=
# PRMS="--port:8888"
java -cp ${CP} utils.samples.tcp.echo.TCPMultiServer ${PRMS}
