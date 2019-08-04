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
import os.path
import subprocess as sp
import sys
from time import time
sys.path.append('../')
import tf_utils

warnings.filterwarnings('ignore')

print("Panda version", pd.__version__)

tf.logging.set_verbosity(tf.logging.ERROR)
print("TensorFlow version", tf.__version__)

sess = tf_utils.get_TF_session()
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

train_loss, train_accuracy = model.evaluate(X_train, y_train, batch_size=BATCH_SIZE)
print("Training Loss {}, Quality {}%".format(train_loss, 100 * train_accuracy))

test_loss, test_accuracy = model.evaluate(X_test, y_test, batch_size=BATCH_SIZE)
print("Test Loss {}, Quality {}%".format(test_loss, 100 * test_accuracy))

print("Saving the model")
model.save('insurance.h5')
print("Done")
