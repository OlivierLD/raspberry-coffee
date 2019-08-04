import cv2
import numpy as np

image = cv2.imread('snap.jpg')
if image is not None:
    # I just resized the image to a quarter of its original size
    image = cv2.resize(image, (0, 0), None, .25, .25)

    grey = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    # Make the grey scale image have three channels
    grey_3_channel = cv2.cvtColor(grey, cv2.COLOR_GRAY2BGR)

    numpy_vertical = np.vstack((image, grey_3_channel))
    numpy_horizontal = np.hstack((image, grey_3_channel))

    numpy_vertical_concat = np.concatenate((image, grey_3_channel), axis=0)
    numpy_horizontal_concat = np.concatenate((image, grey_3_channel), axis=1)

    cv2.imshow('Main', image)
    cv2.imshow('Numpy Vertical', numpy_vertical)
    cv2.imshow('Numpy Horizontal', numpy_horizontal)
    cv2.imshow('Numpy Vertical Concat', numpy_vertical_concat)
    cv2.imshow('Numpy Horizontal Concat', numpy_horizontal_concat)

    cv2.waitKey()
else:
    print("Image not found...")
