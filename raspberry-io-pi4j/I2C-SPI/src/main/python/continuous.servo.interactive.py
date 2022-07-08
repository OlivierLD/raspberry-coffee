#!/usr/bin/env python3
#
# Pure PWM in Python
# Interactive version.
#
# Drive a continuous or standard servo
#
# Ground (black on continuous, or brown on standard) on pin #6
# 5V (red on continuous, or orange on standard) on pin #2
# Signal (white on continuous, or yellow on standard) on pin #3
#
#
# The PWM for Rpi.GPIO works in percent.
#
# Normal servo use a cycle of 20ms (50Hz)
#
# Then
# 1000 us = 1000us / 20000us * 1000 = 5
# 1500 us = 7.5
# 2000 us = 10
#
# See also https://tutorials-raspberrypi.com/raspberry-pi-servo-motor-control/
#          https://www.instructables.com/Program-a-Servo-Build-a-Catapult-and-Solve-for-%ce%a0-w
#
# Doc at https://pypi.org/project/RPi.GPIO/,
#    and https://sourceforge.net/p/raspberry-gpio-python/wiki/Home/
#
import RPi.GPIO as GPIO
from time import sleep

servo_pin = 3  # Physical pin. (3: SDA)

print(f"RPi.GPIO version {GPIO.VERSION}")

def set_duty_cycle(duty: int) -> None:
    pwm.ChangeDutyCycle(duty)    # pwm defined below


# Start here, prompt for the servo pin
user_input = input(f"Enter Servo Physical pin (default is {servo_pin}) > ")
if len(user_input.strip()) > 0:
    try:
        servo_pin = int(user_input.strip())
    except ValueError:
        print(f"Bad int value [{user_input}], keeping default {servo_pin}")

GPIO.setmode(GPIO.BOARD)          # <= i.e. Use physical pin numbers. Could also be GPIO.BCM
GPIO.setwarnings(False)
GPIO.setup(servo_pin, GPIO.OUT)

pwm = GPIO.PWM(servo_pin, 50)
pwm.start(0)

GPIO.output(servo_pin, True)

print("Go ahead! Enter Q to quit.")
keep_working = True
while keep_working:
    user_input = input("> PWM value - like in [0, 15] (or Q to quit) : ")
    if user_input.upper() == 'Q':
        keep_working = False
    else:
        try:
            duty_value = float(user_input)
            set_duty_cycle(duty_value)
        except ValueError:
            print(f"Bad float value [{user_input}]")

GPIO.output(servo_pin, False) # This would stop the servo. A continuous would stop spinning.
pwm.ChangeDutyCycle(0)

print("Done with Continuous demo")
pwm.stop()

GPIO.cleanup()
