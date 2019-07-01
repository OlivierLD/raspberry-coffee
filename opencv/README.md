## Tentative! OpenCV in Java!

#### Misc bulk notes

<https://opencv-java-tutorials.readthedocs.io/en/latest/index.html>

Java stuff:
```
ll /usr/local/Cellar/opencv/4.1.0_2/share/java/opencv4
```

```
 export JAVA_HOME=`/usr/libexec/java_home -v 9.0.1`
 ../gradlew run
 
```
> If this raises a  `no opencv_java410 in java.library.path`

You need 
```
cd /usr/local/Cellar/opencv/4.1.0_2/share/java/opencv4
ln -s libopencv_java410.dylib libopencv_java410.so 
```
See in `build.gradle`.

```
 ../gradlew runFX
```
