# -*- coding: utf-8 -*-
import sys
import time
import cv2
import numpy as np
import os
import math

#
# OpenCV drawing functions at https://docs.opencv.org/2.4/modules/core/doc/drawing_functions.html
#

Kernel_size = 15
low_threshold = 40
high_threshold = 120

rho = 10
threshold = 15
theta = np.pi / 180
minLineLength = 10
maxLineGap = 1

# Initialize camera
camera = cv2.VideoCapture(0)

width  = 640
height = 480
camera.set(3, width)
camera.set(4, height)

print('Select an image, and hit [q] tp exit')


def line_length(x1, y1, x2, y2):
    return math.sqrt(((x2 - x1) ** 2) + ((y2 - y1) ** 2))

#
# 0,0 is top left of the image
#
# Below, images are:
# - frame: original
# - gray: gray scale
# - blurred: Gaussian Blur
# - edged: Canny
# - lines: array of detected lines
#


from_blur = True
dot_on_frame = True

while True:
    # CAPTURE FRAME-BY-FRAME
    ret, frame = camera.read()
    time.sleep(0.1)
    # Convert to Grayscale
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    # Blur image to reduce noise. if Kernel_size is bigger the image will be more blurry
    blurred = cv2.GaussianBlur(gray, (Kernel_size, Kernel_size), 0)

    # Perform canny edge-detection.
    # If a pixel gradient is higher than high_threshold is considered as an edge.
    # if a pixel gradient is lower than low_threshold is is rejected , it is not an edge.
    # Bigger high_threshold values will provoke to find less edges.
    # Canny recommended ratio upper:lower  between 2:1 or 3:1
    if from_blur:
        edged = cv2.Canny(blurred, low_threshold, high_threshold)
    else:
        edged = cv2.Canny(gray, low_threshold, high_threshold)
    # Perform Hough lines probalistic transform
    lines = cv2.HoughLinesP(edged, rho, theta, threshold, minLineLength, maxLineGap)

    # Draw circles in the center of the picture
    cv2.circle(frame, (int(width / 2), int(height / 2)), 20, (0, 0, 255), 1)
    cv2.circle(frame, (int(width / 2), int(height / 2)), 10, (0, 255, 0), 1)
    cv2.circle(frame, (int(width / 2), int(height / 2)), 2, (255, 0, 0), 2)

    if dot_on_frame:
        # With this for loops only a dots matrix is painted on the picture
        for y in range(0, height, 20):
            for x in range(0, width, 20):
                cv2.line(frame, (x, y), (x, y), (0, 255, 255), 2)
    else:
        # With this for loops a grid is painted on the picture
        for y in range(0, height, 40):
            cv2.line(frame, (0, y), (width, y), (255, 0, 0), 1)
            for x in range(0, width, 40):
                cv2.line(frame, (x, 0), (x, height), (255, 0, 0), 1)

    # Draw lines on input image
    if lines is not None:
        # print("Detected {} line(s)".format(len(lines)))
        max_len = 0
        max_idx = -1
        for i in range(len(lines)):
            # Find the longest line
            for x1, y1, x2, y2 in lines[i]:
                # Only in th top part of the screen
                if y1 < (height / 2) or y2 < (height / 2):
                    line_len = line_length(x1, y1, x2, y2)
                    if line_len > max_len:
                        max_len = line_len
                        max_idx = i
        if max_idx > -1:
            for x1, y1, x2, y2 in lines[max_idx]:
                if x1 > (width / 2) and x2 > (width / 2):
                    print("Turn right")
                else:
                    print("Turn left")
                cv2.line(frame, (x1, y1), (x2, y2), (0, 255, 0), 10)  # 2) Green line, from red, to blue
                cv2.line(frame, (x1, y1), (x1, y1), (255, 0, 0), 10)  # Red dot
                # cv2.putText(frame, 'From {},{}'.format(x1, y1), (x1, y1), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 1)
                cv2.line(frame, (x2, y2), (x2, y2), (0, 0, 255), 10)  # Blue dot
                cv2.putText(frame, 'lines_detected', (50, 50), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 1)

    cv2.imshow("Original", frame)
    cv2.imshow("Gray", gray)
    cv2.imshow("Blurred", blurred)
    cv2.imshow("Edged", edged)

    # row_one = np.hstack((frame, gray))
    # row_two = np.hstack((blurred, edged))
    # two_rows = np.vstack((row_one, row_two))
    # cv2.imshow('4 images', two_rows)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# When everything is done, release the capture

camera.release()
cv2.destroyAllWindows()
