#
# pip install opencv-python
#
import cv2
# import numpy as np
import math

print('Using OpenCV version', cv2.__version__)

verboseContour = False
verbose = False

Kernel_size = 31  # 15

#
# On ONE image, static.
# Pixel by pixel approach
# - Take am image
# - resize it
# - make it gray
# - blur it
# - threshold it
# - get the first and last black pixel every x lines
# - get center of the 'boxes'
# - calculate heading from center to center
#

# img_path = "path.jpg"
img_path = "path.2.png"
#


def get_dir(x, y):
    direction = math.degrees(math.atan2(x, y))
    while direction < 0:
        direction += 360
    return direction


print("Reading original image...")
image = cv2.imread(img_path)  # , cv2.IMREAD_GRAYSCALE)
image = cv2.resize(image, (480, 240))
print("Image is a {}, its shape is {} (h, w, ch)".format(type(image), image.shape))

# cv2.imshow('Original', image)

img_gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
# cv2.imshow('Gray', img_gray)

blurred = cv2.GaussianBlur(img_gray, (Kernel_size, Kernel_size), 0)
# cv2.imshow('Blurred', blurred)

# ret, thresh = cv2.threshold(img_gray, 127, 255, 0)
ret, thresh = cv2.threshold(blurred, 127, 255, 0)
height, width = thresh.shape[:2]

# The thresh image is close to pure black and white,
# close to displaying the path only

new_threshed = cv2.cvtColor(thresh, cv2.COLOR_GRAY2RGB)
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
            # color = (0, 255, 255)    # yellow
            # cv2.line(new_threshed, (w, h), (w, h), color, 2)  # A dot
    tiles.append((h, first_black, last_black))

# print("Tiles:", tiles)
color = (0, 255, 0)  # green
previous_step = None
previous_center = None
for step in tiles:
    # print("Step:", step)
    if previous_step is not None:
        bottom, bottom_left, bottom_right = previous_step
        top, top_left, top_right = step
        # Draw a box
        cv2.line(new_threshed, (bottom_left, bottom), (bottom_right, bottom), color, 2)
        cv2.line(new_threshed, (bottom_right, bottom), (top_right, top), color, 2)
        cv2.line(new_threshed, (top_right, top), (top_left, top), color, 2)
        cv2.line(new_threshed, (top_left, top), (bottom_left, bottom), color, 2)
        # a dot in the middle
        center_y = (bottom + top) / 2
        center_x = (((bottom_left + bottom_right) / 2) + ((top_left + top_right) / 2)) / 2
        # print("Plot at x {}, y {}".format(center_x, center_y))
        cv2.circle(new_threshed, (int(center_x), int(center_y)), 10, (0, 0, 255), -1)
        if previous_center is not None:
            # Calculate course
            prev_x, prev_y = previous_center
            course = get_dir(center_x - prev_x, prev_y - center_y)
            print("Course is {}".format(course))
        previous_center = (center_x, center_y)
    previous_step = step

cv2.imshow('Colored Thresh', new_threshed)

# cv2.imshow('Thresh WxH {}x{}'.format(width, height), thresh)
# cv2.imshow('Thresh', thresh)

input("Press [Enter] to end")
print("Hit any key to finish")
cv2.waitKey(0)

cv2.destroyAllWindows()

