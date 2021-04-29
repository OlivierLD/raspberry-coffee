Moved to <https://github.com/OlivierLD/oliv-ai>

### OpenCV (for Java) on the Raspberry Pi?
- Build instructions moved [here](https://github.com/OlivierLD/oliv-ai/tree/master/opencv).

After building and installing, quick test, from Python
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
> Note the paths above may change if you are not on Debian on Raspi OS.
> The `gradle` script(s) would be impacted.

## Some first tests
- Update the `build.gradle` with the right paths
- Do the same for `opencv.101.sh`
- Then run `../gradlew clean shadowJar run` or `./opencv.101.sh`.

This uses the images in the `images` folder.

## See also
- Some more OpenCV examples (Java & Python, Web, Swing, etc) are also available in [oliv.ai](https://github.com/OlivierLD/oliv-ai/tree/master/opencv)
- In this project, the module `Project-Trunk/WebcamTemplate`.

---
