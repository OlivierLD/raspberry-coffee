#!/bin/bash
CP=./build/libs/diozero-101-1.0-all.jar
#
java -cp ${CP} com.diozero.sampleapps.GpioDetect
#
java -cp ${CP} com.diozero.sampleapps.SystemInformation
#
java -cp ${CP} com.diozero.sampleapps.GpioReadAll
#
# java -cp ${CP} com.diozero.sampleapps.LEDTest 24
# java -cp ${CP} com.diozero.sampleapps.ButtonTest 12
#
# Compare to others, if available
#
echo -e "-----------------------"
echo -e "--- Other utilities ---"
echo -e "-----------------------"
if [[ "$(which pinout)" != "" ]]; then
  echo -e "-- pinout --"
  pinout
else
  echo -e "pinout not available."
fi

if [[ "$(which gpio)" != "" ]]; then
  echo -e "-- gpio readall --"
  gpio readall
else
  echo -e "gpio readall not available."
fi
#
if [[ -f ../common-utils/build/libs/common-utils-1.0-all.jar ]]; then
  echo -e "-- java utils.GenericPinUtil (fat) --"
  java -cp ../common-utils/build/libs/common-utils-1.0-all.jar utils.GenericPinUtil
else
  if [[ -f ../common-utils/build/libs/common-utils-1.0.jar ]]; then
    echo -e "-- java utils.GenericPinUtil (non-fat, fallback) --"
    java -cp ../common-utils/build/libs/common-utils-1.0.jar utils.GenericPinUtil
  else
    echo -e "utils.GenericPinUtil not available..."
  fi
fi
#
if [[ -f ../raspberry-io-pi4j/Utils/build/libs/Utils-1.0-all.jar ]]; then
  echo -e "-- java utils.PinUtil (fat) --"
  java -cp ../raspberry-io-pi4j/Utils/build/libs/Utils-1.0-all.jar utils.PinUtil
else
#  if [[ -f ../raspberry-io-pi4j/Utils/build/libs/Utils-1.0.jar ]]; then
#    echo -e "-- java utils.PinUtil (non-fat) --"
#    java -cp ../raspberry-io-pi4j/Utils/build/libs/Utils-1.0.jar utils.PinUtil
#  else
    echo -e "utils.PinUtil not available..."
#  fi
fi

