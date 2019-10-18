#
# Good paper at https://loctv.wordpress.com/2017/02/17/learn-opencv3-python-contours-convex-contours-bounding-rect-min-area-rect-min-enclosing-circle-approximate-bounding-polygon/
#
import numpy
import cv2
import math

verboseContour = True

# read and downscale image
# image = cv2.pyrDown(cv2.imread('hand.2.jpg', cv2.IMREAD_UNCHANGED))
image = cv2.imread('hand.2.jpg')   # , cv2.IMREAD_UNCHANGED)
# cv2.imshow('original', image)
# cv2.waitKey(0)

img_gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
# cv2.imshow('Gray', img_gray)

ret, thresh = cv2.threshold(img_gray, 127, 255, 0)
height, width = thresh.shape[:2]
# cv2.imshow('Thresh {}x{}'.format(width, height), thresh)
# cv2.imshow('Thresh', thresh)
try:
    # Only 2 prms returned!!!
    contours, hierarchy = cv2.findContours(thresh, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    if verboseContour:
        print("Contours were found!! list of {} elements (ndarrays)".format(len(contours)))
        for i in range(len(contours)):
            nb_points = len(contours[i])
            if nb_points > 50:
                print("Contour {} has {} points".format(i, nb_points))
finally:
    # Ok!
    print("End of contour detection")

# # threshold image
# # this step is necessary when you work with contours
# ret, threshed_img = cv2.threshold(cv2.cvtColor(img, cv2.COLOR_BGR2GRAY), 127, 255, cv2.THRESH_BINARY)
# # find contours in image
# image, contours, hier = cv2.findContours(threshed_img, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
#
for cnt in contours:
    (x, y, w, h) = cv2.boundingRect(cnt)
    if w > 10 and h > 10:
        # calculate epsilon base on contour's perimeter
        # contour's perimeter is returned by cv2.arcLength
        epsilon = 0.01 * cv2.arcLength(cnt, True)
        # get approx polygons
        approx = cv2.approxPolyDP(cnt, epsilon, True)
        # draw approx polygons
        cv2.drawContours(image, [approx], -1, (0, 255, 0), 1)  # green

        # hull is convex shape as a polygon
        hull = cv2.convexHull(cnt)
        print("Found Contours x:{} y:{} w:{} h:{}, hull has {} elements, approx has {} elements.".format(x, y, w, h, len(hull), len(approx)))
        cv2.drawContours(image, [hull], -1, (0, 0, 255))  # red
        nb_points = 0
        prev_point = None
        for i in range(len(hull)):
            # print("Pt {}".format(hull[i][0]))   # x, y
            pt = (hull[i][0][0], hull[i][0][1])
            if pt[1] < (image.shape[0] / 2):
                if prev_point is not None:
                    delta_x = prev_point[0] - pt[0]
                    delta_y = prev_point[1] - pt[1]
                    dist = math.sqrt((delta_x * delta_x) + (delta_y * delta_y))
                    print("Dist between {} and {}: {}".format(prev_point, pt, dist))
                if prev_point is None or (prev_point is not None and dist > 20):
                    if prev_point is None:
                        print("\t>> Adding first point {}".format(pt))
                    cv2.putText(image, "{},{}".format(pt[0], pt[1]), pt,
                                cv2.FONT_HERSHEY_SIMPLEX, 0.35, (0, 0, 255), 1)
                    nb_points += 1
                    prev_point = pt
        print("Found {} finger(s)".format(nb_points))
        # print("Hull {}".format(hull))
        # print("Approx {}".format(approx))
        #
        # Count the vertex above the middle of the figure
        # print("Image h:{} x w:{}, Hull:\n{}".format(image.shape[0], image.shape[1], hull))

cv2.imshow('contours', image)
ESC = 27

print("Hit [ESC] to quit")
while True:
    keycode = cv2.waitKey()
    if keycode != -1:
        keycode &= 0xFF
        if keycode == ESC:
            break

cv2.destroyAllWindows()
