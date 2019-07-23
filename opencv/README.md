## OpenCV in Java and Python

### Good resources, OpenCV & JavaFX
- <https://opencv-java-tutorials.readthedocs.io/en/latest/index.html>
- <https://github.com/opencv-java/>


### Face recognition, OpenCV and DL
- <https://www.freecodecamp.org/news/facial-recognition-using-opencv-in-java-92fa40c22f62/>

#### Misc bulk notes, scratch-pad, scribblings, etc.

> Great resource [here](https://docs.opencv.org/master/d9/df8/tutorial_root.html),
> samples in 3 languages: C++, Java, Python.

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
See in [`build.gradle`](./build.gradle).

```
 ../gradlew runFX
```

### Custom code
> Using Java Swing

Path detection on one image.

Would also work each frame returned by the camera
```
 ../gradlew runOlivSwing
```
| ![Original](./docimg/step.01.png) | ![Gray](./docimg/step.02.png) | ![Blur](./docimg/step.03.png) |
|:--------:|:--------:|:--------:|
| ![Threshed](./docimg/step.04.png) | ![Contours](./docimg/step.05.png) | ![Canny Edges](./docimg/step.06.png) |
| ![Detected](./docimg/step.07.png) |  |  |

This could be the basis to drive a robot carrying the camera

---

Interactive, transforming images returned by the camera in real time.
```
 ../gradlew runOlivSwingCamera
```
| Interactive transformations from Swing |
|:-----------------------------:|
| ![Swing UI](./docimg/snap.01.png) |

---
