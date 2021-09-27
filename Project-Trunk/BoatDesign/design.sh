#!/bin/bash
CP=./build/libs/BoatDesign-1.0-all.jar
#
OPT=
if [[ "$1" != "" ]]
then
  OPT="-Dinit-file=$1"
fi
java ${OPT} -jar ${CP} $*
