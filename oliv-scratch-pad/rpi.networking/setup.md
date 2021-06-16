# Networking on the Raspberry Pi
See your config:
```
$ iwconfig
```
See your IP address(es)
```
$ ifconfig
```

Resources:
- April 22, 2013: https://spin.atomicobject.com/2013/04/22/raspberry-pi-wireless-communication/
- May 6, 2020:  https://www.maketecheasier.com/how-to-turn-raspberry-pi-into-wireless-access-point/

See those 2 files
=================
```
$ cat /etc/network/interfaces
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

# allow-hotplug wlan1
# iface wlan1 inet dhcp
# wpa-ssid "ATT856"
# wpa-psk "4314681968"

allow-hotplug wlan1
iface wlan1 inet dhcp
wpa-ssid "Sonic-00e0_EXT"
wpa-psk "67369c7831"

#iface wlan1 inet manual
#    wpa-conf /etc/wpa_supplicant/wpa_supplicant.conf
```

Other option:
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

```
$ cat /etc/hostapd/hostapd.conf
interface=wlan0
# driver=rtl871xdrv
ssid=SunFlower-Net
country_code=US
hw_mode=g
channel=6
macaddr_acl=0
auth_algs=1
ignore_broadcast_ssid=0
wpa=2
wpa_passphrase=raspberrypi
wpa_key_mgmt=WPA-PSK
wpa_pairwise=CCMP
wpa_group_rekey=86400
ieee80211n=1
wme_enabled=1
```

From scratch, enable hotspot (router)  
You might not be able to connect to it...

```
sudo apt update
sudo apt upgrade
sudo reboot

sudo apt install hostapd

sudo systemctl unmask hostapd
sudo systemctl enable hostapd

sudo apt install dnsmasq
sudo DEBIAN_FRONTEND=noninteractive apt install -y netfilter-persistent iptables-persistent

== Edit /etc/dhcpcd.conf, at the bottom, add
interface wlan0
    static ip_address=192.168.50.10/24
    nohook wpa_supplicant

- Make sure the address above does not conflict with another one on your network..., like your home router.

== Enable routing, edit /etc/sysctl.d/routed-ap.conf, add
net.ipv4.ip_forward=1

sudo iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE
sudo netfilter-persistent save

== Configure DHCP & DNS Services
sudo mv /etc/dnsmasq.conf /etc/dnsmasq.conf.orig
sudo vi /etc/dnsmasq.conf

== add the following options
interface=wlan0
dhcp-range=192.168.50.11,192.168.50.30,255.255.255.0,24h
domain=wlan
address=/gw.wlan/192.168.50.10

== Create network name and password
sudo vi /etc/hostapd/hostapd.conf

== See NetworkName and PassphrasePassphrase

interface=wlan0
ssid=NetworkName
hw_mode=g
channel=7
macaddr_acl=0
auth_algs=1
ignore_broadcast_ssid=0
wpa=2
wpa_passphrase=PassphrasePassphrase
wpa_key_mgmt=WPA-PSK
wpa_pairwise=TKIP
rsn_pairwise=CCMP

== Reboot if needed.
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

# allow-hotplug wlan1
# iface wlan1 inet dhcp
# wpa-ssid "ATT856"
# wpa-psk "4314681968"

allow-hotplug wlan1
iface wlan1 inet dhcp
wpa-ssid "Sonic-00e0_EXT"
wpa-psk "67369c7831"

#iface wlan1 inet manual
#    wpa-conf /etc/wpa_supplicant/wpa_supplicant.conf
```

### hostapd
```
sudo apt install hostapd

sudo systemctl unmask hostapd
sudo systemctl enable hostapd
```
> /etc/hostapd/hostapd.conf
```
interface=wlan0
# driver=rtl871xdrv
ssid=SunFlower-Net
country_code=US
hw_mode=g
channel=6
macaddr_acl=0
auth_algs=1
ignore_broadcast_ssid=0
wpa=2
wpa_passphrase=raspberrypi
wpa_key_mgmt=WPA-PSK
wpa_pairwise=CCMP
wpa_group_rekey=86400
ieee80211n=1
wme_enabled=1
```

```
systemctl disable dhcpcd
```