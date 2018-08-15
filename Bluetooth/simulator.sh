#!/usr/bin/env bash
CP=./build/libs/Bluetooth-1.0-all.jar
JAVA_OPTS=
JAVA_OPT="$JAVA_OPTS -Dobd.verbose=true"
# JAVA_OPTS="$JAVA_OPTS -Dserial.port=/dev/ttyS0"
JAVA_OPTS="$JAVA_OPTS -Dserial.port=/dev/ttyAMA0"
# JAVA_OPTS="$JAVA_OPTS -Dserial.port=/dev/tty.Bluetooth-Incoming-Port"
JAVA_OPT="$JAVA_OPTS -Djava.library.path=/usr/lib/jni"
sudo java -cp $CP $JAVA_OPTS obd.OBDIISimulator
