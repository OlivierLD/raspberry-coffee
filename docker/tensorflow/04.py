#!/usr/bin/env python3
from keras.datasets import mnist

(train_images, train_labels), (test_images, test_labels) = mnist.load_data()

my_slice = train_images[10:100]
# my_slice = train_images[10:100, :, :]
# my_slice = train_images[10:100, 0:28, 0:28]
print("MySlice shape:", my_slice.shape)

my_slice = train_images[10:100, 7:-7, 7:-7]
print("MySlice shape:", my_slice.shape)

digit = my_slice[4]

import matplotlib.pyplot as plt
plt.imshow(digit, cmap=plt.cm.binary)
plt.show()
