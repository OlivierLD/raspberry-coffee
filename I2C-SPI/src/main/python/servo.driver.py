#
# Drive a continuous or standard servo
# Ground (black or brown) on pin #6
# 5V (red or orange) on pin #2
# Signal (white or yellow) on pin #3
#
# See also https://tutorials-raspberrypi.com/raspberry-pi-servo-motor-control/
#          https://www.instructables.com/Program-a-Servo-Build-a-Catapult-and-Solve-for-%ce%a0-w/?utm_source=newsletter&utm_medium=email
#

import RPi.GPIO as GPIO
from time import sleep

servo_pin = 3  # Physical pin. (3: SDA)


def set_angle(angle):
    duty = angle / 18 + 2
    print(f"For angle {angle}, duty is {duty}")
    GPIO.output(servo_pin, True)
    pwm.ChangeDutyCycle(duty)
    sleep(1)
    GPIO.output(servo_pin, False)
    pwm.ChangeDutyCycle(0)


GPIO.setmode(GPIO.BOARD)          # <= i.e. Use physical numbers
GPIO.setwarnings(False)
GPIO.setup(servo_pin, GPIO.OUT)

pwm = GPIO.PWM(servo_pin, 50)
pwm.start(0)

print("Setting angle to 90")
set_angle(90)
sleep(1)

print("Setting angle to 0")
set_angle(0)
sleep(1)

print("Setting angle to 180")
set_angle(180)
sleep(1)

print("Setting angle to 0")
set_angle(0)
# sleep(1)

print("Deon with demo")
pwm.stop()


GPIO.cleanup()
