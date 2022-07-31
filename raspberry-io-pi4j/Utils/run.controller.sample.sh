#!/bin/bash
CP=./build/libs/Utils-1.0-all.jar
java -cp ${CP} -Dbutton.verbose=true utils.gpio.PushButtonControllerSample
#
