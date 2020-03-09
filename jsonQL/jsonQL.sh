#!/bin/bash
# echo $*
NB=1
PRMS=
for ARG in "$@"
do
	# echo "Managing prm $ARG"
  echo "${NB} : ${ARG}"
  if [[ ${NB} == 1 ]]
  then
    PRMS=${ARG}
  fi
  NB=$((${NB} + 1))
done
#
CP=./build/libs/jsonQL-1.0-all.jar
#
java -cp $CP oliv.json.JsonQL  -q Query  # ${PRMS}

