#!/bin/bash
CP=./build/libs/Servos-and-Co-1.0-all.jar
sudo java -cp ${CP} feedback.one0one.DemoInteractiveContinuous $*
