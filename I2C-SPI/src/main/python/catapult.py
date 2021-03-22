# Very cool one,
# From https://www.instructables.com/Program-a-Servo-Build-a-Catapult-and-Solve-for-%ce%a0-w/?utm_source=newsletter&utm_medium=email
#
# TODO add some println, to know what's going on...
#
# Anything you see after '#' is not part of the code.  It's comments meant to help you understand the code

# These are libraries.  They are code written by other people that we're re-using to make our life easier.
# Importing them lets the computer know we're going to be using them.  Without them this code won't work.
# The RPi.GPIO library controls the pins we hooked up our servo to.
# the time library us used to let us delay parts of the program from running.
# the random library lets us launch different distances to solve for pi.
import RPi.GPIO as GPIO
import time
import random

# Here we're setting up our variables.  Ideally you want to only change things in one place, and have it work.
# That's why we've set up these variables.  We then re-use the variables throughout the code to make life easy.
# ServoPin is where we've connected the servo.  You can use any GPIO pin if you prefer
# Duty cycle high - this is how many milliseconds your servo says is for 90 degrees up.  Ours is actually 1.5,
#    but I'll explain why I'm using 1.4 in the instructable.
# Duty cycle low is where your servo is parallel with the ground.  You can actually use 180 degree's or 0.
#    again, this is in milliseconds per your manufacturer's spec sheet.
# Period.  This is how long between pulses on your spec sheet.  Ours is 20 milliseconds.
# TimeHigh.  You can have your arm go quickly back down, or wait at the top.  I'll explain my time on instructables
# TimeLow.  This is how long you think you'll want for reloading.
ServoPin = 3
DutyCycleHigh = 1.4
DutyCycleLow = .2
Period = 20
TimeHigh = .15
TimeLow = 2

# These are functions from the GPIO library to get our board set up and ready to go.
GPIO.setmode(GPIO.BOARD)
GPIO.setwarnings(False)
GPIO.setup(ServoPin, GPIO.OUT)

verbose = True
if verbose:
    print(f"ServoPin ${ServoPin}, DutyCycle: High ${DutyCycleHigh}, Low ${DutyCycleLow}, Period ${Period}")
    print(f"Time: High ${TimeHigh}, Low ${TimeLow}")

keep_looping = True

# This code after the 'while' will run forever until you select stop in thonny above, or you unplug it.
while keep_looping:
    try:
        # This loop controls both how long and how high to raise the arm.
        for i in range(int(TimeHigh * random.randrange(1, 4) * 1000 / (Period * 2))):
            GPIO.output(ServoPin, GPIO.HIGH)
            time.sleep(DutyCycleHigh / 1000)
            GPIO.output(ServoPin, GPIO.LOW)
            time.sleep((Period - DutyCycleHigh) / 1000)
        # This loop lowers the arm for the amount of time you've chosen.
        for i in range(int(TimeLow * 1000 / Period)):
            GPIO.output(ServoPin, GPIO.HIGH)
            time.sleep(DutyCycleLow / 1000)
            GPIO.output(ServoPin, GPIO.LOW)
            time.sleep((Period - DutyCycleLow) / 1000)
    except KeyboardInterrupt:
        print("\n\t\tUser interrupted, exiting.")
        keep_looping = False

print("Done")
