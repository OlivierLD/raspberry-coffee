#!/bin/bash
#
# Used to run the GUI Components tests.
#
CP=./build/libs/NMEA-Mux-Other-Clients-1.0-all.jar
#
# java -cp ${CP} clients.components.ClockTest
# java -cp ${CP} clients.components.HeadingTest
# java -cp ${CP} clients.components.SpeedTest
# java -cp ${CP} clients.components.DirectionTest
java -cp ${CP} clients.components.JumboTest
