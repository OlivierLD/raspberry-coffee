#!/usr/bin/env python3
#
# Handwritten figures recognition => classification
# also see https://www.tensorflow.org/api_docs/python/tf/keras and similar pages.
#
# Similar to sample.05.py, but with YOUR own images.
# Model training.h5 must already exist
#
# Great doc at https://medium.com/@ashok.tankala/build-the-mnist-model-with-your-own-handwritten-digits-using-tensorflow-keras-and-python-f8ec9f871fd3
#
# Interactive: https://ashok.tanka.la/assets/examples/mnist/mnist.html
#
# pip install opencv-python
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

print("{} script arguments.".format(len(sys.argv)))

sess = tf_utils.get_TF_session()
devices = sess.list_devices()
print("----- D E V I C E S -----")
for d in devices:
    print(d.name)
print("-------------------------")

model = None
try:
    model = tf.keras.models.load_model('training.h5')
    print(">> Model is now loaded")
except OSError:
    print('Model not found?')
    sys.exit(1)

keepLooping = True
print("Type Q or q to exit the loop")
while keepLooping:
    userInput = input("Enter the image file name (Q to quit) > ")
    # TODO Use Camera Settings to take a real picture snapshot (on Mac use Camera Settings & Photo Booth)
    if userInput != 'Q' and userInput != 'q':
        try:
            img = cv2.imread(userInput, cv2.IMREAD_GRAYSCALE)
            img = cv2.resize(255-img, (28, 28))

            # Show the image, as it's been transformed
            # plt.imshow(img, cmap=plt.cm.binary)
            # plt.show()

            im2arr = np.array(img)
            im2arr = im2arr.reshape(1, 28, 28, 1)
            pred = model.predict_classes(im2arr)
            precision = model.predict(im2arr)
            print("Prediction: it looks like a ", pred, " (", precision[0][np.argmax(precision)] * 100, "% sure ), Nb predictions:", len(precision))
            if platform.system() == 'Darwin':
                sp.run(['say',
                        'It looks like a ' + str(int(pred[0])) + ' to me, I\'m {:2.0f}% sure'.format(precision[0][np.argmax(precision)] * 100)])
            plt.imshow(mpimg.imread(userInput))
            # plt.imshow(img, cmap=plt.cm.binary)
            plt.show()
        except FileNotFoundError:
            print("File", userInput, "not found...")
    else:
        keepLooping = False

print("Bye!")
