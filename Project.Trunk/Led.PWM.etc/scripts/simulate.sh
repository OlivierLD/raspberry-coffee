#!/bin/bash
echo -e "Swing led simulation"
CP=../build/libs/Led.PWM.etc-1.0-all.jar
java -cp ${CP} pwm.simulator.LedPanelMain
