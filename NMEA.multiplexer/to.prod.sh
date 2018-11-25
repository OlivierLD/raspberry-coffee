#!/usr/bin/env bash
#
# WIP
#
echo -e "This is an example showing how to generate a 'production' version, without all the github repo"
echo -e "just what is needed to run the NMEA Multiplexer - in several configurations - and its web clients"
#
# 1 - Build
#
../gradlew shadowJar
#
# 2 - Create new dir
#
echo -en "Where should we create the distribution ? > "
read distdir
echo -e "Creating $distdir"
mkdir $distdir
mkdir $distdir/build
mkdir $distdir/build/libs
#
# 3 - Copying needed resources
#
echo -e "Copying resources"
cp ./build/libs/NMEA.multiplexer-1.0-all.jar $distdir/build/libs
# Log folder
mkdir $distdir/logged
# Web resources
cp -R web $distdir
# Properties files
cp *.properties $distdir
# If needed, more resources would go here (like dev curves, etc)
cp mux.sh $distdir
#
# 4 - Archiving
#
zip -q -r $distdir.zip $distdir
rm -rf $distdir
#
# 5 - Ready!
#
echo -e "For the runner/logger, use nmea.mux.gps.log.properties"
echo -e "Archive $distdir.zip ready for deployment. The script to launch will be 'mux.sh'"
