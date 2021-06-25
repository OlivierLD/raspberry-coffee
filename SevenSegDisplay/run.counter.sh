#!/bin/bash
echo 7 segment display
PI4J_HOME=/opt/pi4j
CP=./build/libs/SevenSegDisplay-1.0.jar
CP=$CP:$PI4J_HOME/lib/pi4j-core.jar
sudo java -cp $CP sevensegdisplay.samples.CounterSample
