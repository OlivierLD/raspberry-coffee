### Keras, TensorFlow, and friends
See [Keras web site](http://keras.io) for instructions on how to install Keras

I did:
```
$ git clone https://github.com/keras-team/keras.git
$ cd keras
$ sudo python setup.py install
```
Then, you're good to go.
Set your proxy if needed,
```
$ export HTTP_PROXY=http://www-proxy.us.oracle.com:80
$ export HTTPS_PROXY=http://www-proxy.us.oracle.com:80
```

Run an example from the Keras repo:
```
$ python examples/antirectifier.py
Using TensorFlow backend.
Downloading data from https://s3.amazonaws.com/img-datasets/mnist.npz
11493376/11490434 [==============================] - 7s 1us/step
60000 train samples
10000 test samples
WARNING:tensorflow:From /Users/olediour/anaconda3/lib/python3.7/site-packages/tensorflow/python/framework/op_def_library.py:263: colocate_with (from tensorflow.python.framework.ops) is deprecated and will be removed in a future version.
Instructions for updating:
Colocations handled automatically by placer.
WARNING:tensorflow:From /Users/olediour/anaconda3/lib/python3.7/site-packages/Keras-2.2.4-py3.7.egg/keras/backend/tensorflow_backend.py:3721: calling dropout (from tensorflow.python.ops.nn_ops) with keep_prob is deprecated and will be removed in a future version.
Instructions for updating:
Please use `rate` instead of `keep_prob`. Rate should be set to `rate = 1 - keep_prob`.
WARNING:tensorflow:From /Users/olediour/anaconda3/lib/python3.7/site-packages/tensorflow/python/ops/math_ops.py:3066: to_int32 (from tensorflow.python.ops.math_ops) is deprecated and will be removed in a future version.
Instructions for updating:
Use tf.cast instead.
Train on 60000 samples, validate on 10000 samples
Epoch 1/40
2019-06-07 07:45:57.731221: I tensorflow/core/platform/cpu_feature_guard.cc:141] Your CPU supports instructions that this TensorFlow binary was not compiled to use: AVX2 FMA
60000/60000 [==============================] - 6s 102us/step - loss: 0.6046 - acc: 0.9135 - val_loss: 0.1447 - val_acc: 0.9632
Epoch 2/40
60000/60000 [==============================] - 5s 87us/step - loss: 0.1224 - acc: 0.9660 - val_loss: 0.1016 - val_acc: 0.9701
Epoch 3/40
...
Epoch 39/40
60000/60000 [==============================] - 6s 98us/step - loss: 0.0049 - acc: 0.9983 - val_loss: 0.0860 - val_acc: 0.9814
Epoch 40/40
60000/60000 [==============================] - 6s 99us/step - loss: 0.0044 - acc: 0.9985 - val_loss: 0.0833 - val_acc: 0.9829
$
```
etc...
```
$ python examples/addition_rnn.py
```

Some Python scripts are also similar to the Jupyter notebooks, they can be run from this directory:
```
$ python sample.01.py
$ python sample.03.py
$ python sample.04.py
```
You need to have downloaded the data before running the scripts:
```
$ curl -O https://raw.githubusercontent.com/DJCordhose/deep-learning-crash-course-notebooks/master/data/insurance-customers-1500.csv
```

## Misc pointers
- [Oliv's OpenCV WIP, in Java & Python](https://github.com/OlivierLD/raspberry-coffee/tree/master/opencv)
- [Facial recognition, OpenCV & Java](https://docs.opencv.org/2.4/doc/tutorials/imgproc/threshold/threshold.html)
- [Machine Learning in Digital Process Automation - Part 1](https://medium.com/oracledevs/machine-learning-in-digital-process-automation-part-i-7c7468e23804)
- [Machine Learning in Digital Process Automation - Part 2](https://medium.com/@ralf_mueller/ebeeec8763dc)
- [Data mining concepts](https://docs.oracle.com/en/database/oracle/oracle-database/19/dmcon/index.html)
- [Serverless and Recurrent Neural Networks with Fn, GraphPipe and TensorFlow](https://medium.com/fnproject/serverless-and-recurrent-neural-networks-with-tensorflow-and-graphpipe-fc73785f1a16)
- [Simple Guide to Hyperparameter Tuning in Neural Networks](https://towardsdatascience.com/simple-guide-to-hyperparameter-tuning-in-neural-networks-3fe03dad8594)
