#!/usr/bin/env bash
echo "Temp $(/opt/vc/bin/vcgencmd measure_temp)"
echo "Core Voltage $(/opt/vc/bin/vcgencmd measure_volts core)"
echo "UName: $(uname -a | awk '{print $2}')"
echo "IP: $(hostname -I | cut -d' ' -f1)"
echo "CPU Load: $(top -bn1 | grep load | awk '{printf "CPU Load: %.2f%%", $(NF-2)*100}')"
echo "Memory usage: $(free -m | awk 'NR==2{printf "Mem: %s/%s MB %.2f%%", $3, $2, $3*100/$2 }')"
echo "Disk usage: $(df -h | awk '$NF=="/"{printf "Disk: %d/%d GB %s", $3, $2, $5}')"
