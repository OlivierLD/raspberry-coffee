#!/bin/bash
#
# Dumps the manifest of a jar file.
#
file_exists() {
  if [[ -f $1 ]]; then
    return 0  # true
  else
    return 1 # false
  fi
}
#
oops ( ) {
    echo
    echo "$*"
    echo
    exit 1
}
#
dump_manifest() {
  mkdir temp || oops "'temp' directory already exists in ${PWD}..."
  pushd temp
    jar -xf ../$1 META-INF/MANIFEST.MF
    echo -e "-------- MANIFEST.MF --------"
    cat META-INF/MANIFEST.MF
    echo -e "-----------------------------"
  popd
  rm -rf temp
}
# Default
JAR_FILE_NAME=./build/libs/small-server-extended-1.0-all.jar
# User provided as CLI prm
if [[ $# -gt 0 ]]; then
  JAR_FILE_NAME=$1
fi
#
if file_exists ${JAR_FILE_NAME} ; then
  echo -e "Found ${JAR_FILE_NAME}. Moving on."
  dump_manifest ${JAR_FILE_NAME}
else
  echo -e "File ${JAR_FILE_NAME} was NOT found. Not Ok."
fi
