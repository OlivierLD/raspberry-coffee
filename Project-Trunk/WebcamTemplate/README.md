# Foundation for a WebCam Web Application

### Tags and Keywords
`camera`, `images`, `raspistill`, `REST Server`, `no flickering`.

### Several components
- A Java thread (`image.snap.SnapSnapSnap`) that uses a camera (WebCam, PiCam, whatever, see the code to get the right one) to take pictures at a given time interval
- A REST server that goes along with it, allowing an HTTP access to the picture taken above
- A small Web App that pings the REST server, and display the last picture in a web page, refreshable at will (automatically or manually).

### Compile and run
On the Pi:
```
 $ ../../gradlew clean shadowJar
 $ ./server.sh
``` 
From any browser, an any machine on the same network as the Pi: 
```
 http://[the-pi-address]:1234/web/index.html
``` 

### OpenCV on the Raspberry Pi?
- Instructions are available at <https://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html#introduction-to-opencv-for-java>
- Also, worth a look: <https://www.learnopencv.com/install-opencv-4-on-raspberry-pi/>
- And <https://gist.github.com/ivanursul/146b3474a7f3449ec70729f5c7f946ee>

Some differences below with the scripts provided above...

#### April-30, 2020.
Raspian Buster comes with a JDK 11.
```
$ export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-armhf/
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
Make it (this one takes time, hours...):
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

For JavaFX, see [this](https://stackoverflow.com/questions/38359076/how-can-i-get-javafx-working-on-raspberry-pi-3).
 
### Next 
- OpenCV processing of the snapshots (Java 9...)

---
