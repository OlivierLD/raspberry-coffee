#!/usr/bin/env python3
#
# Handwritten figures recognition => classification
# Mathplotlib doc at https://matplotlib.org/3.1.0/api/_as_gen/matplotlib.pyplot.html
# also see https://www.tensorflow.org/api_docs/python/tf/keras and similar pages.
#
# Model training.
# Model can be saved from here too.
#
# For the figures images, see https://github.com/myleott/mnist_png
# images are 28px x 28px big.
#
import tensorflow as tf
import numpy as np
import matplotlib.pyplot as plt
import random
import sys
import warnings
from tensorflow.python.keras.utils.vis_utils import plot_model

sys.path.append('../')
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
    print("The network has 2 layers, 512, and then 10 neurons, fully connected.")
    print("Let's go!")

sess = tf_utils.get_TF_session()
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
print("Train images shape:", x_train.shape, ", ", len(y_train), "labels")
print("Test images shape:", x_test.shape, ", ", len(y_test), "labels")

x_train, x_test = x_train / 255.0, x_test / 255.0
print("Import completed, displaying a random set of data, once displayed, close the image to move on.")

if not loadOnly:  # Model to be trained
    start_idx = random.randint(0, len(x_train)) - 1
    print("Starting sample display at index {}".format(start_idx))

    # fig = plt.figure(figsize=(10, 10))
    fig = plt.figure(figsize=(5, 6))
    fig.canvas.set_window_title("Examples and labels of the training dataset")
    rows = 7
    columns = 7
    for i in range(rows * columns):
        subplot = plt.subplot(rows, columns, i + 1)
        plt.xticks([])
        plt.yticks([])
        plt.grid(False)
        plt.imshow(x_train[start_idx + i], cmap=plt.cm.binary)
        plt.xlabel(y_train[start_idx + i])
        # subplot.set_title(i + start_idx)
    plt.show()

    #
    # Define model here
    #
    # First layer (Flatten) takes arrays of 28x28=784 pixels. See below "Number of parameters: Explanation"
    # Last layer has 10 neurons, because we have 10 categories (0-9 digits)
    # Dropout is here to avoid over-fitting
    # SoftMax layer will dispatch the value so the highest is the one to choose,
    # and its value the percentage of reliability
    model = tf.keras.models.Sequential([
        tf.keras.layers.Flatten(),  # https://www.tensorflow.org/api_docs/python/tf/keras/layers/Flatten
        tf.keras.layers.Dense(512, name='FirstDense-512', activation=tf.nn.relu),   # ReLU for the first layer
        tf.keras.layers.Dropout(0.2),  # https://www.tensorflow.org/api_docs/python/tf/keras/layers/Dropout
        tf.keras.layers.Dense(10, name='SecondDense-10', activation=tf.nn.softmax)  # SoftMax at the last layer
    ])

    # plot_model(model, to_file='model_plot.png', show_shapes=True, show_layer_names=True)

    model.compile(optimizer='adam',
                  loss='sparse_categorical_crossentropy',
                  metrics=['accuracy'])

    epochs = 5
    print("----------------------------------")
    print("Starting the training, on {} epochs".format(epochs))
    print("----------------------------------")
    model.fit(x_train, y_train, epochs=epochs, verbose=1)

    show_details = True
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
            except Exception:
                print("Oops")

    model.summary()
    print("------ Number of parameters: Explanation -------")
    print("  401,920 = 512 x 785 ")
    print("              |   785 = (28 x 28) + 1")
    print("              |                |    | ")
    print("              |                |    bias ")
    print("              |                input shape ")
    print("              # neurons 1st (hidden) layer ")
    print(" +  5,130 = (512 x 10) + 10")
    print("               |    |     | ")
    print("               |    |     bias ")
    print("               |    final # of neurons ")
    print("               # neurons previous layer ")
    print("-----------")
    print("= 407,050 Trainable params ")
    print("------------------------------------------------")
    #
    print("Model evaluate:")
    model.evaluate(x_test, y_test)
    #
    model.save('training.h5')
    print("Model was saved")
else:
    model = tf.keras.models.load_model('training.h5')
    print(">> Model is now loaded")
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
                print("Test index {} ... image of {} rows of {} bytes.".format(test_idx, len(digit), len(digit[0])))
                # with open('digit_{}.png'.test_idx, 'wb') as f:
                #     f.write(digit)
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
