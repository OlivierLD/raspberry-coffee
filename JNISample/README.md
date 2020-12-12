### Quick Java Native Interface (JNI) Sample on the Raspberry Pi

The Java Development Kit comes with the `javah` utility, this is the one you will use to generate a Java Native Interface (JNI).

In short:
1. You start with the Java class you want to use from your Java code, the one that will invoke the native code.
  * You prefix the native methods with the `native` directive.
  * You invoke the `System.loadLibrary` in a static block.
2. You compile your Java code
3. You run the `javah` utility on the generated class
4. You implement and compile your C code into a system library (`.so` in our case)

The script named `jni` does all the job. You can run it. Here are below the detailed steps the script is going through:

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
  This will generate a C header file named `jnisample_HelloWorld.h`, in the `C` directory.
* **_Then_** implement the native code (`HelloWorld.c`) that _includes_ the generated `.h` file:

```C
#include <jni.h>
#include <stdio.h>
#include "jnisample_HelloWorld.h"

JNIEXPORT void JNICALL Java_jnisample_HelloWorld_print (JNIEnv * env, jobject obj) {
  printf("Hello C World!\n");
  return;
}
```
  Notice the `#include "jnisample_HelloWorld.h"`, and the `#include <jni.h>`.
  Notice that the `native void print` is implemented in C as `JNIEXPORT void JNICALL Java_jnisample_HelloWorld_print`. All it
  does here is an output on `stdout`.
* Compile it, using `gcc` or `g++`. Make sure you use the right flags for the C compiler... Notice that the generated library _**must**_ be named `libHelloWorld.so` and _**not**_ `HelloWorld.so`, for the `System.loadLibrary("HelloWorld");` to work.
```
$> g++ -Wall -shared -I$JAVA_HOME/include -I$JAVA_HOME/include/linux HelloWorld.c [-lwiringPi] -o libHelloWorld.so
```
* To run the Java code, the `-Djava.library.path`  variable must be set.
* The Java class should run.

This is the output of the execution of the `jni` script:
```
pi@raspi-dev ~/raspberry-coffee/JNISample $ ./jni
>> Compiling Java
>> Running javah
>> Here you should implement HelloWorld.c, including jnisample_HelloWorld.h, and compile it.
>> Library must be named libHelloWorld.so and not only HelloWorld.so
>> Compiling C
>> Now running the Java class invoking the native lib:
Hello C World!
>> Done.
pi@raspi-dev ~/raspberry-coffee/JNISample $
```

Good luck!

---
