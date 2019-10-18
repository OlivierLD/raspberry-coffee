#!/usr/bin/env python3
#
# Insurance Company model training
# Saves the model at the end
#
import warnings
import pandas as pd
import tensorflow as tf
from tensorflow.python import keras
from tensorflow.python.keras.callbacks import TensorBoard
from sklearn.model_selection import train_test_split
from tensorflow.python.keras.layers import Dense, Dropout, BatchNormalization, Activation
from tensorflow.python.keras.utils.vis_utils import plot_model
import os.path
import subprocess as sp
import sys
from time import time
sys.path.append('../')
import tf_utils

warnings.filterwarnings('ignore')

print("Panda version", pd.__version__)

sess = tf_utils.get_TF_session()

try:
    tf.logging.set_verbosity(tf.logging.ERROR)
except Exception as ex:
    print("Ooops {}".format(ex))
finally:
    print("Moving on")

print("TensorFlow version", tf.__version__)

devices = sess.list_devices()
print("----- D E V I C E S -----")
for d in devices:
    print(d.name)
print("-------------------------")

# a small sanity check, does tf seem to work ok?
hello = tf.constant('Hello TF!')
print(sess.run(hello))

print("Keras version", keras.__version__)

found_data = False
if os.path.isfile('./insurance-customers-1500.csv'):
    found_data = True

if not found_data:
    print("Data file insurance-customers-1500.csv is not here")
    userInput = input("Do you want to download it now ? Y/n > ")
    if userInput == '' or userInput == 'y' or userInput == 'Y':
        print("Downloading...")
        sp.run(["curl", "-O",
                "https://raw.githubusercontent.com/DJCordhose/deep-learning-crash-course-notebooks/master/data/insurance-customers-1500.csv"])
    else:
        print("Ok, exiting.")
        sys.exit()

# Read the data frame, split inputs (X) and labels (y)
df = pd.read_csv('./insurance-customers-1500.csv', sep=';')
y = df['group']
df.drop('group', axis='columns', inplace=True)
X = df.as_matrix()

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.4, random_state=42, stratify=y)
print("Training (X and y) shapes, Test (X and y) shapes")
print(X_train.shape, y_train.shape, X_test.shape, y_test.shape)

print("Building the model")

num_categories = 3
dropout = 0.6
model = keras.Sequential()

model.add(Dense(100, name='Hidden1Layer1', input_dim=3))
model.add(BatchNormalization())
model.add(Activation('relu'))
model.add(Dropout(dropout))

model.add(Dense(100, name='HiddenLayer2'))
model.add(BatchNormalization())
model.add(Activation('relu'))
model.add(Dropout(dropout))

# Last layer, SoftMax, as many neurons as categories we want (3)
model.add(Dense(num_categories, name='SoftmaxLayer', activation='softmax'))

plot_model(model, to_file='model_plot.png', show_shapes=True, show_layer_names=True)

model.compile(loss='sparse_categorical_crossentropy',
              optimizer='adam',
              metrics=['accuracy'])
# model.compile(loss='categorical_crossentropy',
#               optimizer='sgd',
#               metrics=['accuracy'])
model.summary()

# Bonus: tensorboard
#
# From another prompt, launch tensorboard --logdir=logs/ --host localhost
# The tensorboard function will be mention in the fit method
tensorboard = TensorBoard(log_dir="logs/{}".format(time()))

print("Starting the training")
BATCH_SIZE = 1_000
EPOCHS = 2_000

# Actual training, fit method. Turn verbose to 0 to zip it up.
# Notice the tensorboard callback
history = model.fit(X_train, y_train, epochs=EPOCHS, batch_size=BATCH_SIZE, validation_split=0.2, verbose=0, callbacks=[tensorboard])
print("Training completed")

show_details = True
if show_details:
    network_details = open("network.details.properties", "w")
    # config = model.get_config()
    # from keras.models import model_from_json
    json_string = model.to_json()
    network_details.write("structure: {}\n".format(json_string))
    network_details.write("name: {}\n".format(model.name))
    # print("Model, json format: {}".format(json_string))
    layers = model.layers
    network_details.write("nb.layers: {}\n".format(len(layers)))
    print("Model, {} layer(s)".format(len(layers)))
    layer_idx = 0
    for layer in layers:
        try:
            print("Name: {}, trainable: {}".format(layer.name, layer.trainable))
            network_details.write("layer.{:02d}.name = {}\n".format(layer_idx, layer.name))
            if len(layer.get_weights()) > 0:
                weights = layer.get_weights()[0]
                # weights is an ndarray
                w_dim = weights.ndim
                if w_dim > 1:
                    for idx in range(w_dim):
                        weight_csv = ''
                        for weight in weights[idx]:
                            if len(weight_csv) == 0:
                                weight_csv = "{}".format(weight)
                            else:
                                weight_csv = "{}, {}".format(weight_csv, weight)
                        network_details.write("layer.{:02d}.weights.{:02d} = {}\n".format(layer_idx, idx, weight_csv))
                else:
                    weight_csv = ''
                    for weight in weights:
                        if len(weight_csv) == 0:
                            weight_csv = "{}".format(weight)
                        else:
                            weight_csv = "{}, {}".format(weight_csv, weight)
                    network_details.write("layer.{:02d}.weights = {}\n".format(layer_idx, weight_csv))

                biases = layer.get_weights()[1]
                # biases is an ndarray
                b_dim = biases.ndim
                if b_dim > 1:
                    for idx in range(b_dim):
                        biases_csv = ''
                        for bias in biases[idx]:
                            if len(biases_csv) == 0:
                                biases_csv = "{}".format(bias)
                            else:
                                biases_csv = "{}, {}".format(biases_csv, bias)
                        network_details.write("layer.{:02d}.biases.{:02d} = {}\n".format(layer_idx, idx, biases_csv))
                else:
                    biases_csv = ''
                    for bias in biases:
                        if len(biases_csv) == 0:
                            biases_csv = "{}".format(bias)
                        else:
                            biases_csv = "{}, {}".format(biases_csv, bias)
                    network_details.write("layer.{:02d}.biases = {}\n".format(layer_idx, biases_csv))

                # print("Weights: {}\nBiases: {}".format(weights, biases))
            else:
                print("No weights")
        except Exception as exception:
            print("Oops: {}".format(exception))
        layer_idx += 1
    network_details.close()

train_loss, train_accuracy = model.evaluate(X_train, y_train, batch_size=BATCH_SIZE)
print("Training Loss {}, Quality {}%".format(train_loss, 100 * train_accuracy))

test_loss, test_accuracy = model.evaluate(X_test, y_test, batch_size=BATCH_SIZE)
print("Test Loss {}, Quality {}%".format(test_loss, 100 * test_accuracy))

print("Saving the model")
model.save('insurance.h5')
print("Done")
