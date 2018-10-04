#!/bin/bash
CP=../build/libs/Weather.Station-1.0-all.jar
#
sudo java -cp $CP raspisamples.log.net.WeatherDataFileLogging $*
