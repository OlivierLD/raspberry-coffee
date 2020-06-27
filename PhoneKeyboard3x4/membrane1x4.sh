#!/bin/bash
CP=./build/libs/PhoneKeyboard3x4-1.0-all.jar
#
OPTIONS=-Dkeypad.verbose=true
#
OPTIONS="${OPTIONS} -Dkeypad.cols=GPIO_1,GPIO_4,GPIO_5,GPIO_6"
OPTIONS="${OPTIONS} -Dcommon.lead=GPIO_7"
#
sudo java -cp $CP $OPTIONS membrane.SampleMain
