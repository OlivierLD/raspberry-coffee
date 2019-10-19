#!/usr/bin/env python3
#
# Continuous capture
#
# https://pypi.org/project/opencv-python/
#
# pip install opencv-python
#
# About matlibplot: https://stackoverflow.com/questions/28269157/plotting-in-a-non-blocking-way-with-matplotlib
#
import sys
import time
import warnings
import cv2

warnings.filterwarnings('ignore')

print("Let's go!")

# Usual yada-yada
print("OpenCV version", cv2.__version__)

print("{} script arguments.".format(len(sys.argv)))

# The core of the program
camera = cv2.VideoCapture(0)

width = 640
height = 640
camera.set(3, width)
camera.set(4, height)

mirror = False
zoom = False
scale = 25  # Zoom scale. Percent of the original (radius). 50 => 100%

print("+----------------------------------------------------+")
print("| Type Q, q or Ctrl+C to exit the loop               |")
print("| > Select the main image before hitting a key... ;) |")
print("+----------------------------------------------------+")
keepLooping = True
while keepLooping:

    _, frame = camera.read()
    time.sleep(0.1)
    try:
        original_image = frame;
        if mirror:
            original_image = cv2.flip(original_image, 1)

        if zoom:
            # Zoom on the image, see 'scale' (in %)
            # get the webcam size
            img_height, img_width, channels = original_image.shape
            # prepare the crop
            centerX, centerY = int(img_height / 2), int(img_width / 2)
            radiusX, radiusY = int(scale * img_height / 100), int(scale * img_width / 100)

            minX, maxX = centerX - radiusX, centerX + radiusX
            minY, maxY = centerY - radiusY, centerY + radiusY

            cropped = frame[minX:maxX, minY:maxY]
            original_image = cv2.resize(cropped, (img_width, img_height))

        # Original image
        cv2.imshow('Original', original_image)
    except Exception as ex:
        print("Oops! {}".format(ex))

    key = cv2.waitKey(1) & 0xFF
    # print("Key : {}".format(key))
    if key == ord('q'):  # select the image window and hit 'q' to quit
        keepLooping = False

# Releasing resources
camera.release()
cv2.destroyAllWindows()

print("Bye!")
