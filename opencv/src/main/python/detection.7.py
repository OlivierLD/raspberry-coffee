# -*- coding: utf-8 -*-
#
# Path detection
# Same as detection.6, but frame by frame
#
import time
import cv2
import numpy as np
import math

Kernel_size = 31
low_threshold = 40
high_threshold = 120

rho = 10
threshold = 15
theta = np.pi / 180
minLineLength = 10
maxLineGap = 1

# Initialize and open camera
camera = cv2.VideoCapture(0)

width = 640
height = 480
camera.set(3, width)
camera.set(4, height)

show_thresh = True

# degree_sign = "º"
# degree_sign = chr(176)
degree_sign = ""

font = cv2.FONT_HERSHEY_SIMPLEX
# font = cv2.FONT_HERSHEY_DUPLEX
# font = cv2.FONT_HERSHEY_PLAIN
# font = cv2.FONT_HERSHEY_COMPLEX
# font = cv2.FONT_HERSHEY_TRIPLEX
# font = cv2.FONT_HERSHEY_COMPLEX_SMALL
# font = cv2.FONT_HERSHEY_SCRIPT_COMPLEX
# font = cv2.FONT_HERSHEY_SCRIPT_SIMPLEX


def get_dir(x, y):
    direction = math.degrees(math.atan2(x, y))
    while direction < 0:
        direction += 360
    return direction


while True:
    # Frame by Frame
    ret, frame = camera.read()
    time.sleep(0.1)
    # cv2.imshow('Original', image)

    img_gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    # cv2.imshow('Gray', img_gray)

    blurred = cv2.GaussianBlur(img_gray, (Kernel_size, Kernel_size), 0)
    # cv2.imshow('Blurred', blurred)

    # ret, thresh = cv2.threshold(img_gray, 127, 255, 0)
    ret, thresh = cv2.threshold(blurred, 127, 255, 0)
    height, width = thresh.shape[:2]
    if show_thresh:
        cv2.imshow('Threshed', thresh)

    # The thresh image is close to pure black and white,
    # close to displaying the path only

    # Look for the black pixels
    # Colors are (b, g, r)
    tiles = []
    for h in range(height - 1, 0, -40):  # begin at the bottom of the image
        first_black = -1
        last_black = -1
        for w in range(width):
            px = thresh[h, w]  # Take the pixel in the threshed image
            if px == 0:  # 0: black, 255: white
                # print("h:{}, w:{}, px {}".format(h, w, px))
                if first_black == -1:
                    first_black = w
                else:
                    last_black = w
        tiles.append((h, first_black, last_black))

    # print("Tiles:", tiles)
    color = (0, 255, 0)  # green
    previous_step = None
    previous_center = None
    first_tile = True
    for step in tiles:
        # print("Step:", step)
        if previous_step is not None:
            bottom, bottom_left, bottom_right = previous_step
            top, top_left, top_right = step
            # Draw a box
            cv2.line(frame, (bottom_left, bottom), (bottom_right, bottom), color, 2)
            cv2.line(frame, (bottom_right, bottom), (top_right, top), color, 2)
            cv2.line(frame, (top_right, top), (top_left, top), color, 2)
            cv2.line(frame, (top_left, top), (bottom_left, bottom), color, 2)
            # a dot in the middle
            center_y = (bottom + top) / 2
            center_x = (((bottom_left + bottom_right) / 2) + ((top_left + top_right) / 2)) / 2
            # print("Plot at x {}, y {}".format(center_x, center_y))
            cv2.circle(frame, (int(center_x), int(center_y)), 10, (0, 0, 255), -1)
            if previous_center is not None:
                # Calculate course
                prev_x, prev_y = previous_center
                course = get_dir(center_x - prev_x, prev_y - center_y)
                print("Course is {}".format(course))
                if first_tile:
                    mess = 'Steer: {:3.0f}{}'.format(course, degree_sign)  # .encode('utf8')
                    # print('\t', mess)
                    cv2.putText(frame, mess, (10, 26), font, 1, (0, 255, 0), 3)
                    first_tile = False
            previous_center = (center_x, center_y)
        previous_step = step

    cv2.imshow('Detected path', frame)

    if cv2.waitKey(1) & 0xFF == ord('q'):  # select the image window and hit 'q' to quit
        break

# When everything is done, release the capture
camera.release()
cv2.destroyAllWindows()

print("Bye!")
