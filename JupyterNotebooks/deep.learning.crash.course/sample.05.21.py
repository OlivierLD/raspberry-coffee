#!/usr/bin/env python3
#
# Handwritten figures recognition => classification
# also see https://www.tensorflow.org/api_docs/python/tf/keras and similar pages.
#
# Similar to sample.05.py, but with YOUR own images, read from the camera.
# Model training.h5 must already exist
# Uses OpenCV to reshape the images as expected by the model
#
#     SHOW AN HAND-WRITTEN CHARACTER TO THE CAMERA
#     HIT 'S' TO HAVE IT RECOGNIZED
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
import cv2

import tf_utils

warnings.filterwarnings('ignore')
Kernel_size = 31


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


def apply_model(image):
    img = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    blurred = cv2.GaussianBlur(img, (Kernel_size, Kernel_size), 0)
    # cv2.imshow('Blurred', blurred)

    # ret, thresh = cv2.threshold(img_gray, 127, 255, 0)
    _, thresh = cv2.threshold(blurred, 127, 255, 0)

    # reworked = cv2.resize(255-img, (28, 28))
    reworked = cv2.resize(255 - thresh, (28, 28))

    # Show the image, as it's been transformed to be processed
    cv2.imshow("As transformed for processing", reworked)

    # Below: pure black and white (not necessary here)
    # thresh = 127
    # img_bw = cv2.threshold(img, thresh, 255, cv2.THRESH_BINARY)[1]
    # # cv2.imwrite('blackwhite.png', im_bw)
    # plt.imshow(img_bw, cmap=plt.cm.binary)
    # plt.show()

    # To match the model requirements
    im2arr = np.array(reworked)
    im2arr = im2arr.reshape(1, 28, 28, 1)
    pred = model.predict_classes(im2arr)
    precision = model.predict(im2arr)
    print("Prediction: it looks like a ",
          str(int(pred[0])),
          " (",
          precision[0][np.argmax(precision)] * 100,
          "% sure ), Nb predictions:",
          len(precision))
    if platform.system() == 'Darwin':
        sp.run(['say',
                'It looks like a ' +
                str(int(pred[0])) +
                ' to me, I\'m {:2.0f}% sure'.format(precision[0][np.argmax(precision)] * 100)])


# The core of the program
camera = cv2.VideoCapture(0)

width = 480
height = 640
camera.set(3, width)
camera.set(4, height)

keepLooping = True
print("+---------------------------------------------------------+")
print("| Type Q or q to exit the loop, S or s to take a snapshot |")
print("+---------------------------------------------------------+")
while keepLooping:

    _, frame = camera.read()
    time.sleep(0.1)
    try:
        # Original image
        cv2.imshow('Original', frame)
    except Exception as ex:
        print("Oops! {}".format(ex))

    key = cv2.waitKey(1) & 0xFF
    # print("Key : {}".format(key))
    if key == ord('q'):  # select the image window and hit 'q' to quit
        keepLooping = False
    if key == ord('s'):  # Take snapshot
        print('\t\tTaking snapshot -')  # Invoke model
        apply_model(frame)

# Releasing resources
camera.release()
cv2.destroyAllWindows()

print("Bye!")
