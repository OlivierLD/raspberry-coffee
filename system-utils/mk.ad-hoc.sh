#!/bin/bash
ifconfig wlan0 down
iwconfig wlan0 channel 4
iwconfig wlan0 mode ad-hoc
iwconfig wlan0 essid 'boatraspi'
# iwconfig wlan0 key 'merde'
ifconfig wlan0 192.168.2.1
