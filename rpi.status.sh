#!/usr/bin/env bash
echo -e "+----------------+"
echo -e "| Machine status |"
echo -e "+----------------+"
VCGENCMD=/opt/vc/bin/vcgencmd
if [[ -f ${VCGENCMD} ]]; then
  echo -e "Temp $(/opt/vc/bin/vcgencmd measure_temp)"
  echo -e "Core Voltage $(/opt/vc/bin/vcgencmd measure_volts core)"
else
  echo -e "${VCGENCMD} not available."
fi
echo -e "UName: $(uname -a | awk '{print $2}')"
echo -e "IP: $(hostname -I | cut -d' ' -f1)"
echo -e "CPU Load: $(top -bn1 | grep load | awk '{printf "CPU Load: %.2f%%", $(NF-2)*100}')"
echo -e "Memory usage: $(free -m | awk 'NR==2{printf "Mem: %s/%s MB %.2f%%", $3, $2, $3*100/$2 }')"
echo -e "Disk usage: $(df -h | awk '$NF=="/"{printf "Disk: %d/%d GB %s", $3, $2, $5}')"
#
echo -e "-----------------------------------------------------------------"
echo -e "Commands executed at startup: See /etc/rc.local"
echo -e "-----------------------------------------------------------------"
echo -e "Executed when starting the Desktop, see ~/.config/lxsession/LXDE-pi/autostart", and add, at the end, the following lines:
echo -e "  like"
echo -e "@chromium-browser --incognito --kiosk [--force-device-scale-factor=0.90] http://localhost:9999/web/nmea/headup.html \\"
echo -e "                                      [url.2] \\"
echo -e "                                      [url.3] \\"
echo -e "                                      [url.4]"
echo -e "-----------------------------------------------------------------"


