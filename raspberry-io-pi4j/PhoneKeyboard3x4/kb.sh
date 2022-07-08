#!/bin/bash
CP=./build/libs/PhoneKeyboard3x4-1.0-all.jar
#
OPTIONS=-Dkeypad.verbose=true
# Those pins: GPIO_5, GPIO_6, GPIO_0 generates errors on RPi 4B
# OPTIONS="${OPTIONS} -Dkeypad.rows=GPIO_1,GPIO_4,GPIO_5,GPIO_6"
# OPTIONS="${OPTIONS} -Dkeypad.cols=GPIO_7,GPIO_0,GPIO_3"
#
OPTIONS="${OPTIONS} -Dkeypad.rows=GPIO_1,GPIO_4,GPIO_21,GPIO_22"
OPTIONS="${OPTIONS} -Dkeypad.cols=GPIO_7,GPIO_23,GPIO_3"
#
#
# For remote debugging
# OPTIONS="${OPTIONS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
#
sudo java -cp ${CP} ${OPTIONS} phonekeyboard3x4.SampleMain
