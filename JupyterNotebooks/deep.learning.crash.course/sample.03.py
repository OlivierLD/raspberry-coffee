#!/usr/bin/env python3
#
# Insurance Company model training, over-fitting?
# Saves the model at the end
#
import warnings
import pandas as pd
import tensorflow as tf
from tensorflow import keras
from sklearn.model_selection import train_test_split
from tensorflow.keras.layers import Dense, Dropout, BatchNormalization, Activation

import tf_utils

warnings.filterwarnings('ignore')

print("Panda version", pd.__version__)

tf.logging.set_verbosity(tf.logging.ERROR)
print("TensorFlow version", tf.__version__)

# let's see what compute devices we have available, hopefully a GPU
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

df = pd.read_csv('./insurance-customers-1500.csv', sep=';')
y = df['group']
df.drop('group', axis='columns', inplace=True)
X = df.as_matrix()

df.head()

df.describe()

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42, stratify=y)

print(X_train.shape, y_train.shape, X_test.shape, y_test.shape)

X_train_2_dim = X_train[:, :2]
X_test_2_dim = X_test[:, :2]

num_categories = 3

print("Assembling the network")

dropout = 0.6
model = keras.Sequential()

model.add(Dense(500, name='HiddenLayer1', input_dim=2))
# model.add(BatchNormalization())
model.add(Activation('relu'))
# model.add(Dropout(dropout))

model.add(Dense(500, name='HiddenLayer2'))
# model.add(BatchNormalization())
model.add(Activation('relu'))
# model.add(Dropout(dropout))

model.add(Dense(num_categories, name='SoftmaxLayer', activation='softmax'))

model.compile(loss='sparse_categorical_crossentropy',
              optimizer='adam',
              metrics=['accuracy'])
model.summary()

# reducing batch size might increase over-fitting,
# but might be necessary to reduce memory requirements
BATCH_SIZE = 1000

# reduce this based on what you see in the training history
EPOCHS = 10000

print("Starting the training...")
history = model.fit(X_train_2_dim, y_train, epochs=EPOCHS, batch_size=BATCH_SIZE, validation_split=0.2, verbose=0)
print("Training completed!")

train_loss, train_accuracy = model.evaluate(X_train_2_dim, y_train, batch_size=BATCH_SIZE)
print("Training Accuracy {}%".format(100 * train_accuracy))

test_loss, test_accuracy = model.evaluate(X_test_2_dim, y_test, batch_size=BATCH_SIZE)
print("Testing Accuracy {}%".format(100 * test_accuracy))

print("Saving the model")
model.save('insurance.h5')
print("Done")
