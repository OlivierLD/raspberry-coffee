#!/usr/bin/env python3
#
# Handwritten figures recognition => classification
# Matplotlib doc at https://matplotlib.org/3.1.0/api/_as_gen/matplotlib.pyplot.html
# also see https://www.tensorflow.org/api_docs/python/tf/keras and similar pages.
#
# Model training. Convolutional Network (aka convnet)
# Model can be saved from here too.
#
# For the figures images, see https://github.com/myleott/mnist_png
# images are 28px x 28px big.
#
import tensorflow as tf
from tensorflow.keras import models
from tensorflow.keras import layers
from tensorflow.keras.utils import to_categorical
import numpy as np
import matplotlib.pyplot as plt
import random
import sys
import warnings
import subprocess as sp
import platform

sys.path.append('../../')
import tf_utils

warnings.filterwarnings('ignore')

tf_version = tf.__version__
print("TensorFlow version", tf_version)
print("Keras version", tf.keras.__version__)

print("{} script arguments.".format(len(sys.argv)))

# Evaluate user's parameters
loadOnly = False  # Load model, do not train it (if it is there already)
if len(sys.argv) > 1 and sys.argv[1] == '--help':
    print("\nUsage is ")
    print("\tpython {} [--help | L]".format(sys.argv[0]))
    print("\tL is to Load the already trained model.")
    print("\tNo parameter will train (and save) the model.")
    print("\t--help will display this content.\n")
    sys.exit()

if len(sys.argv) > 1 and sys.argv[1] == 'L':
    loadOnly = True
    print("Will load the model, not train it.")

if not loadOnly:
    print("We are going to train a network to recognize handwritten figures.")
    print("And then we'll see if it is working...")
    print("Let's go!")

sess = tf_utils.get_TF_session()
devices = sess.list_devices()
print("----- D E V I C E S -----")
for d in devices:
    print(d.name)
print("-------------------------")

# Hand writing samples (digit)
print("\nNow importing the test and training datasets...")
# MNIST DataSet
# See https://en.wikipedia.org/wiki/MNIST_database
# MNIST: Modified National Institute of Standards and Technology
mnist = tf.keras.datasets.mnist
(train_images, train_labels), (test_images, test_labels) = mnist.load_data()
# |            |               |            |
# |            |               |            Test labels
# |            |               Test images
# |            Train labels
# Train images
#
print(">> Before reshaping:")
print("Train images shape: {}, {} inputs".format(train_images.shape, len(train_images)))
print("Test images shape : {}, {} inputs".format(test_images.shape, len(test_images)))
print("Train labels      : {}, {} labels".format(train_labels.shape, len(train_labels)))
print("Test labels       : {}, {} labels".format(test_labels.shape, len(test_labels)))

# Reshaping (same cardinality)
train_images = train_images.reshape((60000, 28, 28, 1))  # Adding nb Channels
train_images = train_images.astype('float32') / 255  # Resetting in range float [0, 1] (instead of int [0, 255]

test_images = test_images.reshape((10000, 28, 28, 1))  # Adding nb Channels
test_images = test_images.astype('float32') / 255  # Resetting in range float [0, 1] (instead of int [0, 255]

# to_categorical adds a second dimension, number of categories (labels)
train_labels = to_categorical(train_labels)
test_labels = to_categorical(test_labels)

print(">> After reshaping:")
print("Train images shape: {}, {} inputs".format(train_images.shape, len(train_images)))
print("Test images shape : {}, {} inputs".format(test_images.shape, len(test_images)))
print("Train labels      : {}, {} labels".format(train_labels.shape, len(train_labels)))
print("Test labels       : {}, {} labels".format(test_labels.shape, len(test_labels)))

if not loadOnly:  # Model to be trained
    display_samples = True
    if display_samples:
        print("----------------\n" +
              "Import completed, displaying a random set of data, once displayed, close the image to move on.")
        start_idx = random.randint(0, len(train_images)) - 1
        print("Starting sample display at index {}".format(start_idx))

        fig = plt.figure(figsize=(5, 6))
        fig.canvas.set_window_title("Examples and labels of the training dataset")
        rows = 7
        columns = 7
        for i in range(rows * columns):
            subplot = plt.subplot(rows, columns, i + 1)
            plt.xticks([])
            plt.yticks([])
            plt.grid(False)
            digit = train_images[start_idx + i]
            digit.astype('float32')
            pixels = digit.reshape((28, 28))
            plt.imshow(pixels, cmap='gray')
            label = train_labels[start_idx + i]
            # print("Label Shape:{}, label: {}".format(label.shape, label))
            plt.xlabel(np.argmax(label))
        # subplot.set_title(i + start_idx)
        plt.show()
    #
    # Define model here
    # =================
    #
    model = models.Sequential()
    model.add(layers.Conv2D(32, (3, 3), activation='relu', input_shape=(28, 28, 1), name='Conv2D-one'))
    model.add(layers.MaxPooling2D((2, 2)))
    model.add(layers.Conv2D(64, (3, 3), activation='relu', name='Conv2D-two'))
    model.add(layers.MaxPooling2D((2, 2)))
    model.add(layers.Conv2D(64, (3, 3), activation='relu', name='Conv2D-three'))

    model.summary()

    # First layer (Flatten) takes arrays of whatever comes from above
    # Last layer has 10 neurons, because we have 10 categories (0-9 digits)
    # Dropout is here to avoid over-fitting -- See below what dropout would do here, on the accuracy.
    # SoftMax layer will dispatch the value so the highest is the one to choose,
    # and its value the percentage of reliability
    model.add(layers.Flatten())
    model.add(layers.Dense(64, activation='relu', name='Dense64-one'))  # 64, like above
    # model.add(layers.Dropout(0.2))  # Was not there, this is a test
    # With and without dropout (values may vary, test values are randomly chosen):
    # No Dropout:  final test accuracy is 0.9914. Lowest confidence if 43.16 % (index 3727).
    # Dropout 0.2: final test accuracy is 0.9926. Lowest confidence if 34.38 % (index 3060).
    # Here, Dropout seem not to have a noticeable impact
    model.add(layers.Dense(10, activation='softmax'))  # 10: [0..9]

    model.summary()

    model.compile(optimizer='rmsprop',  # TODO test other optimizers. rmsprop does better than adam here
                  loss='categorical_crossentropy',
                  metrics=['accuracy'])

    epochs = 5
    print("----------------------------------")
    print("Starting the training, on {} epochs".format(epochs))
    print("----------------------------------")
    model.fit(train_images, train_labels, epochs=epochs, batch_size=64, verbose=1)

    test_loss, test_acc = model.evaluate(test_images, test_labels)
    print("Test Accuracy: {}".format(test_acc))

    show_details = False
    if show_details:  # Display model details
        # config = model.get_config()
        # from keras.models import model_from_json
        json_string = model.to_json()
        print("Model, json format: {}".format(json_string))
        for layer in model.layers:
            try:
                weights = layer.get_weights()[0]
                biases = layer.get_weights()[1]
                print("Weights: {}\nBiases: {}".format(weights, biases))
            except Exception as exception:
                print("Oops: {}".format(exception))

    print("Model evaluate:")
    model.evaluate(test_images, test_labels)
    #
    # TODO Display values on diagram, compare with Dense network only (no convnet).
    #
    model.save('convnet.h5')
    print("Model was saved")
else:
    model = tf.keras.models.load_model('convnet.h5')
    print(">> Model is now loaded")
#
# See https://medium.com/tensorflow/hello-deep-learning-fashion-mnist-with-keras-50fcff8cd74a
#
print("{} Training Images, dim {}".format(len(train_images), train_images.ndim))
print("{} Testing Images, dim {} ".format(len(test_images), test_images.ndim))

# Prediction is ALL here. In one shot, on al the test data.
predictions = model.predict(test_images)
print("We have {} predictions".format(len(predictions)))

find_lowest = True
if find_lowest:
    lowest_confidence = 100.0
    lowest_confidence_idx = -1
    for idx in range(len(predictions)):
        confidence = 100 * predictions[idx][np.argmax(predictions[idx])]
        if confidence < 90:
            print("Confidence {} at index {}".format(confidence, idx))
        if confidence < lowest_confidence:
            lowest_confidence = confidence
            lowest_confidence_idx = idx
    print("Lowest Confidence {}, at index {}".format(lowest_confidence, lowest_confidence_idx))

keep_looping = True
# test_idx = random.randint(0, len(x_test)) - 1
print("Type Q or q to exit the loop")
while keep_looping:
    user_input = input("Enter an index between 0 and {} (Q to quit) > ".format(len(test_images) - 1))
    if user_input != 'Q' and user_input != 'q':
        try:
            test_idx = int(user_input)
            if test_idx < 0 or test_idx >= len(test_images):
                print("We said between 0 and {} and you said {}. Try again.".format(len(test_images) - 1, test_idx))
            else:
                # Display the image to 'guess'
                digit = test_images[test_idx]
                print("Input shape: {}".format(digit.shape))
                digit.astype('float32')
                pixels = digit.reshape((28, 28))
                plt.imshow(pixels, cmap='gray')
                print("Test index {} ... image of {} rows of {} bytes.".format(test_idx, len(digit), len(digit[0])))
                # with open('digit_{}.png'.test_idx, 'wb') as f:
                #     f.write(digit)
                plt.show()
                print("This prediction: {}".format(predictions[test_idx]))  # SoftMax
                print("Best match {}, category (%) {}".format(np.argmax(predictions[test_idx]),
                                                              predictions[test_idx][np.argmax(predictions[test_idx])]))
                print("-----------------------------")
                print("It's a {} ({:2.2f}% sure).".format(
                    np.argmax(predictions[test_idx]),
                    100 * predictions[test_idx][np.argmax(predictions[test_idx])]))
                print("-----------------------------")
                say_it = True
                if say_it and platform.system() == 'Darwin':  # On Mac (use speech on Debian/Raspbian)
                    sp.run(['say',
                            'It looks like a {} to me, I\'m {:2.0f}% sure'.format(str(np.argmax(predictions[test_idx])),
                                                                                  100 * predictions[test_idx][np.argmax(
                                                                                      predictions[test_idx])])])
        except ValueError:
            print("Bad integer..., try again")
    else:
        keep_looping = False

print("Bye!")
