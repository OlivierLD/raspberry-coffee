#!/bin/bash
CP=./build/libs/diozero-101-1.0-all.jar
#
java -cp ${CP} com.diozero.sampleapps.GpioDetect
#
java -cp ${CP} com.diozero.sampleapps.SystemInformation

