#!/usr/bin/env python3
#
# Pure PWM in Python
#
# Drive a continuous or standard servo
# This code is more adapted for a Continuous servo (function set_rotation)
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

# Values below for 50Hz, for a PARALLAX Continuous servo
STOP_ROTATION = 7.25                # ~  90 degrees on a Standard Servo
ROTATE_CLOCKWISE = 6.0              # ~   0 degrees on a Standard Servo
SLOW_ROTATE_CLOCKWISE = 7.0
ROTATE_COUNTER_CLOCKWISE = 8.0      # ~ 180 degrees on a Standard Servo
ROTATE_SLOW_COUNTER_CLOCKWISE = 7.4

# For a SG92R (micro standard servo), values would go from 2.0 to 12.0...
# Same for SG-5010 (standard servo)

print(f"RPi.GPIO version {GPIO.VERSION}")

def set_rotation(duty):
    pwm.ChangeDutyCycle(duty)    # pwm defined below


GPIO.setmode(GPIO.BOARD)          # <= i.e. Use physical pin numbers
GPIO.setwarnings(False)
GPIO.setup(servo_pin, GPIO.OUT)

with_user_input = True   # Will use time delay otherwise

pwm = GPIO.PWM(servo_pin, 50)
pwm.start(0)

GPIO.output(servo_pin, True)


print("Setting SLOW_ROTATE_CLOCKWISE")
set_rotation(SLOW_ROTATE_CLOCKWISE)
if with_user_input:
    user_input = input("Hit [return] to move on ")
else:
    sleep(5)

print("Setting ROTATE_CLOCKWISE")
set_rotation(ROTATE_CLOCKWISE)
if with_user_input:
    user_input = input("Hit [return] to move on ")
else:
    sleep(5)

print("Setting STOP_ROTATION")
set_rotation(STOP_ROTATION)
if with_user_input:
    user_input = input("Hit [return] to move on ")
else:
    sleep(5)

print("Setting ROTATE_SLOW_COUNTER_CLOCKWISE")
set_rotation(ROTATE_SLOW_COUNTER_CLOCKWISE)
if with_user_input:
    user_input = input("Hit [return] to move on ")
else:
    sleep(5)

print("Setting ROTATE_COUNTER_CLOCKWISE")
set_rotation(ROTATE_COUNTER_CLOCKWISE)
if with_user_input:
    user_input = input("Hit [return] to move on ")
else:
    sleep(5)

print("Setting STOP_ROTATION")
set_rotation(STOP_ROTATION)
if with_user_input:
    user_input = input("Hit [return] to move on ")
#else:
#    sleep(1)


GPIO.output(servo_pin, False) # This would stop the servo. A continuous would stop spinning.
pwm.ChangeDutyCycle(0)

print("Done with Continuous demo")
pwm.stop()

GPIO.cleanup()
