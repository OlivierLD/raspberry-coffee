# -*- coding: utf-8 -*-
import sys
import time
import cv2
import numpy as np
import os

Kernel_size = 15
low_threshold = 40
high_threshold = 120

rho = 10
threshold = 15
theta = np.pi / 180
minLineLength = 10
maxLineGap = 1

# Initialize camera
video_capture = cv2.VideoCapture(0)

while True:
    # CAPTURE FRAME-BY-FRAME
    ret, frame = video_capture.read()
    time.sleep(0.1)
    # Convert to Grayscale
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    # Blur image to reduce noise. if Kernel_size is bigger the image will be more blurry
    blurred = cv2.GaussianBlur(gray, (Kernel_size, Kernel_size), 0)

    # Perform canny edge-detection.
    # If a pixel gradient is higher than high_threshold is considered as an edge.
    # if a pixel gradient is lower than low_threshold is is rejected , it is not an edge.
    # Bigger high_threshold values will provoque to find less edges.
    # Canny recommended ratio upper:lower  between 2:1 or 3:1
    edged = cv2.Canny(blurred, low_threshold, high_threshold)
    # Perform hough lines probalistic transform
    lines = cv2.HoughLinesP(edged, rho, theta, threshold, minLineLength, maxLineGap)

    # Draw circles in the center of the picture
    cv2.circle(frame, (320, 240), 20, (0, 0, 255), 1)
    cv2.circle(frame, (320, 240), 10, (0, 255, 0), 1)
    cv2.circle(frame, (320, 240), 2, (255, 0, 0), 2)

    # With this for loops only a dots matrix is painted on the picture
    # for y in range(0,480,20):
    # for x in range(0,640,20):
    # cv2.line(frame,(x,y),(x,y),(0,255,255),2)

    # With this for loops a grid is painted on the picture
    for y in range(0, 480, 40):
        cv2.line(frame, (0, y), (640, y), (255, 0, 0), 1)
        for x in range(0, 640, 40):
            cv2.line(frame, (x, 0), (x, 480), (255, 0, 0), 1)

    # Draw lines on input image
    if lines is not None:
        # print("Detected {} line(s)".format(len(lines)))
        for x1, y1, x2, y2 in lines[0]:
            cv2.line(frame, (x1, y1), (x2, y2), (0, 255, 0), 10)  # 2)
            cv2.putText(frame, 'lines_detected', (50, 50), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 1)
    cv2.imshow("line detect test", frame)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# When everything is done, release the capture

video_capture.release()
cv2.destroyAllWindows()
