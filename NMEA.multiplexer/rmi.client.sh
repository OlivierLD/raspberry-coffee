#!/usr/bin/env bash
#
# Classes needed for the client:
#
# nmea/forwarders/rmi/clientoperations/LastString.class (and the list of the other classes to invoke if needed).
# nmea/forwarders/rmi/ServerInterface.class
# nmea/forwarders/rmi/Task.class
#
# and of course
# samples/rmi/client/SampleRMIClient.class
#
# Those classes are in build/lib/NMEA.multiplexer-1.0.jar (../gradlew --daemon build), but this jar
# can be shrinked.
#
CP=./build/libs/NMEA.multiplexer-1.0.jar
#
java -cp $CP samples.rmi.client.SampleRMIClient
#
