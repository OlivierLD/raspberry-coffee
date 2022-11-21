#!/bin/bash
echo Unsetting proxy
if [[ "$(uname)" == "Darwin" ]]; then
  _home=$(greadlink -f ${BASH_SOURCE[0]})
else
  _home=$(readlink -f ${BASH_SOURCE[0]})
fi

if [[ -z "$(echo ${0} | grep bash)" ]]; then
  echo "You MUST run . ${_home}"
  exit 1;
fi
#
git config --global --unset http.proxy
git config --global --unset https.proxy
echo done.
echo ------ Config ------
git config --list
echo --------------------
#
unset HTTP_PROXY
unset HTTPS_PROXY
#
# For npm
npm config rm proxy
npm config rm http-proxy
npm config rm https-proxy
npm config list
