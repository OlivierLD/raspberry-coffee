#!/bin/bash
#
# Display several weather faxes on the same canvas.
# Faxes are:
# Re-scaled, rotated
# Re-colored - based on their types (surface, 500mb, sea-state, ...0
# Made transparent, so they can be stacked, without hiding the lower ones.
# ====================================
# Steps:
# - Download weather faxes locally
# - Start HTTP Server
# - Open Web Page (fax re-working is done in there)
#
# Usage:
# ./fax.processor.sh [--flavor:python|node|java] [--port:8080] [--verbose] [--kill-server:true] [--help]
# defaults:
# - flavor: python
# - port: 8080
# - verbose: false
# - kill-server: false
#
##################################################################
#
echo -e "For help, type $0 --help"
echo -e "----------------------------------"
#
VERBOSE=false
SERVER_FLAVOR=python
HTTP_PORT=8080
KILL_SERVER=false
SERVER_PROCESS_ID=
#
for ARG in "$@"; do
  echo -e "Managing prm $ARG"
  if [[ ${ARG} == "--flavor:"* ]]; then
    SERVER_FLAVOR=${ARG#*:}
  elif [[ ${ARG} == "--port:"* ]]; then
    HTTP_PORT=${ARG#*:}
  elif [[ ${ARG} == "--kill-server:"* ]]; then
    KILL_SERVER=${ARG#*:}
  elif [[ "$ARG" == "-h" ]] || [[ "$ARG" == "--help" ]]; then
    echo -e "Usage is:"
    echo -e " ./fax.processor.sh [--flavor:python|node|java] [--port:8080] [--kill-server:true] [--verbose] [--help]"
    echo -e "    --flavor: The flavor of the HTTP server to start. Default python."
    echo -e "    --port: HTTP port to use. Default 8080."
    echo -e "    --kill-server: Kill the server once the page is displayed. Default false."
    echo -e "    --verbose, or -v. Default false."
    echo -e "    --help. Guess what!"
    exit 0
  elif [[ "$ARG" == "-v" ]] || [[ "$ARG" == "--verbose" ]]; then
    VERBOSE=true
  else
    echo -e "Parameter ${ARG} not managed."
    echo -e "See the script $0 for details."
  fi
done
#
# 1.Download faxes
#
if [[ "${VERBOSE}" == "true" ]]; then
  echo -e "======================"
  echo -e "1 - Downloading faxes."
  echo -e "======================"
fi
QUIET=
if [[ "${VERBOSE}" != "true" ]]; then
  QUIET="--quiet"
fi
# North-West Atlantic: https://tgftp.nws.noaa.gov/fax/PYAA12.gif
wget ${QUIET} https://tgftp.nws.noaa.gov/fax/PYAA12.gif --output-document NW-Atl.gif
# North-East Atlantic: https://tgftp.nws.noaa.gov/fax/PYAA11.gif
wget ${QUIET} https://tgftp.nws.noaa.gov/fax/PYAA11.gif --output-document NE-Atl.gif
# North Atlantic 500mb: https://tgftp.nws.noaa.gov/fax/PPAA10.gif
wget ${QUIET} https://tgftp.nws.noaa.gov/fax/PPAA10.gif --output-document N-Atl-500mb.gif
# North Atlantic Sea State
wget ${QUIET} https://tgftp.nws.noaa.gov/fax/PJAA99.gif --output-document N-Atl-waves.gif
#
# 2.Start small http server
#
if [[ "${VERBOSE}" == "true" ]]; then
  echo -e "======================"
  echo -e "2 - Starting ${SERVER_FLAVOR} HTTP server."
  echo -e "======================"
fi
if [[ "${SERVER_FLAVOR}" == "python" ]]; then
  #
  # See https://rawsec.ml/en/python-3-simplehttpserver/
  #
  echo -e "Starting python server"
  PYTHON_3=$(which python3)
  if [[ "${PYTHON_3}" == "" ]]; then
    echo -e "python3 must be installed on your system... Exiting."
    exit 1
  fi
  python3 -m http.server ${HTTP_PORT} &
  SERVER_PROCESS_ID=$(echo $!)
  echo -e "To kill the server, used PID ${SERVER_PROCESS_ID}"
elif [[ "${SERVER_FLAVOR}" == "node" ]]; then
  echo -e "Starting node server"
  NODE_JS=$(which node)
  if [[ "${NODE_JS}" == "" ]]; then
    echo -e "node must be installed on your system... Exiting."
    exit 1
  fi
  node server.js --verbose:${VERBOSE} --port:${HTTP_PORT} &
  SERVER_PROCESS_ID=$(echo $!)
  echo -e "To kill the server, used PID ${SERVER_PROCESS_ID}"
elif [[ "${SERVER_FLAVOR}" == "java" ]]; then
  echo -e "Starting java server"
  # CP=$(find ~/repos/raspberry-coffee -name http-tiny-server-1.0-all.jar)
  JAR_FILE=../../../../../http-tiny-server/build/libs/http-tiny-server-1.0-all.jar
  if [[ ! -f ${JAR_FILE} ]]; then
    echo -e "${JAR_FILE} not found where expected. Exiting"
    exit 1
  fi
  CP=${JAR_FILE}
  JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.verbose=${VERBOSE}"
  JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.port=${HTTP_PORT}"
  JAVA_OPTIONS="${JAVA_OPTIONS} -Dstatic.docs=/"
  # JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.super.verbose=true"
  echo -e "Will run: java -cp ${CP} ${JAVA_OPTIONS} http.HTTPServer &"
  java -cp ${CP} ${JAVA_OPTIONS} http.HTTPServer &
  SERVER_PROCESS_ID=$(echo $!)
  echo -e "To kill the server, used PID ${SERVER_PROCESS_ID}"
else
  echo -e "-----------------------------------------------------------------"
  echo -e "Unsupported server flavor [${SERVER_FLAVOR}]. Only 'python' (default), 'node', and 'java' are supported."
  echo -e "-----------------------------------------------------------------"
fi
#
# 3.Open the page
#
if [[ "${VERBOSE}" == "true" ]]; then
  echo -e "======================"
  echo -e "3 - Opening Web Page."
  echo -e "======================"
fi
OS=$(uname -a | awk '{ print $1 }')
if [[ "$OS" == "Darwin" ]]; then
  open http://localhost:${HTTP_PORT}/process.faxes.html
else
  SENSIBLE=$(which sensible-browser)
  if [[ "${SENSIBLE}" != "" ]]; then
    sensible-browser http://localhost:${HTTP_PORT}/process.faxes.html
  else
    XDG=$(which xdg-open)
    if [[ "${XDG}" != "" ]]; then
      xdg-open http://localhost:${HTTP_PORT}/process.faxes.html
    else
      echo -e "Enable to open the web page... Sorry."
    fi
  fi
fi
#
if [[ "${KILL_SERVER}" == "true" ]] && [[ "${SERVER_PROCESS_ID}" != "" ]]; then
  echo -e "Will kill the server"
  sleep 10 &&
    echo -e "Killing server process ${SERVER_PROCESS_ID}..." &&
    kill -9 ${SERVER_PROCESS_ID}
else
  if [[ "${SERVER_PROCESS_ID}" != "" ]]; then
    echo -e "Leaving server [${SERVER_PROCESS_ID}] alive"
  else
    echo -e "... No server started."
  fi
fi
