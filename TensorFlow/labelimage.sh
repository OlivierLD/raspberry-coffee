#!/usr/bin/env bash
# CP=./lib/libtensorflow-1.8.0.jar
# CP="$CP:./build/classes/java/main"
#
CP=./build/libs/TensorFlow-1.0.jar
echo "Classpath is $CP"
# java -cp $CP -Djava.library.path=./lib/jni/org/tensorflow/native/darwin-x86_64 org.tensorflow.examples.LabelImage ./models/inception5h $*
java -cp $CP org.tensorflow.examples.LabelImage $*
