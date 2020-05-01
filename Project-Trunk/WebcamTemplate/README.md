# Foundation for a WebCam Web Application
We'll be using the regular Raspberry Pi Camera Module.

We are talking about snapshots here, _not_ videos.

It _**is**_ possible to stream video from a Raspberry Pi, and it is easy.
See [Here](./VIDEO_STREAMING.md). 

### Tags and Keywords
`camera`, `images`, `raspistill`, `REST Server`, `no flickering`, `opencv`.

### Several components
- A Java thread (`image.snap.SnapSnapSnap`) that uses a camera (WebCam, PiCam, whatever, see the code to get the right one) to take pictures at a given time interval
- A REST server that goes along with it, allowing an HTTP access to the picture taken above
- A small Web App that pings the REST server, and display the last picture in a web page, refreshable at will (automatically or manually).

And optionally
- OpenCV

### Compile and run
On the Pi:
```
 $ ../../gradlew clean shadowJar
 $ ../../gradlew clean shadowJar run
```
or
```
 $ ./server.sh
``` 
From any browser, on any machine on the same network as the Pi: 
```
 http://[the-pi-address]:1234/web/index.html
``` 
The snapshot is updated on a regular base, and stored in a directory known by the 
REST/HTTP server, and thus reachable through its URL.

![Default Web Page](./docimages/web.page.png)

### OpenCV on the Raspberry Pi?
OpenCV can pave the way for Artificial Intelligence and Image Recognition. 
We could certainly use its features here.

Installing OpenCV is not snappy, but it is not too difficult.

- Instructions are available at <https://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html#introduction-to-opencv-for-java>
- Also, worth a look: <https://www.learnopencv.com/install-opencv-4-on-raspberry-pi/>
- And <https://gist.github.com/ivanursul/146b3474a7f3449ec70729f5c7f946ee>

Some differences below with the scripts provided above...

#### April-30, 2020.
Raspbian Buster comes with a JDK 11.
```
$ export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-armhf
```
We will need `cmake`, make sure you have it available:
```
$ which cmake
```
If missing, install it:
```
sudo apt-get install build-essential cmake ant
```

- Download the last sources from <https://opencv.org/releases/>
  - Can be a `wget https://github.com/opencv/opencv/archive/4.3.0.zip`
  - `mv 4.3.0.zip opencv-4.3.0.zip`
- `unzip opencv-4.3.0.zip`
- `cd opencv-4.3.0`
- `mkdir build`
- `cd build`
- Make it (this one takes time, hours...):
```
cmake -D CMAKE_BUILD_TYPE=RELEASE \
      -D WITH_LIBV4L=ON \
      -D WITH_OPENCL=OFF \
      -D BUILD_PERF_TESTS=OFF \
      -D BUILD_SHARED_LIBS=OFF \
      -D JAVA_INCLUDE_PATH=$JAVA_HOME/include \
      -D JAVA_AWT_LIBRARY=$JAVA_HOME/lib/libawt.so \
      -D JAVA_JVM_LIBRARY=$JAVA_HOME/lib/server/libjvm.so \
      -D CMAKE_INSTALL_PREFIX=/usr/local ..
```
- `sudo make`
- `sudo make install`

Quick test, from python
```
$ python3

>>> import cv2
>>> print(cv2.__version__)
4.3.0
>>>
```
Some location(s) to keep track of:
```
$ find /usr/local -name '*opencv*.jar' -exec ls -lisah {} \;
1075459 640K -rw-r--r-- 1 root root 639K Apr 30 10:09 /usr/local/share/java/opencv4/opencv-430.jar
```
- `/usr/local/share/java/opencv4/opencv-430.jar` will be used for the Java Classpath 
- `/usr/local/share/java/opencv4` will be used for the Java `-Djava.library.path`, as it contains a required system lib.
```
ll /usr/local/share/java/opencv4/
total 20M
1075457 4.0K drwxr-xr-x 2 root root 4.0K Apr 30 10:21 .
1075456 4.0K drwxr-xr-x 3 root root 4.0K Apr 30 10:21 ..
1075458  20M -rw-r--r-- 1 root root  20M Apr 30 10:11 libopencv_java430.so
1075459 640K -rw-r--r-- 1 root root 639K Apr 30 10:09 opencv-430.jar
pi@rpi-buster:~/opencv-4.3.0/build $ 
``` 

JavaFX was removed from JDK 11, it's to be referred to as a module...

In order not to introduce un-necessary complexity, we will not be using it here.

For JavaFX, see [this](https://stackoverflow.com/questions/38359076/how-can-i-get-javafx-working-on-raspberry-pi-3).
 
### Next 
We have added some support for some query-string parameters to the REST resource we use 
to get to the image.
It can be reworked _on the server_ before being reached from the client (browser) through its URL, just like before.

Those parameters can be driven from the web page displayed above, with the fields labeled 
`Gray`, `Blur`, `Threshold`, `Canny`, and `Contours`. The numbers the fields contain
refers to the order in which the corresponding transformation is applied to the image.
`0` means no transformation, values greater than 0 must be unique (do not give the same number to several transformations). 

Here are a couple of examples:

| Raw                             | Grayed                               |
|:-------------------------------:|:------------------------------------:|
| ![raw](./docimages/snap.01.jpg) | ![raw](./docimages/snap_tx_gray.jpg) |
| Grayed and Blurred              | Grayed & Threshold                   |
| ![raw](./docimages/snap_tx_gray_blur.jpg) | ![raw](./docimages/snap_tx_gray_threshold.jpg) |
| Threshold and Canny             | Color & Threshold                   |
| ![raw](./docimages/snap_tx_threshold_canny.jpg) | ![raw](./docimages/snap_tx_color_threshold.jpg) |

... More to come!

---
