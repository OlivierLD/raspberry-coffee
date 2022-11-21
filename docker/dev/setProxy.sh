#!/bin/bash
echo Setting proxy...
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
# PROXY_HOST=www-proxy-hqdc.us.oracle.com
PROXY_HOST=www-proxy.us.oracle.com
PROXY_PORT=80
# git config --global http.proxy http://www-proxy-hqdc.us.oracle.com:80
git config --global http.proxy http://${PROXY_HOST}:${PROXY_PORT}
echo -n "Proxy: "
git config --global --get http.proxy
#
echo setting HTTP_PROXY and HTTPS_PROXY \(make sure you run the script with .\)
# export HTTP_PROXY=http://www-proxy-hqdc.us.oracle.com:80
# export HTTPS_PROXY=http://www-proxy-hqdc.us.oracle.com:80
export HTTP_PROXY=http://${PROXY_HOST}:${PROXY_PORT}
export HTTPS_PROXY=http://${PROXY_HOST}:${PROXY_PORT}
#
npm config set proxy $HTTP_PROXY
npm config set http-proxy $HTTP_PROXY
npm config set https-proxy $HTTP_PROXY
