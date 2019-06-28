#!/usr/bin/env python3
#
# Handwritten figures recognition => classification
# also see https://www.tensorflow.org/api_docs/python/tf/keras and similar pages.
#
# Synopsis:
# - Enter digit images to compose number A
# - Enter operation (+, -, *, /)
# - Enter digit images to compose number B
# - Enter = to get the result
#
# Model training.h5 must already exist
#
# Great doc at https://medium.com/@ashok.tankala/build-the-mnist-model-with-your-own-handwritten-digits-using-tensorflow-keras-and-python-f8ec9f871fd3
#
# Interactive: https://ashok.tanka.la/assets/examples/mnist/mnist.html
#
import tensorflow as tf
import numpy as np
import sys
import warnings
# Python Imaging Library.
# If 'pip install PIL' fails, try 'pip install Pillow'
from PIL import Image
import subprocess as sp
import platform
import matplotlib.pyplot as plt
import matplotlib.image as mpimg

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
strA = ''
operation = ''
strB = ''
while keepLooping:
    mess = "Composing Number A ({}). Enter an image location, or +, -, * or / > ".format(strA)
    if len(operation) > 0:
        mess = "Composing Number B ({}). Enter an image location, or = > ".format(strB)
    userInput = input(mess)
    if userInput != '+' and userInput != '-' and userInput != '*' and userInput != '/' and userInput != '=':
        try:
            print("Opening", userInput)
            img = Image.open(userInput).convert("L")
            img = np.resize(img, (28, 28, 1))
            im2arr = np.array(img)
            im2arr = im2arr.reshape(1, 28, 28, 1)
            pred = model.predict_classes(im2arr)
            if len(operation) == 0:
                strA += str(int(pred[0]))  # np.array2string(pred)
            else:
                strB += str(int(pred[0]))  # np.array2string(pred)
        except FileNotFoundError:
            print("File", userInput, "not found...")
    else:
        if userInput == '=':
            print("Calculating result for ", strA, operation, strB, "...")
            keepLooping = False
            numberA = float(strA)
            numberB = float(strB)
            result = 0
            if operation == '+':
                result = numberA + numberB
            elif operation == '-':
                result = numberA - numberB
            elif operation == '*':
                result = numberA * numberB
            elif operation == '/':
                result = numberA / numberB
            print(strA, operation, strB, "=", result)
            if platform.system() == 'Darwin':
                sp.run(['say',
                        strA + (" plus " if operation == '+'
                                else " minus " if operation == '-'
                                else " multiplied by " if operation == '*'
                                else " divided by ") + strB + " equals " + str(result)])
        else:
            operation = userInput

print("Bye!")
