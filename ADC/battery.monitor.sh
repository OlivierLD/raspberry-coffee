#!/bin/bash
echo Read an ADC
#
echo -e "+- IMPORTANT -----------------------------------+"
echo -e "| For miso, mosi, clk & cs, use BCM pin numbers |"
echo -e "+-----------------------------------------------+"
#
CP=./build/libs/ADC-1.0-all.jar
sudo java -cp $CP adc.sample.BatteryMonitor $*
