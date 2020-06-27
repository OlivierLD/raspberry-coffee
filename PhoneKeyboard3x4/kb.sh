#!/bin/bash
CP=./build/libs/PhoneKeyboard3x4-1.0-all.jar
#
OPTIONS=-Dkeypad.verbose=true
# For remote debugging
# OPTIONS="${OPTIONS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
#
sudo java -cp ${CP} ${OPTIONS} phonekeyboard3x4.SampleMain
