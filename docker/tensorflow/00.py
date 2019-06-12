import tensorflow as tf

hello = tf.constant('Hello, TensorFlow!')
sess = tf.compat.v1.Session() # tf.Session()
print(sess.run(hello))
