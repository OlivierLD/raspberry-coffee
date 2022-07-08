#!/usr/bin/env python3
#
# Pure PWM in Python
#
# Drive a continuous or standard servo
# This code is more adapted for a Standard servo (function set_angle)
#
# Ground (black on continuous, or brown on standard) on pin #6
# 5V (red on continuous, or orange on standard) on pin #2
# Signal (white on continuous, or yellow on standard) on pin #3
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

def set_angle(angle: float) -> None:
    duty = angle / 18 + 2
    print(f"\tFor angle {angle}, duty is {duty}")
    GPIO.output(servo_pin, True)
    pwm.ChangeDutyCycle(duty)    # pwm defined below
    sleep(1)
    GPIO.output(servo_pin, False) # This would stop the servo. A continuous would stop spinning.
    pwm.ChangeDutyCycle(0)


GPIO.setmode(GPIO.BOARD)          # <= i.e. Use physical pin numbers
GPIO.setwarnings(False)
GPIO.setup(servo_pin, GPIO.OUT)

with_user_input = True   # Will use time delay otherwise

pwm = GPIO.PWM(servo_pin, 50)
pwm.start(0)

print("Setting angle to 90")
set_angle(90)
if with_user_input:
    user_input = input("Hit [return] to move on ")
else:
    sleep(1)

print("Setting angle to 0")
set_angle(0)
if with_user_input:
    user_input = input("Hit [return] to move on ")
else:
    sleep(1)

print("Setting angle to 180")
set_angle(180)
if with_user_input:
    user_input = input("Hit [return] to move on ")
else:
    sleep(1)

print("Setting angle to 0")
set_angle(0)
if with_user_input:
    user_input = input("Hit [return] to move on ")
#else:
#    sleep(1)

print("Done with Standard demo")
pwm.stop()

GPIO.cleanup()
