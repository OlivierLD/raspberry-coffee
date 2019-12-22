#!/usr/bin/env bash
CP=./build/libs/Bluetooth-1.0-all.jar
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Dport.name=/dev/rfcomm0"
# JAVA_OPTS="$JAVA_OPTS -Dport.name=/dev/cu.Bluetooth-Incoming-Port"
JAVA_OPTS="$JAVA_OPTS -Dbaud.rate=9600"
#
sudo java -cp $CP $JAVA_OPTS bt.pi4j.demo.BtPi4j
