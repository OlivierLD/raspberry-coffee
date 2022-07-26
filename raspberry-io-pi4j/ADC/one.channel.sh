#!/bin/bash
CP=./build/libs/ADC-1.0-all.jar
# Try one.channel.sh --help
java -cp ${CP} adc.sample.SampleMain $*
