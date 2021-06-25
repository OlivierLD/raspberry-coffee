#!/bin/bash
CP=./classes:$PI4J_HOME/lib/pi4j-core.jar
java -cp $CP dac.sample.DACSample
