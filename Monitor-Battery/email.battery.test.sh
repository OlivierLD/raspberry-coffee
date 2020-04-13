#!/usr/bin/env bash
echo Usage:
echo  $0 -verbose -send:google -sendto:me@home.net,you@yourplace.biz -help
#
CP=./build/libs/Monitor.Battery-1.0-all.jar
java -cp $CP sample.email.EmailVoltage $*
#
