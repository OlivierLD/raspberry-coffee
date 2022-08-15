#!/usr/bin/env bash
echo "Usage is"
echo -e "  . $0 [false]|true"
echo "- true means 'skip the first part' (ic-components cloning and yarning)"
echo "Make sure you've set the proxies if running inside a firewall"
#
if [[ "$(uname)" == "Darwin" ]]; then
  _home=$(greadlink -f ${BASH_SOURCE[0]})
else
  _home=$(readlink -f ${BASH_SOURCE[0]})
fi

if [[ -z "$(echo $0 | grep bash)" ]]; then
  echo "You MUST run . ${_home}"
  exit 1;
fi
#
SKIP_FIRST=$1
#
if [[ "$SKIP_FIRST" != "true" ]]; then
  echo "Dropping previous directory..."
  rm -rf ic-components
  git clone https://orahub.oraclecorp.com/fmw-bpm-composer/ic-components.git
  cd ic-components
  echo "IMPORTANT: Checking out branch features/user-management-v2"
  git checkout features/user-management-v2
  ls -lisah
  #
  # HTTP_PROXY=http://www-proxy.us.oracle.com:80
  # HTTPS_PROXY=http://www-proxy.us.oracle.com:80
  #
  echo "Yarning..."
  yarn
  cd ..
else
  echo "Skipping the ic-components part..."
fi
#
echo "Dropping previous directory..."
rm -rf integration-suite
git clone https://orahub.oraclecorp.com/fmw-bpm-composer/integration-suite.git
cd integration-suite
echo "IMPORTANT: Checking out branch features/user-management-v2"
git checkout features/user-management-v2
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
echo "Running Maven..."
# PROXY_SETTINGS="-Dhttp.proxyHost=www-proxy-hqdc.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy-hqdc.us.oracle.com -Dhttps.proxyPort=80"
mvn clean install --settings settings.xml -Dhttps.enabled=false -DskipTests -Dcobertura.skip
cd ..
echo "Done!"
