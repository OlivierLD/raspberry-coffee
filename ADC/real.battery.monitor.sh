#!/bin/bash
echo Monitoring the batteries on the boat, for real \(see coeffs\)
#
CP=./build/libs/ADC-1.0-all.jar
# sudo java -client -agentlib:jdwp=transport=dt_socket,server=y,address=1044 -cp $CP adc.sample.BatteryMonitor $*
sudo java -cp $CP adc.sample.BatteryMonitor -min=695:9.1 -max=973:12.6 $*
