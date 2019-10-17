#!/usr/bin/env python3
#
# Handwritten figures recognition => classification
# also see https://www.tensorflow.org/api_docs/python/tf/keras and similar pages.
#
# Similar to sample.05.1.py, but with YOUR own images, read from the camera.
# Model training.h5 must already exist
# Uses OpenCV to reshape the images as expected by the model
#
#     SHOW ONE OR MORE HAND-WRITTEN CHARACTERS TO THE CAMERA (will split)
#     HIT 'S' TO HAVE IT RECOGNIZED
#
#     REQUIRES THE MODEL 'training.h5' TO BE AVAILABLE
#
# Great doc at
# https://medium.com/@ashok.tankala/build-the-mnist-model-with-your-own-handwritten-digits-using-tensorflow-keras-and-python-f8ec9f871fd3
#
# Interactive: https://ashok.tanka.la/assets/examples/mnist/mnist.html
#
# https://pypi.org/project/opencv-python/
#
# pip install opencv-python
#
# About matlibplot: https://stackoverflow.com/questions/28269157/plotting-in-a-non-blocking-way-with-matplotlib
#
import tensorflow as tf
import numpy as np
import sys
import time
import warnings
import subprocess as sp
import platform
from imutils import contours
import imutils
import cv2

sys.path.append('../')
import tf_utils

warnings.filterwarnings('ignore')

print("Let's go!")

# Usual yada-yada
tf_version = tf.__version__
print("TensorFlow version", tf_version)
print("Keras version", tf.keras.__version__)
print("OpenCV version", cv2.__version__)

print("{} script arguments.".format(len(sys.argv)))

sess = tf_utils.get_TF_session()
devices = sess.list_devices()
print("----- D E V I C E S -----")
for d in devices:
    print(d.name)
print("-------------------------")

DEBUG = False

# Now we start the job
model = None
try:
    print("\t\tLoading the model...")
    model = tf.keras.models.load_model('training.h5')
    print(">> Model is now loaded")
except OSError as ose:
    print('Model not found?')
    print(ose)
    sys.exit(1)

THRESHOLD_TYPE = {
    "BINARY": 0,
    "BINARY_INVERTED": 1,
    "TRUNCATED": 2,
    "TO_ZERO": 3,
    "TO_ZERO_INVERTED": 4
}


def apply_model(image, show_all_steps=False, kernel_size=15):

    last_image = image
    gray = cv2.cvtColor(last_image, cv2.COLOR_BGR2GRAY)
    if show_all_steps:
        cv2.imshow('Grayed', gray)
    last_image = gray

    blurred = cv2.GaussianBlur(last_image, (kernel_size, kernel_size), 0)
    if show_all_steps:
        cv2.imshow('Blurred', blurred)
    last_image = blurred

    if True:
        threshold_value = 127  # 127: dark conditions, 200: good light conditions
        _, thresh = cv2.threshold(last_image, threshold_value, 255, THRESHOLD_TYPE["BINARY"])
        if show_all_steps:
            cv2.imshow('Threshed', thresh)
        last_image = thresh

    reworked = cv2.resize(255 - last_image, (28, 28))
    last_image = reworked

    # Show the image, as it's been transformed to be processed
    cv2.imshow("As transformed for processing", last_image)

    time.sleep(0.5)

    # To match the model requirements
    im2arr = np.array(last_image)
    im2arr = im2arr.reshape(1, 28, 28, 1)
    pred = model.predict_classes(im2arr)
    precision = model.predict(im2arr)
    return int(pred[0])


def process_image(image, show_all_steps=False, kernel_size=15):

    final_number = 0

    saved_image = image.copy()
    last_image = image
    gray = cv2.cvtColor(last_image, cv2.COLOR_BGR2GRAY)
    if show_all_steps:
        cv2.imshow('Grayed', gray)
    last_image = gray

    blurred = cv2.GaussianBlur(last_image, (kernel_size, kernel_size), 0)
    if show_all_steps:
        cv2.imshow('Blurred', blurred)
    last_image = blurred

    edged = cv2.Canny(last_image, 50, 200, 255)
    last_image = edged

    if show_all_steps:
        cv2.imshow("Edged", edged)

    if True:
        threshold_value = 127  # 127: dark conditions, 200: good light conditions
        _, thresh = cv2.threshold(last_image, threshold_value, 255, THRESHOLD_TYPE["BINARY"])
        if show_all_steps:
            cv2.imshow('Threshed', thresh)
        last_image = thresh

    all_contours = cv2.findContours(last_image.copy(), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    all_contours = imutils.grab_contours(all_contours)
    print("Found {} contours".format(len(all_contours)))

    if show_all_steps:
        cv2.drawContours(image, all_contours, -1, (0, 255, 0), 3)  # in green
        cv2.imshow('Contours', image)

    digit_contours = []

    # loop over the digit area candidates
    for c in all_contours:
        # compute the bounding box of the contour
        (x, y, w, h) = cv2.boundingRect(c)
        print("Found Contours x:{} y:{} w:{} h:{}".format(x, y, w, h))
        # if the contour is sufficiently large, it must be a digit
        if w >= 15 and h >= 50:  # <= That's the tricky part
            print("\tAdding Contours x:{} y:{} w:{} h:{}".format(x, y, w, h))
            digit_contours.append(c)

    print("Retained {}".format(len(digit_contours)))
    # sort the contours from left-to-right, then initialize the
    # actual digits themselves
    digit_contours = contours.sort_contours(digit_contours,
                                            method="left-to-right")[0]
    # loop over each of the digits
    idx = 0
    padding = 10
    for c in digit_contours:
        idx += 1
        # extract the digit ROI
        (x, y, w, h) = cv2.boundingRect(c)
        #
        roi = saved_image[y:y + h, x:x + w]  # <= THIS is the image that will be processed (recognized) later on.
        #
        if show_all_steps:
            cv2.imshow("Digit {}".format(idx), roi)
        #
        # cv2.rectangle(saved_image, (x, y), (x + w, y + h), (0, 255, 0), 2)
        cv2.rectangle(saved_image, (x - padding, y - padding), (x + w + (2 * padding), y + h + (2 * padding)),
                      (0, 255, 0), 2)
        digit = apply_model(roi, show_all_steps, kernel_size)

        cv2.putText(saved_image, str(digit), (x - 10 - padding, y - 10 - padding),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.65, (0, 255, 0), 2)
        #
        final_number = (final_number * 10) + digit
    cv2.imshow("Recognized characters", saved_image)
    print("Final Number is {}".format(final_number))
    print("Prediction: I've read {}".format(final_number))
    if platform.system() == 'Darwin':
        sp.run(['say',
                'I have read {}.'.format(final_number)])


# The core of the program
camera = cv2.VideoCapture(0)

width = 640
height = 640
camera.set(3, width)
camera.set(4, height)

mirror = False
zoom = False
scale = 25  # Zoom scale. Percent of the original (radius). 50 => 100%

keepLooping = True
print("+----------------------------------------------------+")
print("| Type Q, q or Ctrl+C to exit the loop               |")
print("| Type S or s to take a snapshot                     |")
print("| > Select the main image before hitting a key... ;) |")
print("+----------------------------------------------------+")
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
    if key == ord('s'):  # Take snapshot
        print('\t>> Taking snapshot -')  # And invoke model
        # Select ROI
        # Nice ROI summary: https://www.learnopencv.com/how-to-select-a-bounding-box-roi-in-opencv-cpp-python/
        roi = cv2.selectROI(original_image, showCrosshair=False, fromCenter=False)  # Interactive selection
        if DEBUG:
            print("ROI: {}".format(roi))
            print("Selected ROI: {} {} {} {}".format(int(roi[1]), int(roi[1] + roi[3]), int(roi[0]), int(roi[0] + roi[2])))
        try:
            cropped_image = original_image[int(roi[1]):int(roi[1] + roi[3]), int(roi[0]):int(roi[0] + roi[2])]
            cv2.imshow('Selected ROI', cropped_image)
            time.sleep(0.5)
            process_image(cropped_image, False)
        except Exception as ex:  # ROI was canceled?
            print("Oops! {}".format(ex))
            print("Ok, canceled.")

# Releasing resources
camera.release()
cv2.destroyAllWindows()

print("Bye!")
