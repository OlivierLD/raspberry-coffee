#!/bin/bash
# JAVA_HOME=/usr/lib/jvm/jdk1.8.0_33
# JAVA_HOME=/usr/lib/jvm/oracle-java8-jdk-i386 # For Pixel
JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt
echo JavaH compilation, then run
echo Uses WiringPI
if [[ ! -d ./classes ]]; then
  mkdir classes
fi
#
echo \>\> Javac Compiling
javac -source 1.7 -target 1.7 -sourcepath ./src -d ./classes -classpath ./classes -g ./src/rangesensor/JNI_HC_SR04.java
echo \>\> Running javah
javah -jni -cp ./classes -d C rangesensor.JNI_HC_SR04
echo \>\> Here you should implement the C part in WiringPi_HC_SR04.c
cd C
echo Compiling C code \(producing lib libOlivHCSR04.so\)
g++ -Wall -shared -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux WiringPi_HC_SR04.c -lwiringPi -o libOlivHCSR04.so
cd ..
echo \>\> Now running the class invoking the native lib:
export LD_LIBRARY_PATH=./C
# ls -l ${LD_LIBRARY_PATH}/*.so
export NATIVEDEBUG=true
sudo java -Djava.library.path=${LD_LIBRARY_PATH} -cp ./classes rangesensor.JNI_HC_SR04
echo \>\> Done.
