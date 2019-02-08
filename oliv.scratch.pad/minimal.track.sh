#!/usr/bin/env bash
#
echo -en "Do we compile before running? [n]|y > "
read resp
if [ "$resp" == "y" ]
then
	#
	# 1. Compile
	#
	if [ -d "classes" ]
	then
	  rm -rf classes/*
	else
	  mkdir classes
	fi
	#
	echo -e "Compiling..."
	javac -d classes --source-path some.tests/src/main/java some.tests/src/main/java/oliv/misc/MinimalTrack.java
	#
	# 2. Archive
	#
	cd classes
	echo -e "Archiving..."
	jar -cvf ../track.jar ./*
	cd ..
fi
#
# 3. Run
#
echo -e "Running..."
#
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Dverbose=false"
JAVA_OPTS="$JAVA_OPTS -Dprops=track.properties"
java -cp track.jar $JAVA_OPTS oliv.misc.MinimalTrack
