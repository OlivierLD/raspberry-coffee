# Networking on the Raspberry Pi
See the (well done) official doc: <https://www.raspberrypi.org/documentation/configuration/wireless/>

> Do keep an eye on the IP addresses, 192.168.4.0, 1, 2...

See your config:
```
$ iwconfig
```
See your IP address(es)
```
$ ifconfig
```

Resources:
- April 22, 2013: <https://spin.atomicobject.com/2013/04/22/raspberry-pi-wireless-communication/>
- May 6, 2020:  <https://www.maketecheasier.com/how-to-turn-raspberry-pi-into-wireless-access-point/>


### Other option, ad-hoc network:
```
auto lo
iface lo inet loopback
iface eth0 inet dhcp

auto wlan0
iface wlan0 inet static
   address 192.168.1.1
   netmask 255.255.255.0
   wireless-channel 1
   wireless-essid RPiOnTheBoat
   wireless-mode ad-hoc
```
then
```
sudo ifdown wlan0
sudo ifup wlan0
```

## Working config for `SunFlower`

### interfaces
> /etc/network/interfaces
```
# interfaces(5) file used by ifup(8) and ifdown(8)

# Please note that this file is written to be used with dhcpcd
# For static IP, consult /etc/dhcpcd.conf and 'man dhcpcd.conf'

# Include files from /etc/network/interfaces.d:
source-directory /etc/network/interfaces.d

auto lo
iface lo inet loopback

iface eth0 inet manual

allow-hotplug wlan0
iface wlan0 inet static
address 192.168.127.1
netmask 255.255.255.0

#iface wlan0 inet manual
#    wpa-conf /etc/wpa_supplicant/wpa_supplicant.conf

allow-hotplug wlan1
iface wlan1 inet dhcp
wpa-ssid "Sonic-00e0_EXT"
wpa-psk "67...x...31"

#iface wlan1 inet manual
#    wpa-conf /etc/wpa_supplicant/wpa_supplicant.conf
```

> Notice above: with another WiFi dongle (wlan1) you can have a HotSpot **_AND_** Internet access.  
> That makes your life so much easier.
