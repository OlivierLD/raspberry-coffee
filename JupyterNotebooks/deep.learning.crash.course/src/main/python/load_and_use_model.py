import tensorflow as tf

model_filename = '../../../model/saved_model.pb'
with tf.gfile.GFile(model_filename, 'rb') as f:
    graph_def = tf.GraphDef()
    graph_def.ParseFromString(f.read())
    # tensorflow adds "import/" prefix to all tensors when imports graph definition, ex: "import/input:0"
    # so we explicitly tell tensorflow to use empty string -> name=""
    tf.import_graph_def(graph_def, name="")
print(tf.get_default_graph().get_operations())  # just print all operations for debug

X = tf.get_default_graph().get_tensor_by_name("input:0")
Z = tf.get_default_graph().get_tensor_by_name("not_activated_output:0")
with tf.Session() as sess:
    not_activated_result = sess.run([Z], feed_dict={X: [[4, 3, 2, 1]]})
    print("\n>> Not Activated Result:", not_activated_result)  # should be [[41. 51.5 62.]]
