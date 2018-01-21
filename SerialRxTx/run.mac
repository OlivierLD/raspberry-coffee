#!/bin/bash
#
# RXTX_HOME=~/repos/oliv-soft-project-builder/olivsoft/release/all-3rd-party/rxtx.distrib
RXTX_HOME=./libs
#
CP=./build/libs/SerialRxTx-1.0-all.jar
CP=$CP:$RXTX_HOME/RXTXcomm.jar
#
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Djava.library.path=$RXTX_HOME"
JAVA_OPTS="$JAVA_OPTS -Dserial.port=/dev/tty.usbserial"
JAVA_OPTS="$JAVA_OPTS -Dbaud.rate=115200"
JAVA_OPTS="$JAVA_OPTS -Dverbose=false"
#
# java -Djava.library.path=$RXTX_HOME/mac-10.5 -Dserial.port=/dev/tty.usbserial -Dbaud.rate=115200 -cp $CP console.SerialConsoleCLI
#
java $JAVA_OPTS -cp $CP console.SerialConsoleCLI
