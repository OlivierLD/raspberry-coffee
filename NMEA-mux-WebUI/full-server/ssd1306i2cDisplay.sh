#!/bin/bash
CP=./build/libs/full-server-1.0-all.jar
#
JAVA_OPTIONS=
JAVA_OPTIONS="${JAVA_OPTIONS} -Dverbose=false "
JAVA_OPTIONS="${JAVA_OPTIONS} -Dssd1306.verbose=false "
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dmirror.screen=true"
#
# Separate the lines with a pipe |
#
if [[ "$1" = "IP" ]]
then
  _IP=$(hostname -I) || true
	if [[ "${_IP}" ]]
	then
	  printf "My IP address is %s\n" "${_IP}"
	fi
	#
	_OLED=`i2cdetect -y 1 | grep 3c`
	if [[ "${_OLED}" ]]
	then
	  printf "+---------------+\n"
	  printf "| OLED Detected |\n"
	  printf "+---------------+\n"
	  MESS="${_IP} | on RPi-Logger | default 192.168.127.1"
    sudo java ${JAVA_OPTIONS} -cp ${CP} i2c.samples.oled.OLEDSSD1306_I2C_DisplayStrings "${MESS}"
	else
	  printf "| NO OLED Detected |\n"
	fi
else
  sudo java ${JAVA_OPTIONS} -cp ${CP} i2c.samples.oled.OLEDSSD1306_I2C_DisplayStrings "$1"
fi
#
