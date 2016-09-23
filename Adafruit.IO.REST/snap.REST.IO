#!/bin/bash
#
# This script refers to 'all' archives, like Adafruit.IO.REST-1.0-all.jar, containing
# all dependencies like json.jar.
# Those archives are built from the root like that:
#  raspberry-pi4j-samples $ ./gradlew shadowJar
#
CP=./build/libs/Adafruit.IO.REST-1.0-all.jar
#
if [ "$1" = "" ]
then
  echo Need your Adafruit-IO key as parameter.
  echo Aborting.
  exit 1
fi
sudo java -Dkey=$1 -cp $CP adafruit.io.sample.POSTSnapshot
