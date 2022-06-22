#!/bin/bash
#
PROXY_HOST=www-proxy.us.oracle.com
PROXY_PORT=80
#
git config --global http.proxy ${PROXY_HOST}:${PROXY_PORT}
git config --global https.proxy ${PROXY_HOST}:${PROXY_PORT}
#
git config --list
#
export HTTP_PROXY=http://${PROXY_HOST}:${PROXY_PORT}
export HTTPS_PROXY=http://${PROXY_HOST}:${PROXY_PORT}
#
if [[ "$(which npm)" != "" ]]; then
  npm config set proxy $HTTP_PROXY
  npm config set http-proxy $HTTP_PROXY
  npm config set https-proxy $HTTP_PROXY
  npm config list
fi
