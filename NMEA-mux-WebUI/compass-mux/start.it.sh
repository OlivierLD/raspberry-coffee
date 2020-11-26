#!/bin/bash
#
# Magnetometer HMC5883L, and OLED Screen SSD1306
#
# pushd ~pi/raspberry-coffee/NMEA-mux-WebUI/compass-mux
PROP_FILE="nmea.mux.hmc5883l.oled.yaml"
#
PORT=`cat ${PROP_FILE} | grep http.port:`
PORT=${PORT#*http.port: }
echo -e "Http port will be [${PORT}]"
echo -e " Try curl -X GET \"$(hostname -I):${PORT}/mux/cache\" | jq"
#
./mux.sh ${PROP_FILE}
# popd
