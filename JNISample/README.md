### Quick Java Native Interface (JNI) Sample on the Raspberry PI
The script named `jni` does all the job. You can run it. Here are below the steps the script is going through:

* **_First_**, write the class named `jnisample.HelloWorld.java`. Notice in the code the `System.loadLibrary("HelloWorld");`
* Compile it
```
$> javac -source 1.7 -target 1.7 -sourcepath ./src -d ./classes -classpath ./classes -g ./src/jnisample/HelloWorld.java
```
* Run the `javah` utility on it
```
 $> javah -jni -cp ./classes -d C jnisample.HelloWorld
```
* Implement the native code (`HelloWorld.c`) that includes the generated `.h` file
* Compile it, using `gcc` or `g++`. Make sure you use the right flags for the C compiler... Notice that the generated
```
$> g++ -Wall -shared -I$JAVA_HOME/include -I$JAVA_HOME/include/linux HelloWorld.c -lwiringPi -o libHelloWorld.so
```
library _**must**_ be named `libHelloWorld.so` and _**not**_ `HelloWorld.so`, for the `System.loadLibrary("HelloWorld");` to
work.
* To run the Java code, the `-Djava.library.path`  variable must be set.
* The Java class should run.

This is the output of the execution of the `jni` script:
```
pi@raspi-dev ~/raspberry-pi4j-samples/JNISample $ ./jni
>> Compiling
warning: [options] bootstrap class path not set in conjunction with -source 1.7
1 warning
>> Running javah
>> Here you should implement HelloWorld.c, including jnisample_HelloWorld.h
>> Library must be named libHelloWorld.so and not only HelloWorld.so
>> Now running the class invoking the native lib:
Hello World!
>> Done.
pi@raspi-dev ~/raspberry-pi4j-samples/JNISample $
```

Good luck!

---
