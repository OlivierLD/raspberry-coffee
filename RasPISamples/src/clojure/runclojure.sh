#!/usr/bin/env bash
CLOJURE_JAR=/usr/local/Cellar/clojure/1.9.0.381/libexec/clojure-tools-1.9.0.381.jar
CP=.:$CLOJURE_JAR
#
java -cp .:$CP clojure.main --main systems.matrix
