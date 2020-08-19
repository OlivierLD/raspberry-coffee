#!/bin/bash
#
# Move this to your $HOME directory, and modify the pushd command below accordingly
# Compare to rpi.status.sh ;)
#
pushd raspberry-coffee/common-utils
  ../gradlew run
popd
