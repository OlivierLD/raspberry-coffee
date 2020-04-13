#!/bin/bash
#
# Triple axis compass
#
CP=./build/libs/I2C.SPI-1.0-all.jar
#
# Comment/Uncomment as needed
#
JAVA_OPTIONS=""
# JAVA_OPTIONS="$JAVA_OPTIONS -Dhmc5883l.verbose=true"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dhmc5883l.verbose.raw=true"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dhmc5883l.verbose.mag=true"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dhmc5883l.low.pass.filter=false"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dhmc5883l.log.for.calibration=true"
#
# JAVA_OPTIONS="$JAVA_OPTIONS -Dhmc5883l.cal.prop.file=hmc5883l.cal.properties"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dssd1306.compass.verbose=false"
#
if [[ "$1" == "-w" ]] # To wait for everything to start?
then
	echo -e ""
	echo -e "+------------------------+"
	echo -e "| Giving Java some slack |"
	echo -e "+------------------------+"
	sleep 10
	echo -e ""
	echo -e "+-------------------+"
	echo -e "| Now starting Java |"
	echo -e "+-------------------+"
fi
#
sudo java ${JAVA_OPTIONS} -cp ${CP} i2c.samples.HMC5883LWithSSD1306
