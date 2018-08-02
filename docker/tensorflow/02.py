#!/usr/bin/env python3
#
# Run this from the keras directory
# $ python3 examples/oliv/02.py
#
from keras.datasets import mnist

(train_images, train_labels), (test_images, test_labels) = mnist.load_data()

print("Train dim:", train_images.ndim)

def naive_relu(x):
	assert len(x.shape) == 2

	x = x.copy()
	for i in range(x.shape[0]):
		for j in range(x.shape[1]):
			x[i, j] = max(x[i, j], 0)
	return x

def naive_add(x, y):
	assert len(x.shape) == 2
	assert x.shape == y.shape

	x = x.copy()
	for i in range(x.shape[0]):
		for j in range(x.shape[1]):
			x[i, j] += y[i, j]
	return x

digit = train_images[4]
print(naive_relu(digit))

print(naive_add(train_images[0], train_images[1]))
