#!/usr/bin/env bash
#
echo -e "Swagger generation"
#
GENERATION_FOLDER=./generated/jaxrs
YAML_FILE=./yaml/sample.yaml
DESTINATION_PACKAGE=oliv.io
FLAVOR=jaxrs-jersey
# FLAVOR=nodejs-express-server
if [ -d ${GENERATION_FOLDER} ]
then
	echo -e "----------------------------------------------------------------------------------------------------"
  echo -e "Folder ${GENERATION_FOLDER} exists. The changes you may have made would be lost if we drop the folder."
	echo -e "----------------------------------------------------------------------------------------------------"
  echo -en "Do you want to drop it? y|n > "
  read a
  if [ "$a" == "y" ] || [ "$a" == "Y" ]
  then
		rm -rf ${GENERATION_FOLDER}
  else
    echo -e "Change the directory name in the script, or delete the folder yourself.\nExiting."
    exit 0
  fi
fi
OPENAPI_GENERATOR=openapi-generator
GENERATOR_EXISTS=$(which ${OPENAPI_GENERATOR})
if [[ "${GENERATOR_EXISTS}" == "" ]]
then
  echo -e "---------------------------------------------------"
  echo -e "Trying plan B, openapi-generator was not found on this system."
  echo -e "Assume that you've downloaded the jar with"
  echo -e "wget https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/3.3.4/openapi-generator-cli-3.3.4.jar -O openapi-generator-cli.jar"
  echo -e "---------------------------------------------------"
  OPENAPI_GENERATOR="java -jar openapi-generator-cli.jar"
fi
#
VERBOSE=
# VERBOSE=--verbose
# swagger-codegen generate --lang jaxrs-jersey --input-spec $YAML_FILE --output $GENERATION_FOLDER --api-package $DESTINATION_PACKAGE $VERBOSE
# openapi-generator generate --generator-name jaxrs-jersey --input-spec $YAML_FILE --output $GENERATION_FOLDER --package-name $DESTINATION_PACKAGE $VERBOSE
# With custom templates
TEMPLATE_DIR=~/.openapi-generator/JavaJaxRS/libraries/jersey1
COMMAND="${OPENAPI_GENERATOR} generate --generator-name ${FLAVOR} --input-spec ${YAML_FILE} --output ${GENERATION_FOLDER} --api-package ${DESTINATION_PACKAGE} --template-dir ${TEMPLATE_DIR} ${VERBOSE}"
echo -e "We are going to run:"
echo -e "${COMMAND}"
echo -en "Proceed y|n ? > "
read REPLY
#
if [[ ! ${REPLY} =~ ^(yes|y|Y)$ ]]
then
  echo "Canceled."
  exit 0
fi
${COMMAND}
