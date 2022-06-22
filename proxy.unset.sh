#!/bin/bash
#
git config --global --unset http.proxy
git config --global --unset https.proxy
#
git config --list
#
unset HTTP_PROXY
unset HTTPS_PROXY
#
# For npm
if [[ "$(which npm)" != "" ]]; then
  npm config rm proxy
  npm config rm http-proxy
  npm config rm https-proxy
  npm config list
fi
