#!/bin/bash
#
JAVA_OPTS=
# JAVA_OPTS="${JAVA_OPTS} -client -agentlib:jdwp=transport=dt_socket,server=y,address=1044"
JAVA_OPTS="${JAVA_OPTS} -Dhc_sr04.verbose=true"
CP=./build/libs/HC-SR04-1.0-all.jar
sudo java ${JAVA_OPTS} -cp ${CP} rangesensor.HC_SR04

