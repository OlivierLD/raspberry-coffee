#!/usr/bin/env bash
GENERATION_FOLDER=./generated/jaxrs
YAML_FILE=./yaml/sample.yaml
DESTINATION_PACKAGE=samples.io
if [ -d $GENERATION_FOLDER ]
then
	echo -e "----------------------------------------------------------------------------------------------------"
  echo -e "Folder $GENERATION_FOLDER exists. The changes you may have made would be lost if we drop the folder."
	echo -e "----------------------------------------------------------------------------------------------------"
  echo -en "Do you want to drop it? y|n > "
  read a
  if [ "$a" == "y" ] || [ "$a" == "Y" ]
  then
		rm -rf $GENERATION_FOLDER
  else
    echo -e "Change the directory name in the script, or delete the folder yourself.\nExiting."
    exit 0
  fi
fi
#
swagger-codegen generate --lang jaxrs-jersey --input-spec $YAML_FILE --output $GENERATION_FOLDER --api-package $DESTINATION_PACKAGE --verbose
