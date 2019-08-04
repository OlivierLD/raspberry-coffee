#!/bin/bash
CP=../build/libs/Weather.Station.Implementation-1.0.jar
#
sudo java -cp $CP raspisamples.log.net.WeatherDataLogging $*
