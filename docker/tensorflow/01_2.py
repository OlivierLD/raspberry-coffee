#!/usr/bin/env python3
from keras.datasets import mnist
import matplotlib.pyplot as plt

(train_images, train_labels), (test_images, test_labels) = mnist.load_data()

print(train_images.ndim)

for i in range(10):
	digit = train_images[i]

	plt.imshow(digit, cmap=plt.cm.binary)
	plt.show()
