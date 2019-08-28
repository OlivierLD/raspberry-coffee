#
# Install on Linux/Debian (including Raspberry Pi):
# sudo apt-get install libatlas-base-dev
# sudo apt-get install python3-pip
# pip3 install tensorflow
#
import tensorflow as tf
tf.enable_eager_execution()
print(tf.reduce_sum(tf.random_normal([1000, 1000])))
