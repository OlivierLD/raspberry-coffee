#!/bin/bash
echo -e "---------------------------------------------------------------------"
echo -e "This will work, but you\'d better run ./gradlew clean build [install] [shadowJar]"
echo -e "Might (will) not be 100% up-to-date..."
echo -e "---------------------------------------------------------------------"
#
buildProject() {
  WORK_DIR=$1
  cd ${WORK_DIR}
  echo Building ${WORK_DIR}
  ../gradlew clean build install
  cd ..
}
echo Building everything...
buildProject SevenSegDisplay
buildProject I2C-SPI
buildProject ADC
buildProject DAC
buildProject GPIO.01
# buildProject GPS.sun.servo
buildProject PhoneKeyboard3x4
buildProject Arduino-RaspberryPI
buildProject HC-SR04
buildProject PIR
buildProject FONA
buildProject Relay
# buildProject WeatherStation
# buildProject RasPISamples
buildProject LelandOilDetector
buildProject Serial-IO
buildProject MindWave
echo Done!
