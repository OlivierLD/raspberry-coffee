#!/usr/bin/env bash
GENERATION_FOLDER=./generated/jaxrs
YAML_FILE=./yaml/sample.yaml
DESTINATION_PACKAGE=samples.io
if [ -d $GENERATION_FOLDER ]
then
  echo -en "$GENERATION_FOLDER exists. Do you want to drop it? [y]|n > "
  read a
  if [ "$a" == "y" ] || [ "$a" == "Y" ]
  then
		rm -rf $GENERATION_FOLDER
  else
    echo -e "Change the directory name or delete it yourself. Exiting."
    exit 0
  fi
fi
#
swagger-codegen generate --lang jaxrs-jersey --input-spec $YAML_FILE --output $GENERATION_FOLDER --api-package $DESTINATION_PACKAGE --verbose
