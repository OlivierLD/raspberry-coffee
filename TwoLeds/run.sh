#!/bin/bash
echo Two leds!!
CP=./classes:${PI4J_HOME}/lib/pi4j-core.jar
sudo java -Dverbose=true -cp ${CP} twoleds.MainController
