#!/usr/bin/env python3
import tensorflow as tf
import numpy as np
import matplotlib.pyplot as plt
import random
import sys

print("{} script arguments.".format(len(sys.argv)))

print("\nUsage is ")
print("\tpython {} [L]".format(sys.argv[0]))
print("\tL is to Load the already trained model.")
print("\tNo parameter will train (and save) the model.\n")

loadOnly = False
if len(sys.argv) > 1 and sys.argv[1] == 'L':
	loadOnly = True
	print("Will load the model, not train it.")

if not loadOnly:
	print("We are going to train a network to recognize hand-written digits.")
	print("And then we'll see if it is working...")
	print("The network as 2 layers, 512, and than 10 neurons, fully connected.")
	print("Let's go!")

sess = tf.Session()
devices = sess.list_devices()
print("----- D E V I C E S -----")
for d in devices:
    print(d.name)
print("-------------------------")

# Hand writing samples (digit)
print("\nNow importing the test and training datasets...")
mnist = tf.keras.datasets.mnist
(x_train, y_train), (x_test, y_test) = mnist.load_data()
# |       |          |       |
# |       |          |       Test labels
# |       |          Test images
# |       Train labels
# Train images
#
x_train, x_test = x_train / 255.0, x_test / 255.0
print("Import completed, displaying a random set of data, close it to move on.")

if not loadOnly:
	start_idx = random.randint(0, len(x_train)) - 1

	fig = plt.figure(figsize=(10, 10))
	fig.canvas.set_window_title("25 examples of the training dataset")
	for i in range(25):
		plt.subplot(5, 5, i + 1)
		plt.xticks([])
		plt.yticks([])
		plt.grid(False)
		plt.imshow(x_train[start_idx + i], cmap=plt.cm.binary)
		plt.xlabel(y_train[start_idx + i])
	plt.show()

	model = tf.keras.models.Sequential([
		tf.keras.layers.Flatten(),
		tf.keras.layers.Dense(512, activation=tf.nn.relu),
		tf.keras.layers.Dropout(0.2),
		tf.keras.layers.Dense(10, activation=tf.nn.softmax)
	])
	model.compile(optimizer='adam',
				  loss='sparse_categorical_crossentropy',
				  metrics=['accuracy'])

	# model.summary()

	model.fit(x_train, y_train, epochs=5)
	model.evaluate(x_test, y_test)
	#
	model.save('training.h5')
else:
	model = tf.keras.models.load_model('training.h5')
#
# See https://medium.com/tensorflow/hello-deep-learning-fashion-mnist-with-keras-50fcff8cd74a
#
print("Training Images", len(x_train), "elements, dim", x_train.ndim)
print("Testing Images ", len(x_test), "elements, dim", x_test.ndim)

keepLooping = True
# test_idx = random.randint(0, len(x_test)) - 1
print("Type Q or q to exit the loop")
while keepLooping:
	userInput = input("Enter an index between 0 and {} (Q to quit) > ".format(len(x_test) - 1))
	if userInput != 'Q' and userInput != 'q':
		try:
			test_idx = int(userInput)
			if test_idx < 0 or test_idx >= len(x_test):
				print("We said between 0 and {} and you said {}. Try again.".format(len(x_test) - 1, test_idx))
			else:
				digit = x_test[test_idx]
				print("Test index {} ...".format(test_idx))
				plt.imshow(digit, cmap=plt.cm.binary)
				plt.show()

				predictions = model.predict(x_test)

				print("We have", len(predictions), "predictions")
				print("First prediction", predictions[test_idx])
				print("Best match, category", np.argmax(predictions[test_idx]),
					  predictions[test_idx][np.argmax(predictions[test_idx])])
				print("-----------------------------")
				print("It's a", y_test[test_idx],
					  "({:2.0f}% sure).".format(100 * predictions[test_idx][np.argmax(predictions[test_idx])]))
				print("-----------------------------")
		except ValueError:
			print("Bad integer..., try again")
	else:
		keepLooping = False

print("Bye!")
