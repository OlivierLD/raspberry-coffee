#!/usr/bin/env bash
#
# Compile and run the EXPORTED PApplet
#
if [ ! -d classes ]
then
  mkdir classes
fi
#
echo -ew "Make sure you're running java 8, not 9"
/usr/libexec/java_home -V
export JAVA_HOME=`/usr/libexec/java_home -v 1.8.0_144`
#
java -version
#
echo -e "Libraries must be compiled with 'sourceCompatibility = 1.8'"
#
CP=application.macosx/Radar.app/Contents/Java/core.jar
CP=$CP:application.macosx/Radar.app/Contents/Java/gluegen-rt.jar
CP=$CP:application.macosx/Radar.app/Contents/Java/jogl-all.jar
CP=$CP:application.macosx/Radar.app/Contents/Java/gluegen-rt-natives-macosx-universal.jar
CP=$CP:application.macosx/Radar.app/Contents/Java/jogl-all-natives-macosx-universal.jar
#
CP=$CP:code/RasPiRadar-1.0-all.jar
#
javac -d classes -cp $CP application.macosx/source/Radar.java
#
java -cp $CP:classes Radar
