#!/bin/bash
FROM_DIR=$(pwd)
echo -e "Compiling oliv.oda.flow.UpdateV1..."
if [[ -d classes ]]
then
  echo -e "Dropping the classes folder"
  rm -rf classes
fi
mkdir classes
#
# No debug option (to keep it small)
# Add a '-g' to the javac command to have it.
#
echo -e "Now compiling"
# -verbose
javac -g -d classes -s src/main/java \
      src/main/java/oliv/oda/flow/UpdateV1.java
echo -e "Done compiling."
#
# Run it
#
echo -en "Run it ? > "
read REPLY
if [[ $REPLY =~ ^(yes|y|Y)$ ]]
then
  java -cp classes oliv.oda.flow.UpdateV1
fi
echo -e "Done"
