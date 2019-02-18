#!/bin/bash
echo -e "Start HTTP Server, Read an ADC (MPC3008) for 3.3 Volt estimation, drive a relay"
#
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Drelay.map=1:11"
JAVA_OPTS="$JAVA_OPTS -Dmiso.pin=23"
JAVA_OPTS="$JAVA_OPTS -Dmosi.pin=24"
JAVA_OPTS="$JAVA_OPTS -Dclk.pin=18"
JAVA_OPTS="$JAVA_OPTS -Dcs.pin=25"
JAVA_OPTS="$JAVA_OPTS -Dadc.channel=2"
JAVA_OPTS="$JAVA_OPTS -Drelay.verbose=false"
JAVA_OPTS="$JAVA_OPTS -Dhttp.verbose=false"
CP=./build/libs/REST.assembler-1.0-all.jar
#
#
sudo java -cp $CP $JAVA_OPTS httpserver.HttpRequestServer
#
echo Done.
