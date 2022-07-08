#!/bin/bash
#
echo Heel and Pitch WebGL demo.
echo Start the node server with
echo \$\> cd node
echo \$\> node server.js
echo ----
echo Then open http://localhost:9876/data/heel.pitch.html in a WS enabled browser.
#
CP=./build/libs/I2C-SPI-1.0-all.jar
JAVA_OPTS="-Dlsm303.verbose=true"
JAVA_OPTS="${JAVA_OPTS} -Dlsm303.verbose.mag=false"
JAVA_OPTS="${JAVA_OPTS} -Dlsm303.verbose.acc=true"
# JAVA_OPTS="${JAVA_OPTS} -Dws.uri=ws://localhost:9876/"
sudo java ${JAVA_OPTS} -cp ${CP} i2c.samples.LSM303HeelPitchWebGL
