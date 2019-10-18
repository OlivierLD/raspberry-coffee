#
# Good paper at https://loctv.wordpress.com/2017/02/17/learn-opencv3-python-contours-convex-contours-bounding-rect-min-area-rect-min-enclosing-circle-approximate-bounding-polygon/
#
import numpy
import cv2

verboseContour = True

# read and downscale image
image = cv2.pyrDown(cv2.imread('hand.and.fingers.jpg', cv2.IMREAD_UNCHANGED))
cv2.imshow('original', image)
cv2.waitKey(0)

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
    # calculate epsilon base on contour's perimeter
    # contour's perimeter is returned by cv2.arcLength
    epsilon = 0.01 * cv2.arcLength(cnt, True)
    # get approx polygons
    approx = cv2.approxPolyDP(cnt, epsilon, True)
    # draw approx polygons
    cv2.drawContours(image, [approx], -1, (0, 255, 0), 1)

    # hull is convex shape as a polygon
    hull = cv2.convexHull(cnt)
    cv2.drawContours(image, [hull], -1, (0, 0, 255))

cv2.imshow('contours', image)
ESC = 27

while True:
    keycode = cv2.waitKey()
    if keycode != -1:
        keycode &= 0xFF
        if keycode == ESC:
            break

cv2.destroyAllWindows()
