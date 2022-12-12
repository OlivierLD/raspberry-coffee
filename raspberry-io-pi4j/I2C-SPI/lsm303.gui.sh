#!/bin/bash
CP=./build/libs/I2C-SPI-1.0-all.jar
# CP=${CP}:${PI4J_HOME}/lib/pi4j-core.jar
# sudo java -cp ${CP} main.gui.acc.AccelerometerUI
echo -e "Replaced. Run it from Gradle:"
echo -e "../../gradle test --tests main.gui.acc.AccelerometerUI"
# ../../gradle test --tests main.gui.acc.AccelerometerUI
