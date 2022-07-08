#!/bin/bash
# JAVA_HOME=/usr/lib/jvm/jdk1.8.0_33
JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt
echo Just run, no javah generation.
echo Uses WiringPI
#
echo \>\> Now running the class invoking the native lib:
export LD_LIBRARY_PATH=./C
# ls -l $LD_LIBRARY_PATH/*.so
# Problem with NATIVEDEBUG? Not set in the C code when invoked from Java ???
export NATIVEDEBUG=true
sudo java -Djava.library.path=$LD_LIBRARY_PATH -cp ./classes rangesensor.JNI_HC_SR04
echo \>\> Done.
