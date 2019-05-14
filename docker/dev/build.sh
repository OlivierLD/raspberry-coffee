#!/usr/bin/env bash
#!/bin/sh
SKIP_FIRST=true
#
if [ "$SKIP_FIRST" != "true" ]
then
  rm -rf ic-components
  git clone https://orahub.oraclecorp.com/fmw-bpm-composer/ic-components.git
  cd ic-components
  ls -lisah
  #
  echo "Make sure you've set the proxies if running inside a firewall"
  # HTTP_PROXY=http://www-proxy.us.oracle.com:80
  # HTTPS_PROXY=http://www-proxy.us.oracle.com:80
  #
  yarn
  cd ..
fi
#
rm -rf integration-suite
git clone https://orahub.oraclecorp.com/fmw-bpm-composer/integration-suite.git
cd integration-suite
git checkout features/user-management-v2
#
if [ "$(uname)" == "Darwin" ]; then
        _home=$(greadlink -f ${BASH_SOURCE[0]})
else
        _home=$(readlink -f ${BASH_SOURCE[0]})
fi

if [ -z "$(echo $0 | grep bash)" ]; then
        echo "You MUST run . ${_home}"
        exit 1;
fi
#
BPMHOME=$(dirname $_home)

# ENVIRONMENT VARS:
export BPMHOME
export ORAHUB_PROJECT_ID=5912
export MAVEN_LOCAL_REPO=$HOME/.m2
#export MAVEN_LOCAL_REPO=/scratch/oic/maven-repo
export BPM_TOOLS_DIR=$HOME/repositories/tools
export ESE_RTF_LOCAL_URL=http://artifactory-slc-prod.oraclecorp.com/artifactory
#export ESE_RTF_LOCAL_URL=http://artifactory-slc.oraclecorp.com/artifactory/fmw-virtual
export GIT_LOCATION=US
export GIT_REPOSITORY=true
#
# PROXY_SETTINGS="-Dhttp.proxyHost=www-proxy-hqdc.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy-hqdc.us.oracle.com -Dhttps.proxyPort=80"
mvn clean install --settings settings.xml -Dhttps.enabled=false  -DskipTests  -Dcobertura.skip
cd ..
