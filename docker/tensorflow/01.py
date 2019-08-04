#!/usr/bin/env python3
#
# Run this from the keras directory
# $ python3 examples/oliv/01.py
#
from keras.datasets import mnist

(train_images, train_labels), (test_images, test_labels) = mnist.load_data()

print("Training Images", train_images.ndim)

digit = train_images[4]

import matplotlib.pyplot as plt
plt.imshow(digit, cmap=plt.cm.binary)
plt.show()
