#!/usr/bin/env python3
#
# Handwritten figures recognition => classification
# also see https://www.tensorflow.org/api_docs/python/tf/keras and similar pages.
#
# Similar to sample.05.py, but with YOUR own images.
# Model training.h5 must already exist
# Uses OpenCV to reshape the images as expected by the model
#
# Great doc at https://medium.com/@ashok.tankala/build-the-mnist-model-with-your-own-handwritten-digits-using-tensorflow-keras-and-python-f8ec9f871fd3
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
import warnings
import subprocess as sp
import platform
import matplotlib.pyplot as plt
import matplotlib.image as mpimg
import cv2

import tf_utils

warnings.filterwarnings('ignore')

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

model = None
try:
    print("Loading the model...")
    model = tf.keras.models.load_model('training.h5')
    print(">> Model is now loaded")
except OSError as ose:
    print('Model not found?')
    print(ose)
    sys.exit(1)

keepLooping = True
print("Type Q or q to exit the loop")
while keepLooping:
    userInput = input("Enter the image file name (Q to quit) > ")
    if userInput != 'Q' and userInput != 'q':
        if userInput != '':
            try:
                # Original image
                fig = plt.figure()
                fig.canvas.set_window_title("Original")
                plt.imshow(mpimg.imread(userInput))
                plt.show()
                """
                This allows ANY image to be used, 
                not only the ones the model has been trained with
                """
                img = cv2.imread(userInput, cv2.IMREAD_GRAYSCALE)
                img = cv2.resize(255-img, (28, 28))

                # Show the image, as it's been transformed to be processed
                fig = plt.figure()
                fig.canvas.set_window_title("As transformed for processing")
                plt.imshow(img, cmap=plt.cm.binary)
                plt.draw()
                plt.pause(0.001)
                # plt.show()

                # Below: pure black and white (not necessary here)
                # thresh = 127
                # img_bw = cv2.threshold(img, thresh, 255, cv2.THRESH_BINARY)[1]
                # # cv2.imwrite('blackwhite.png', im_bw)
                # plt.imshow(img_bw, cmap=plt.cm.binary)
                # plt.show()

                # To match the model requirements
                im2arr = np.array(img)
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
            except FileNotFoundError:
                print("File", userInput, "not found...")
        else:
            print("Please enter something...")
    else:
        keepLooping = False

print("Bye!")
