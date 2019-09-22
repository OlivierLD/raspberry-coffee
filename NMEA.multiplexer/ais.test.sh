#!/usr/bin/env bash
#
CP=./build/libs/NMEA.multiplexer-1.0-all.jar
java -cp $CP nmea.consumers.client.AISClientV2
