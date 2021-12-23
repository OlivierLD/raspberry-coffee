#!/bin/bash
JAVA_OPTS="-Djava.library.path=/usr/lib/jni"
CP=./classes
CP=${CP}:../Serial.IO/build/libs/Serial.IO-1.0.jar
CP=${CP}:/usr/share/java/RXTXcomm.jar
#
java -cp "${CP}" "${JAVA_OPTS}" mindwave.samples.io.gui.MindWaves
#
