#!/usr/bin/env bash
#
# An example. Shows how to read the STH10 sensor from Groovy.
# Install Groovy through SDKMan, as explained here: http://groovy-lang.org/download.html
# sdk install groovy
#
export GROOVY_HOME=/home/pi/.sdkman/candidates/groovy/2.5.0
export CLASSPATH=$(find ${GROOVY_HOME}/lib -name '*.jar' | tr '\n' ':')
export CLASSPATH=${CLASSPATH}:${PWD}/build/libs/PlantWateringSystem-1.0-all.jar
#
groovy src/groovy/ReadSensor
