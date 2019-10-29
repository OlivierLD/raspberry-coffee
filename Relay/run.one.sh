#!/bin/bash
echo Driving a relay, pin 00
CP=build/libs/Relay-1.0-all.jar
sudo java -cp ${CP} relay.OneRelay
