#!/bin/bash
CP=../build/libs/Weather.Station.Implementation-1.0-all.jar
#
sudo java -cp $CP raspisamples.log.net.WeatherDataFileLogging $*
