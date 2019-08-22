#
#  TensorFlow only, no Keras
#
import tensorflow as tf
from tensorflow.python.framework import graph_util

# create a simple model
X = tf.placeholder(tf.float32, shape=(None, 4), name="input")
W = tf.get_variable("W", [4, 3], initializer=tf.zeros_initializer())  # W parameters with shape [4, 3]
b = tf.get_variable("b", [3], initializer=tf.zeros_initializer())  # bias
Z = tf.add(tf.matmul(X, W), b,
           name="not_activated_output")  # in case we want to check values without activation function
A = tf.nn.sigmoid(Z, "output")
#
# The following lines are for tensorboard:
# $ tensorboard --logdir .
# then reach http://localhost:6006/
#
writer = tf.summary.FileWriter('.')
writer.add_graph(tf.get_default_graph())
writer.flush()
#
with tf.Session() as sess:
    # tensorflow variables have to be initialized
    sess.run(tf.global_variables_initializer())

    # we won't train a model here, we'll just assign some values to variables
    sess.run(tf.assign(W, [[1, 2, 3], [4, 5, 6], [7, 8, 9], [10, 11, 12]]))
    sess.run(tf.assign(b, [1, 1.5, 2]))

    # run model to check calculations
    not_activated_output, activated_output = sess.run([Z, A], feed_dict={X: [[4, 3, 2, 1]]})
    print(">> NotActivatedOutput:", not_activated_output)  # should be [[41. 51.5 62.]]

    # Now save the model
    output_node_names = "not_activated_output,output"
    output_graph_def = graph_util.convert_variables_to_constants(
        sess,  # We need to pass session object, it contains all variables
        tf.get_default_graph().as_graph_def(),  # also graph definition is necessary
        output_node_names.split(",")  # we may use multiple nodes for output
    )

model_file = "../../../model/saved_model.pb"
with tf.gfile.GFile(model_file, "wb") as f:
    f.write(output_graph_def.SerializeToString())  # That's it!
