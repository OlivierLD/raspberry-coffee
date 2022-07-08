#!/bin/bash
FROM_DIR=$(pwd)
echo -e "Packaging the Astro Computer..." # Without using gradle...
rm -rf classes
rm -rf dist
mkdir classes
#
# No debug option (to keep it small)
# Add a '-g' to the javac command to have it.
#
find src/main/java -name '*.java' > sources.txt
# For windows:
# dir /s /B *.java > sources.txt
# cat sources.txt
javac -Xlint:deprecation -d classes -s src/main/java @sources.txt
mkdir dist
# Manifest
echo "Main-Class: celestial.almanac.JavaSample" > manifest.txt
echo "Compile-date: $(date)" >> manifest.txt
cd classes
# Add -v option to -cfm below for verbose
jar -cfm ../dist/astro.jar ../manifest.txt *
#
echo -e "Done."
echo -e "-------------------------------------------------"
echo -e "To run the program:"
echo -e "cd ./dist"
echo -e "java -jar astro.jar --now"
echo -e "-------------------------------------------------"
#
cd ${FROM_DIR}
rm manifest.txt
rm sources.txt
