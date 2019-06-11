### TensorFlow
Getting started...

- [TensorFlow](https://www.tensorflow.org/)
- [Install](https://www.tensorflow.org/install/)
- [Install for Java](https://www.tensorflow.org/install/install_java)
- [Samples](https://github.com/tensorflow/tensorflow/blob/r1.8/tensorflow/java/src/main/java/org/tensorflow/examples/LabelImage.java) (Git Repo)

I used Gradle, see the `build.gradle` file.

To run the LabelImage sample: 
```
$ curl -O https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip
```
Unzip it, then run
```
$ ./labelimage.sh ./inception5h ./img/mug.jpeg 
```
