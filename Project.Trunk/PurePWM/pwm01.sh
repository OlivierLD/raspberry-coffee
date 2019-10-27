#!/usr/bin/env bash
#
# pin GPIO_18 is #12
# pin GPIO_27 is #13
#
PIN=18
gpio -g mode $PIN pwm
gpio pwm-ms
gpio pwmc 192
gpio pwmr 2000
gpio -g pwm $PIN 150
sleep 1
gpio -g pwm $PIN 200
echo Done.
