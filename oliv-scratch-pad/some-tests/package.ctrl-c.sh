#!/bin/bash
FROM_DIR=$(pwd)
echo -e "Packaging the Ctrl-C demo..."
rm -rf classes
javac -d classes -s src/main/java \
        src/main/java/oliv/interrupts/*.java
#
echo -e "To run:"
echo -e "java -cp classes oliv.interrupts.CtrlCBad"
echo -e "or"
echo -e "java -cp classes oliv.interrupts.CtrlCGood"
#
echo -e "Done."
