#!/bin/bash
CP=./build/libs/I2C-SPI-1.0-all.jar
#
JAVA_OPTIONS=
JAVA_OPTIONS="$JAVA_OPTIONS -Dverbose=true "
JAVA_OPTIONS="$JAVA_OPTIONS -Dssd1306.verbose=true "
JAVA_OPTIONS="$JAVA_OPTIONS -Dmirror.screen=false"
JAVA_OPTIONS="$JAVA_OPTIONS -Dreturn.to.move.on=true"
#
sudo java ${JAVA_OPTIONS} -cp ${CP} spi.lcd.oled.samples.OLEDSSD1306_128x64_Sample
