#!/usr/bin/env python3
# From https://www.tensorflow.org/tutorials/keras/basic_classification
#
# Fashion mnist, images classification
# Train the model, then use it.
#
from __future__ import absolute_import, division, print_function

# TensorFlow and tf.keras
import tensorflow as tf
from tensorflow import keras

# Helper libraries
import numpy as np
import matplotlib.pyplot as plt

print("TensorFlow version ", tf.__version__)

fashion_mnist = keras.datasets.fashion_mnist

(train_images, train_labels), (test_images, test_labels) = fashion_mnist.load_data()

class_names = ['T-shirt/top', 'Trouser', 'Pullover', 'Dress', 'Coat', 'Sandal', 'Shirt', 'Sneaker', 'Bag', 'Ankle boot']

print("TrainImages shape", train_images.shape)

print("TrainLabel len", len(train_labels))

print("TrainLabels", train_labels)

print("TestImage shape", test_images.shape)

print("TestLabels len", len(test_labels))

fig = plt.figure()
fig.canvas.set_window_title('First Image')
plt.imshow(train_images[0])
plt.colorbar()
plt.grid(False)
plt.show()

train_images = train_images / 255.0

test_images = test_images / 255.0

fig = plt.figure(figsize=(10, 10))
fig.canvas.set_window_title("25 first examples of the training dataset")
rows = 5
columns = 5
for i in range(rows * columns):
    plt.subplot(rows, columns, i + 1)
    plt.xticks([])
    plt.yticks([])
    plt.grid(False)
    plt.imshow(train_images[i], cmap=plt.cm.binary)
    plt.xlabel(class_names[train_labels[i]])
plt.show()

print("Building model")
model = keras.Sequential([
    keras.layers.Flatten(input_shape=(28, 28)),
    keras.layers.Dense(128, activation=tf.nn.relu),
    keras.layers.Dense(10, activation=tf.nn.softmax)
])

print("Compiling model")
model.compile(optimizer='adam', loss='sparse_categorical_crossentropy', metrics=['accuracy'])

print("-------------------------------")
print("Training model")
print("-------------------------------")
model.fit(train_images, train_labels, epochs=5)
print("Training completed")
model.summary()

test_loss, test_acc = model.evaluate(test_images, test_labels)

print('Test accuracy:', test_acc)

# TODO Save the model here?

print("-------------------------------")
print("No making predictions")
print("-------------------------------")

predictions = model.predict(test_images)
print("First prediction", predictions[0])

print("Best match, category", np.argmax(predictions[0]))

print("TestLabel[0]", test_labels[0])


def plot_image(i, predictions_array, true_label, img):
    predictions_array, true_label, img = predictions_array[i], true_label[i], img[i]
    plt.grid(False)
    plt.xticks([])
    plt.yticks([])

    plt.imshow(img, cmap=plt.cm.binary)

    predicted_label = np.argmax(predictions_array)
    if predicted_label == true_label:
        color = 'blue'
    else:
        color = 'red'

    plt.xlabel("{} {:2.0f}% ({})".format(class_names[predicted_label],
                                         100 * np.max(predictions_array),
                                         class_names[true_label]),
               color=color)


def plot_value_array(i, predictions_array, true_label):
    predictions_array, true_label = predictions_array[i], true_label[i]
    plt.grid(False)
    plt.xticks([])
    plt.yticks([])
    this_plot = plt.bar(range(10), predictions_array, color="#777777")
    plt.ylim([0, 1])
    predicted_label = np.argmax(predictions_array)

    this_plot[predicted_label].set_color('red')
    this_plot[true_label].set_color('blue')


i = 0
fig = plt.figure(figsize=(6, 3))
fig.canvas.set_window_title("Image[0] (prediction)")
plt.subplot(1, 2, 1)
plot_image(i, predictions, test_labels, test_images)
plt.subplot(1, 2, 2)
plot_value_array(i, predictions, test_labels)
plt.show()

i = 12
fig = plt.figure(figsize=(6, 3))
fig.canvas.set_window_title("Image[12] (prediction)")
plt.subplot(1, 2, 1)
plot_image(i, predictions, test_labels, test_images)
plt.subplot(1, 2, 2)
plot_value_array(i, predictions, test_labels)
plt.show()

# Plot the first X test images, their predicted label, and the true label
# Color correct predictions in blue, incorrect predictions in red
num_rows = 5
num_cols = 3
num_images = num_rows * num_cols
fig = plt.figure(figsize=(2 * 2 * num_cols, 2 * num_rows))
fig.canvas.set_window_title("Summary (prediction, test dataset)")
for i in range(num_images):
    plt.subplot(num_rows, 2 * num_cols, 2 * i + 1)
    plot_image(i, predictions, test_labels, test_images)
    plt.subplot(num_rows, 2 * num_cols, 2 * i + 2)
    plot_value_array(i, predictions, test_labels)
plt.show()

# Grab an image from the test dataset
img = test_images[0]

print("Image Shape", img.shape)

# Add the image to a batch where it's the only member.
img = (np.expand_dims(img, 0))

print("Image Shape", img.shape)

predictions_single = model.predict(img)

print("Single Prediction", predictions_single)

plot_value_array(0, predictions_single, test_labels)
_ = plt.xticks(range(10), class_names, rotation=45)

print("Best guess, category", np.argmax(predictions_single[0]), ":", class_names[np.argmax(predictions_single[0])])
