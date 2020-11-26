#!/bin/bash
#
# Magnetometer HMC5883L, and OLED Screen SSD1306
#
# pushd ~pi/raspberry-coffee/NMEA-mux-WebUI/compass-mux
./mux.sh nmea.mux.hmc5883l.oled.yaml
# popd
echo -e " Try curl -X GET \"$(hostname -I):9991/mux/cache\" | jq"
