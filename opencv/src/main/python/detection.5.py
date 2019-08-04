#
# pip install opencv-python
#
import cv2
import numpy as np

print('Using OpenCV version', cv2.__version__)

verboseContour = False
verbose = False

Kernel_size = 31  # 15

#
# On ONE image, static.
# Contour approach
#
print("Reading original image...")
image = cv2.imread("path.jpg")  # , cv2.IMREAD_GRAYSCALE)
print("Image is a {}, its shape is {} (h, w, ch)".format(type(image), image.shape))

cv2.imshow('Original', image)
# cv2.imwrite('./snap1.jpg', image)

img_gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
cv2.imshow('Gray', img_gray)

blurred = cv2.GaussianBlur(img_gray, (Kernel_size, Kernel_size), 0)
cv2.imshow('Blurred', blurred)

# ret, thresh = cv2.threshold(img_gray, 127, 255, 0)
ret, thresh = cv2.threshold(blurred, 127, 255, 0)
height, width = thresh.shape[:2]

cv2.imshow('Thresh WxH {}x{}'.format(width, height), thresh)
# cv2.imshow('Thresh', thresh)

try:
    # Only 2 prms returned!!!
    contours, hierarchy = cv2.findContours(thresh, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)

    print("Contours were found: list of {} elements (ndarrays)".format(len(contours)))
    if verboseContour:
        biggest_contour_idx = -1
        biggest_contour_num = 0
        for i in range(len(contours)):
            nb_points = len(contours[i])
            if nb_points > biggest_contour_num:
                biggest_contour_idx = i
                biggest_contour_num = nb_points

        print("Contour #{} has {} points".format(biggest_contour_idx, biggest_contour_num))
        print("Contour #{}:".format(biggest_contour_idx))
        for i in range(len(contours[biggest_contour_idx])):
            print("Element #{}, type:{}".format(i, type(contours[biggest_contour_idx][i])))
            print(contours[biggest_contour_idx][i])

    contours = sorted(contours, key=cv2.contourArea)

    print("Getting moments...")
    moment = cv2.moments(thresh)
    print("Moment:", moment)
    X = int(moment["m10"] / moment["m00"])
    Y = int(moment["m01"] / moment["m00"])
    # Draw center of the image
    cv2.circle(image, (X, Y), 15, (205, 114, 101), 3)

    # Print contours on original image
    cv2.drawContours(image, contours, -1, (0, 255, 0), 3)  # in green
    cv2.imshow('Contours', image)

    # Create blank image, print contours on it
    blank_image = np.zeros((height, width, 3), np.uint8)
    color = (255, 255, 255)   # white
    blank_image[:] = color
    #
    cv2.drawContours(blank_image, contours, -1, (0, 0, 0), 3)  # in black on white
    cv2.imshow('Blank Contours', blank_image)

except ValueError as ve:
    # keep going
    if verbose:
        print(ve)
finally:
    if verbose:
        print('Moving on')

# input("Press [Enter] to end")
print("Hit any key to finish")
cv2.waitKey(0)

cv2.destroyAllWindows()

