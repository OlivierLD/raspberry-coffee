#!/bin/bash
CP=./build/libs/TemperatureMonitor-1.0-all.jar
#
java -cp ${CP} monitor.SwingTemperatureMonitor $*
