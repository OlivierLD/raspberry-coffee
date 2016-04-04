## Lib RxTx, Serial communication.
This involves the classes located in the package `gnu.io`.
To install this package (on Raspberry PI, or more generally on Ubuntu), type
```
$> sudo apt-get install librxtx-java
```
This is an possible alternate to the `com.pi4j.io.serial` package (that comes with PI4J).
Give a bit more flexibility, specially on the callback side.

It requires:
* on the runtime command line `-Djava.library.path=/usr/lib/jni`
* in the classpath `/usr/share/java/RXTXcomm.jar`, to compile or run.

