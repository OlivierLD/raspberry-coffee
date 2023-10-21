#!/bin/bash
#
# Uncomment if needed...
# See <https://www.baeldung.com/linux/no-x11-display-error>
# export DISPLAY=:0.0
#
# CLI Parameters:
#--help, Help!!!
#--headless:, Headless version.
#--boat-def:, Boat definition, json file.
#--between-frames:, Space between frames, in cm
#--between-wl:, Space between Water Lines, in cm
#--between_buttocks:, Space between buttocks, in cm
#
CP=./build/libs/BoatDesign-1.0-all.jar
LOGGING_FLAGS="-Djava.util.logging.config.file=./logging.properties"
#
# init.json is the default init-file
#
OPT=
#
# Obsolete, use CLI prm --boat-def
# if [[ "$1" != "" ]]; then
#   OPT="-Dinit-file=$1"
# fi
#
OPT="${OPT} -Dspit-out-points=true"
OPT="${OPT} -Duser.language=en -Duser.country=US"
#
OPT="${OPT} -D3d-verbose=false"
# OPT="${OPT} -Dproximity-tolerance=8"
#
# The main class boatdesign.ThreeViews is in the MANIFEST (and build.gradle)
#
COMMAND="java ${OPT} ${LOGGING_FLAGS} -jar ${CP} $*"
echo -e "Running ${COMMAND}"
${COMMAND}
