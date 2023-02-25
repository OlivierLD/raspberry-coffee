#!/bin/bash
# 0. Cleanup, setup
rm -rf build > /dev/null 2>&1
mkdir build > /dev/null 2>&1
mkdir build/classes > /dev/null 2>&1
mkdir build/libs > /dev/null 2>&1
#
VERBOSE=
# VERBOSE="-verbose"
# 1. Interfaces
echo -e "Interface Jars"
javac ${VERBOSE} -d build/classes -sourcepath src/main/java src/main/java/compute/Task.java
pushd build/classes
# cd build/classes
jar -cvf ../libs/compute.jar compute/*.class
popd
# cd ../..
# 2. Server
echo -e "Server classes"
javac ${VERBOSE} -d build/classes -sourcepath src/main/java -cp build/libs/compute.jar src/main/java/engine/ComputeEngine.java
# 3. Client
echo -e "Client classes"
javac ${VERBOSE} -d build/classes -sourcepath src/main/java -cp build/libs/compute.jar src/main/java/client/*.java
