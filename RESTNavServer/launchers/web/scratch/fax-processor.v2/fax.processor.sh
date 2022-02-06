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
# ./fax.processor.sh [--flavor:none|python|node|java] [--port:8080] [--verbose] [--kill-server:true] [--browser:true] [--help] [--spot:atl|pac]
# defaults:
# - flavor: python
# - port: 8080
# - verbose: false
# - kill-server: false
# - browser: true
# - spot: atl
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
WITH_BROWSER=true
SERVER_PROCESS_ID=
SPOT_OPTION=atl
#
for ARG in "$@"; do
  echo -e "Managing prm $ARG"
  if [[ ${ARG} == "--flavor:"* ]]; then
    SERVER_FLAVOR=${ARG#*:}
  elif [[ ${ARG} == "--browser:"* ]]; then
    WITH_BROWSER=${ARG#*:}
  elif [[ ${ARG} == "--spot:"* ]]; then
    SPOT_OPTION=${ARG#*:}
  elif [[ ${ARG} == "--port:"* ]]; then
    HTTP_PORT=${ARG#*:}
  elif [[ ${ARG} == "--kill-server:"* ]]; then
    KILL_SERVER=${ARG#*:}
  elif [[ "$ARG" == "-h" ]] || [[ "$ARG" == "--help" ]]; then
    echo -e "Usage is:"
    echo -e " ./fax.processor.sh [--flavor:none|python|node|java] [--port:8080] [--kill-server:true] [--verbose] [--browser:true] [--help] [--spot:atl|pac]"
    echo -e "    --flavor:none|python|node|java - The flavor of the HTTP server to start. Default python. 'none' does not start a server. It may reuse an already started one."
    echo -e "    --port:XXXX - HTTP port to use. Default 8080."
    echo -e "    --kill-server:true|false - Kill the server once the page is displayed. Default false."
    echo -e "    --browser:true|false - Default true."
    echo -e "    --spot:atl|pac - Default atl."
    echo -e "    --verbose, or -v - Default 'no verbose'."
    echo -e "    --help, or -h - Guess what! Exit after displaying help."
    echo -e "Bye now."
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
#
# Proxy?
PROXY_CMD=
# An example:
# PROXY_CMD="-e use_proxy=yes -e http_proxy=http://www-proxy.us.oracle.com:80 -e https_proxy=http://www-proxy.us.oracle.com:80"
#
# Values below are the same as in faxes.js, in the json bject named faxTransformer
if [[ "${SPOT_OPTION}" == "atl" ]]; then
  # North-West Atlantic Sat Pic: http://tropic.ssec.wisc.edu/real-time/atlantic/images/xxirg8bbm.jpg
  wget ${QUIET} ${PROXY_CMD} http://tropic.ssec.wisc.edu/real-time/atlantic/images/xxirg8bbm.jpg --output-document NW-Atl-SP.jpg
  echo -e "North-West Atlantic Sat Pic OK"
  # North-East Atlantic Sat Pic: http://tropic.ssec.wisc.edu/real-time/europe/images/xxirm7bbm.jpg
  wget ${QUIET} ${PROXY_CMD} http://tropic.ssec.wisc.edu/real-time/europe/images/xxirm7bbm.jpg --output-document NE-Atl-SP.jpg
  echo -e "North-East Atlantic Sat Pic OK"
  # North-West Atlantic: https://tgftp.nws.noaa.gov/fax/PYAA12.gif
  wget ${QUIET} ${PROXY_CMD} https://tgftp.nws.noaa.gov/fax/PYAA12.gif --output-document NW-Atl.gif
  echo -e "North-West Atlantic OK"
  # North-East Atlantic: https://tgftp.nws.noaa.gov/fax/PYAA11.gif
  wget ${QUIET} ${PROXY_CMD} https://tgftp.nws.noaa.gov/fax/PYAA11.gif --output-document NE-Atl.gif
  echo -e "North-East Atlantic OK"
  # North Atlantic 500mb: https://tgftp.nws.noaa.gov/fax/PPAA10.gif
  wget ${QUIET} ${PROXY_CMD} https://tgftp.nws.noaa.gov/fax/PPAA10.gif --output-document N-Atl-500mb.gif
  echo -e "North Atlantic 500mb OK"
  # North Atlantic Sea State: https://tgftp.nws.noaa.gov/fax/PJAA99.gif
  wget ${QUIET} ${PROXY_CMD} https://tgftp.nws.noaa.gov/fax/PJAA99.gif --output-document N-Atl-waves.gif
  echo -e "North Atlantic Sea State OK"
elif [[ "${SPOT_OPTION}" == "pac" ]]; then
  # North-East Pacific: https://tgftp.nws.noaa.gov/fax/PYBA90.gif
  wget ${QUIET} ${PROXY_CMD} https://tgftp.nws.noaa.gov/fax/PYBA90.gif --output-document NE-Pac.gif
  echo -e "North-West Pacific OK"
  # North-West Pacific: https://tgftp.nws.noaa.gov/fax/PYBA91.gif
  wget ${QUIET} ${PROXY_CMD} https://tgftp.nws.noaa.gov/fax/PYBA91.gif --output-document NW-Pac.gif
  echo -e "North-East Pacific OK"
  # North Pacific 500mb: https://tgftp.nws.noaa.gov/fax/PPBA10.gif
  wget ${QUIET} ${PROXY_CMD} https://tgftp.nws.noaa.gov/fax/PPBA10.gif --output-document N-Pac-500mb.gif
  echo -e "North Pacific 500mb OK"
  # North Pacific Set State: https://tgftp.nws.noaa.gov/fax/PJBA99.gif
  wget ${QUIET} ${PROXY_CMD} https://tgftp.nws.noaa.gov/fax/PJBA99.gif --output-document N-Pac-waves.gif
  echo -e "North Pacific Sea State OK"
  # Central Pacific Streamlines: https://tgftp.nws.noaa.gov/fax/PWFA11.gif
  wget ${QUIET} ${PROXY_CMD} https://tgftp.nws.noaa.gov/fax/PWFA11.gif --output-document C-Pac-streamlines.gif
  echo -e "Central Pacific Streamlines OK"
else
  echo -e "Unknown spot [${SPOT_OPTION}], aborting."
  exit 0
fi
#
# 2.Start small http server
#
if [[ "${VERBOSE}" == "true" ]]; then
  echo -e "======================"
  echo -e "2 - Starting ${SERVER_FLAVOR} HTTP server."
  echo -e "======================"
fi
if [[ "${SERVER_FLAVOR}" != "none" ]]; then
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
    JAVA_OPTIONS="${JAVA_OPTIONS} -Dautobind=false"
    JAVA_OPTIONS="${JAVA_OPTIONS} -Dwith.rest=false"
    JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.verbose=${VERBOSE}"
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
else
  echo "No server started"
fi
#
# 3.Open the page
#
if [[ "${VERBOSE}" == "true" ]]; then
  echo -e "======================"
  echo -e "3 - Opening Web Page."
  echo -e "======================"
fi
#
WEB_PAGE=process.atl.faxes.html
if [[ "${SPOT_OPTION}" == "atl" ]]; then
  WEB_PAGE=process.atl.faxes.html
elif [[ "${SPOT_OPTION}" == "pac" ]]; then
  WEB_PAGE=process.pac.faxes.html
fi
#
OS=$(uname -a | awk '{ print $1 }')
if [[ "${WITH_BROWSER}" == "true" ]]; then
  if [[ "$OS" == "Darwin" ]]; then
    open http://localhost:${HTTP_PORT}/${WEB_PAGE} &
  else
    SENSIBLE=$(which sensible-browser)
    if [[ "${SENSIBLE}" != "" ]]; then
      sensible-browser http://localhost:${HTTP_PORT}/${WEB_PAGE} &
    else
      XDG=$(which xdg-open)
      if [[ "${XDG}" != "" ]]; then
        xdg-open http://localhost:${HTTP_PORT}/${WEB_PAGE} &
      else
        echo -e "Enable to open the web page... Sorry."
      fi
    fi
  fi
else
  IP_ADDR=$(hostname -I | awk '{ print $1 }')
  SERVER_NAME="localhost"
  if [[ "${IP_ADDR}" != "" ]]; then
    SERVER_NAME=${IP_ADDR}
  fi
  echo -e "\tNO browser, as per user's choice."
  echo -e "\tReach the page at http://${SERVER_NAME}:${HTTP_PORT}/${WEB_PAGE}"
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
    if [[ "${SERVER_FLAVOR}" != "none" ]]; then
      echo -e "... No server started."
    fi
  fi
fi
