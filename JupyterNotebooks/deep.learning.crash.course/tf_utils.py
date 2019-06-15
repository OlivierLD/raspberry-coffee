import tensorflow as tf

tf_version = tf.__version__
print("TensorFlow", tf_version)
tf_split = tf_version.split('.')
# print(tf_split, len(tf_split))
tf_major = int(tf_split[0])
tf_minor = int(tf_split[1])
tf_patch = int(tf_split[2])

def get_TF_session():
	sess = None
	if tf_major >= 2 or (tf_major == 1 and tf_minor > 10):
		sess = tf.compat.v1.Session()
	else:
		sess = tf.Session()
	return sess
