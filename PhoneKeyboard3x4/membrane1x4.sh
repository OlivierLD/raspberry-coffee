#!/bin/bash
CP=./build/libs/PhoneKeyboard3x4-1.0-all.jar
#
OPTIONS=-Dkeypad.verbose=true
#
# Those pins: GPIO_5, GPIO_6, GPIO_0 generates errors on RPi 4B
# OPTIONS="${OPTIONS} -Dkeypad.cols=GPIO_1,GPIO_4,GPIO_5,GPIO_6"
OPTIONS="${OPTIONS} -Dkeypad.cols=GPIO_1,GPIO_4,GPIO_21,GPIO_22"
OPTIONS="${OPTIONS} -Dcommon.lead=GPIO_7"
#
sudo java -cp $CP $OPTIONS membrane.SampleMain
