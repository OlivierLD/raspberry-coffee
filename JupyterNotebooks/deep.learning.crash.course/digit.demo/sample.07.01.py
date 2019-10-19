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
import math

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
verboseContour = False
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
        image = original_image.copy()

        img_gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        cv2.imshow('Gray', img_gray)

        ret, thresh = cv2.threshold(img_gray, 127, 255, 0)
        height, width = thresh.shape[:2]
        # cv2.imshow('Thresh {}x{}'.format(width, height), thresh)
        cv2.imshow('Thresh', thresh)
        try:
            # Only 2 prms returned!!!
            contours, hierarchy = cv2.findContours(thresh, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
            if verboseContour:
                print("Contours were found!! list of {} elements (ndarrays)".format(len(contours)))
                for i in range(len(contours)):
                    nb_points = len(contours[i])
                    if nb_points > 50:
                        print("Contour {} has {} points".format(i, nb_points))
        except BaseException as ex:
            print("Ah ben merde {}".format(ex))

        # # threshold image
        # # this step is necessary when you work with contours
        # ret, threshed_img = cv2.threshold(cv2.cvtColor(img, cv2.COLOR_BGR2GRAY), 127, 255, cv2.THRESH_BINARY)
        # # find contours in image
        # image, contours, hier = cv2.findContours(threshed_img, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        #
        # Find the biggest contour
        biggest_contour = None
        biggest_size = 0
        for cnt in contours:
            (x, y, w, h) = cv2.boundingRect(cnt)
            size = w * h
            if size > biggest_size:
                biggest_size = size
                biggest_contour = cnt

        # print("Biggest size is {}".format(biggest_size))
        (x, y, w, h) = cv2.boundingRect(biggest_contour)
        if w > 10 and h > 10:
            # calculate epsilon base on contour's perimeter
            # contour's perimeter is returned by cv2.arcLength
            epsilon = 0.01 * cv2.arcLength(biggest_contour, True)
            # get approx polygons
            approx = cv2.approxPolyDP(biggest_contour, epsilon, True)
            # draw approx polygons
            cv2.drawContours(image, [approx], -1, (0, 255, 0), 2)  # green

            # hull is convex shape as a polygon
            hull = cv2.convexHull(biggest_contour)
            # print("Found Contours x:{} y:{} w:{} h:{}, hull has {} elements, approx has {} elements.".format(x, y, w, h, len(hull), len(approx)))
            cv2.drawContours(image, [hull], -1, (0, 0, 255), 4)  # red
            nb_points = 0
            prev_point = None
            min_dist = 100  # TODO Make it depend on the image size?
            for i in range(len(hull)):
                # print("Pt {}".format(hull[i][0]))   # x, y
                pt = (hull[i][0][0], hull[i][0][1])
                if pt[1] < (image.shape[0] / 4):   # From the topq
                    if prev_point is not None:
                        delta_x = prev_point[0] - pt[0]
                        delta_y = prev_point[1] - pt[1]
                        dist = math.sqrt((delta_x * delta_x) + (delta_y * delta_y))
                        # print("Dist between {} and {}: {}".format(prev_point, pt, dist))
                    if prev_point is None or (prev_point is not None and dist > min_dist):
                        # if prev_point is None:
                        #     print("\t>> Adding first point {}".format(pt))
                        cv2.putText(image, "{},{}".format(pt[0], pt[1]), pt, cv2.FONT_HERSHEY_SIMPLEX, 0.35, (0, 0, 255), 1)
                        nb_points += 1
                        prev_point = pt
            print("Found {} finger(s)".format(nb_points))
            # print("Hull {}".format(hull))
            # print("Approx {}".format(approx))
            #
            # Count the vertex above the middle of the figure
            # print("Image h:{} x w:{}, Hull:\n{}".format(image.shape[0], image.shape[1], hull))

        cv2.imshow('Detected Contours', image)

    except Exception as ex:
        print("Oops! {}".format(ex))

    key = cv2.waitKey(1) & 0xFF
    # print("Key : {}".format(key))
    if key == ord('q'):  # select the image window and hit 'q' to quit
        keepLooping = False
    if key == ord('s'):  # Take snapshot
        print('\t>> Taking snapshot -')  # Saving imageq

# Releasing resources
camera.release()
cv2.destroyAllWindows()

print("Bye!")
