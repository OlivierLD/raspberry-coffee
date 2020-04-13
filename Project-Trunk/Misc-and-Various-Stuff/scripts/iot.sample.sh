#!/bin/bash
CP=../build/libs/Misc.and.Various.Stuff-1.0-all.jar
#
# OPTS=-Dbme280.debug=true
OPTS=-Dkey=54c2767878ca793f2e3cae1cxxxxxxxxxxxx
#
sudo java -cp $CP $OPTS sample.iot.TemperatureRelaySample
