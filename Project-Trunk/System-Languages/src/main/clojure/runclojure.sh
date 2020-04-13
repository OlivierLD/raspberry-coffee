#!/usr/bin/env bash
# Resources:
#  https://learnxinyminutes.com/docs/clojure/
#
CLOJURE_JAR=/usr/local/Cellar/clojure/1.9.0.381/libexec/clojure-tools-1.9.0.381.jar
CP=.:$CLOJURE_JAR
#
# To start the REPL: java -cp $CP clojure.main
#
java -cp $CP clojure.main --main systems.matrix
