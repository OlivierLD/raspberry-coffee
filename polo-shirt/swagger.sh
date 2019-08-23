#!/usr/bin/env bash
GENERATION_FOLDER=./generated/jaxrs
YAML_FILE=./yaml/sample.yaml
DESTINATION_PACKAGE=oliv.io
FLAVOR=jaxrs-jersey
# FLAVOR=nodejs-express-server
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
VERBOSE=
# VERBOSE=--verbose
# swagger-codegen generate --lang jaxrs-jersey --input-spec $YAML_FILE --output $GENERATION_FOLDER --api-package $DESTINATION_PACKAGE $VERBOSE
# openapi-generator generate --generator-name jaxrs-jersey --input-spec $YAML_FILE --output $GENERATION_FOLDER --package-name $DESTINATION_PACKAGE $VERBOSE
# With custom templates
TEMPLATE_DIR=~/.openapi-generator/JavaJaxRS/libraries/jersey1
COMMAND="openapi-generator generate --generator-name $FLAVOR --input-spec $YAML_FILE --output $GENERATION_FOLDER --api-package $DESTINATION_PACKAGE --template-dir $TEMPLATE_DIR $VERBOSE"
echo -e "We are going to run:"
echo -e "$COMMAND"
echo -en "Proceed y|n ? > "
read a
if [ "$a" == "y" ] || [ "$a" == "Y" ]
then
  $COMMAND
fi
