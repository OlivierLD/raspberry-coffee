#!/bin/bash
mkdir classes 2> /dev/null
echo Compiling
# Remove the -verbose flag for less details
# scalac -verbose -sourcepath ../../src/scala -d classes ../../src/scala/HelloScala.scala
scalac -sourcepath ../../src/scala -d classes ../../src/scala/HelloScala.scala
echo Now running
# scala -verbose -cp classes HelloScala
scala -cp classes HelloScala

