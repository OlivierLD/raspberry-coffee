#!/bin/bash
echo Read an ADC
#
CP=./build/libs/ADC-1.0-all.jar
sudo java -cp ${CP} adc.gui.AnalogDisplayApp
