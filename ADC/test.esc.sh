#!/bin/bash
echo "Test the escape sequences..."
#
CP=./build/libs/ADC-1.0-all.jar
#
java -cp $CP adc.utils.EscapeSeq
