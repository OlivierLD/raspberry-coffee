### Quick Java Native Interface (JNI) Sample on the Raspberry PI
The script named `jni` does all the job. You can run it. Here are below the steps the script is going through:

* **_First_**, write the class named [`jnisample.HelloWorld.java`](./src/jnisample/HelloWorld.java). Notice in the code the `private native void print();` directive:
```java
  private native void print(); // Tells javah to build the stub
```
and the `System.loadLibrary("HelloWorld");` statement:
```java
  static {
    System.loadLibrary("HelloWorld");
  }
```
* Compile it:
```
$> javac -sourcepath ./src -d ./classes -classpath ./classes -g ./src/jnisample/HelloWorld.java
```
* Run the `javah` utility on it
```
 $> javah -jni -cp ./classes -d C jnisample.HelloWorld
```
* **_Then_** implement the native code (`HelloWorld.c`) that _includes_ the generated `.h` file
* Compile it, using `gcc` or `g++`. Make sure you use the right flags for the C compiler... Notice that the generated library _**must**_ be named `libHelloWorld.so` and _**not**_ `HelloWorld.so`, for the `System.loadLibrary("HelloWorld");` to work.
```
$> g++ -Wall -shared -I$JAVA_HOME/include -I$JAVA_HOME/include/linux HelloWorld.c -lwiringPi -o libHelloWorld.so
```
* To run the Java code, the `-Djava.library.path`  variable must be set.
* The Java class should run.

This is the output of the execution of the `jni` script:
```
pi@raspi-dev ~/raspberry-pi4j-samples/JNISample $ ./jni
>> Compiling Java
>> Running javah
>> Here you should implement HelloWorld.c, including jnisample_HelloWorld.h, and compile it.
>> Library must be named libHelloWorld.so and not only HelloWorld.so
>> Compiling C
>> Now running (java) the class invoking the native lib:
Hello C World!
>> Done.
pi@raspi-dev ~/raspberry-pi4j-samples/JNISample $
```

Good luck!

---
