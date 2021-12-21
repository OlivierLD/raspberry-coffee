#!/bin/bash
#
# Download weather faxes locally
# Start HTTP Server
# Open Web Page (fax re-working is done in there)
#
# 1.Download faxes
#
# North-West Atlantic: https://tgftp.nws.noaa.gov/fax/PYAA12.gif
wget https://tgftp.nws.noaa.gov/fax/PYAA12.gif --output-document NW-Atl.gif
# North-East Atlantic: https://tgftp.nws.noaa.gov/fax/PYAA11.gif
wget https://tgftp.nws.noaa.gov/fax/PYAA11.gif --output-document NE-Atl.gif
# North Atlantic 500mb: https://tgftp.nws.noaa.gov/fax/PPAA10.gif
wget https://tgftp.nws.noaa.gov/fax/PPAA10.gif --output-document N-Atl-500mb.gif
# North Atlantic Sea State
wget https://tgftp.nws.noaa.gov/fax/PJAA99.gif --output-document N-Atl-waves.gif
#
# 2.Start small python server
# See https://rawsec.ml/en/python-3-simplehttpserver/
#
python3 -m http.server 8080 &
SERVER_PROCESS_ID=$(echo $!)
echo -e "To kill the server, used PID ${SERVER_PROCESS_ID}"
#
# 3.Open the page
#
OS=`uname -a | awk '{ print $1 }'`
if [[ "$OS" == "Darwin" ]]
then
  open http://localhost:8080/process.faxes.html
else 
  XDG=$(which xdg-open)
  if [[ "${XDG}" != "" ]]
  then
    xdg-open http://localhost:8080/process.faxes.html
  else 
    echo -e "Enable to open the web page... Sorry."  
  fi
fi

