#!/usr/bin/env bash
#
# Pure shell approach, using gpio from the CLI
# Pins HAVE to be GPIO pins. Provide their BCM numbers.
# Default is 18, physical #12
#
# pin GPIO_18 is #12
# pin GPIO_27 is #13
#
# GPIO pins (BCM numbers (Physical #)) are 2(3), 3(5), 4(7), 17(11), 27(13), 22(15), 10(19), 9(21), 11(23), 0(27), 5(29), 6(31), 13(33), 19(35), 26(37),
#                                         21(40), 20(38), 16(36), 12(32), 1(28), 7(26), 8(24), 25(22), 24(18), 23(16), 18(12), 15(10), 14(8)
# See https://www.raspberrypi.org/documentation/usage/gpio/
#
PIN_VALUES=(2 3 4 17 27 22 10 9 11 0 5 6 13 19 26 21 20 16 12 1 7 8 25 24 23 18 15 14)
#
PIN=18
if [[ $# -gt 0 ]]
then
  PIN=$1
fi
# Validate pin #
FOUND=false
for pin in ${PIN_VALUES[@]}
do
  # echo -e "Testing pin $pin vs $PIN"
  if [[ "$PIN" == "$pin" ]]
  then
    echo -e "Good!"
    FOUND=true
    break
  fi
done
#
# echo -e "Found: $FOUND"
if [[ "$FOUND" == "false" ]]
then
  echo -e "Pin $PIN is not valid."
  echo -e "Value values are ${PIN_VALUES[@]}"
  exit 1
fi
#
echo -e "Using pin #$PIN"
#
gpio readall
#
gpio -g mode ${PIN} pwm
gpio pwm-ms
gpio pwmc 192
gpio pwmr 2000
gpio -g pwm ${PIN} 150
sleep 1
gpio -g pwm ${PIN} 200
echo Done.
