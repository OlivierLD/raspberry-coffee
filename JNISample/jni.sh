#!/bin/bash
# On Mac, for JAVA_HOME, use something like /Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home
if [[ "${JAVA_HOME}" = "" ]]; then
  JAVA_HOME=/opt/jdk/jdk1.8.0_112
fi
PATH=${JAVA_HOME}/bin:$PATH
mkdir classes 2> /dev/null
echo \>\> Compiling Java
mkdir classes 2> /dev/null
javac -sourcepath ./src -d ./classes -classpath ./classes -g ./src/jnisample/HelloWorld.java
echo \>\> Running javah
javah -jni -cp ./classes -d C jnisample.HelloWorld
echo \>\> Here you should implement HelloWorld.c, including jnisample_HelloWorld.h, and compile it
cd C
echo \>\> Library must be named libHelloWorld.so and not only HelloWorld.so
echo \>\> Compiling C
RPI=`uname -a | grep arm`
if [[ "$RPI" != "" ]]; then
  # For Raspberry Pi. -lwiringPi is not mandatory in this case...
  echo C compilation on the Raspberry Pi
  g++ -Wall -shared -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux HelloWorld.c -lwiringPi -o libHelloWorld.so
else
  DARWIN=`uname -a | grep Darwin`
  if [[ "$DARWIN" != "" ]]; then
    # For Mac OS
    echo C Compilation on Mac OS
    gcc -Wall -shared -fPIC -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin HelloWorld.c -o libHelloWorld.jnilib
  else
    # For Linux Debian
    echo C Compilation on Linux \(Not Raspberry Pi\)
    g++ -Wall -shared -fPIC -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux HelloWorld.c -o libHelloWorld.so
  fi
fi
cd ..
echo \>\> Now running the Java class invoking the native lib:
export LD_LIBRARY_PATH=./C
# ls -ls $LD_LIBRARY_PATH/*.so
java -Djava.library.path=$LD_LIBRARY_PATH -cp ./classes jnisample.HelloWorld
echo \>\> Done.
