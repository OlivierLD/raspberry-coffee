#!/bin/bash
CP=./build/libs/small-server-extended-1.0-all.jar
#
# Try ./ssd1306i2cDisplay.sh "Lorem ipsum dolor sit|amet, tellus tempus|vitae tempor|pellentesque. Lobortis|condimentum tortor|volutpat ipsum augue,..."
#
SUDO=
# DARWIN=`uname -a | grep Darwin`
DARWIN=$(uname -a | grep Darwin)
#
if [[ "$DARWIN" != "" ]]; then
	echo Running on Mac
else
	echo Assuming Linux/Raspberry Pi
  SUDO="sudo "
fi
#
JAVA_OPTIONS=
JAVA_OPTIONS="${JAVA_OPTIONS} -Dverbose=false"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dssd1306.verbose=false"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dmirror.screen=true"
JAVA_OPTIONS="${JAVA_OPTIONS} -Doled.height=64"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dled.color=white"
#
# Separate the lines with a pipe |
#
if [[ "$1" = "IP" ]]; then
  _IP=$(hostname -I) || true
	if [[ "$_IP" ]]; then
	  printf "My IP address is %s\n" "$_IP"
	fi
	#
	_OLED=`i2cdetect -y 1 | grep 3c`
	if [[ "$_OLED" ]]; then
	  printf "+---------------+\n"
	  printf "| OLED Detected |\n"
	  printf "+---------------+\n"
	  MESS="$_IP | on RPi-Logger | default 192.168.127.1"
    sudo java ${JAVA_OPTIONS} -cp ${CP} i2c.samples.oled.OLEDSSD1306_I2C_DisplayStrings "$MESS"
	else
	  printf "| NO OLED Detected |\n"
	fi
else
  ${SUDO}java ${JAVA_OPTIONS} -cp ${CP} i2c.samples.oled.OLEDSSD1306_I2C_DisplayStrings "$1"
fi
#
