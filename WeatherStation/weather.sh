#!/bin/bash
CP=./build/libs/WeatherStation-1.0-all.jar
#
sudo java -cp ${CP} -Dverbose=true weatherstation.samples.BasicWindTest
