#!/bin/bash
CP=./classes
CP=$CP:$PI4J_HOME/lib/pi4j-core.jar
CP=$CP:./lib/javax.mail_1.1.0.0_1-4-4.jar
CP=$CP:./lib/json.jar
#
java -classpath $CP relay.email.PIControllerMain $*
