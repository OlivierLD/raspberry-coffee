#!/bin/bash
echo 1. Compiling the Java files we want to invoke from Clojure
mkdir build > /dev/null 2>&1
mkdir build/classes > /dev/null 2>&1
mkdir build/classes/main > /dev/null 2>&1
javac -d build/classes/main -sourcepath src/java src/java/test/clojure/TestObject.java
#
echo 2. Running clojure REPL
CLOJURE_HOME=~/clojure-1.8.0
#
java -cp $CLOJURE_HOME/clojure-1.8.0.jar:./build/classes/main clojure.main src/clojure/example/invokejava.clj
