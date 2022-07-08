## Misc resources and bulk notes.

BLE Setup:
- <https://www.instructables.com/id/Control-Bluetooth-LE-Devices-From-A-Raspberry-Pi/> (2015)
- <https://www.cnet.com/how-to/how-to-setup-bluetooth-on-a-raspberry-pi-3/> 
- <https://learn.pi-supply.com/make/fix-raspberry-pi-3-bluetooth-issues/>

From the UI, make the RPi discoverable

> hciconfig

> sudo service bluetooth status

On your Mac, choose `Apple menu` > `System Preferences`, then click `Bluetooth`. Your Mac is now discoverable.

Also see <https://www.piborg.org/blog/pi-zero-wifi-bluetooth>.

### On the Raspberry Pi
```
 $ lsusb
```

```
 $ hciconfig
 $ hcitool dev
 $ hcitool scan
```

```
 $ lsmod
 $ dmesg
```

```
 $ bluetoothctl
[NEW] Controller 00:15:83:0C:BF:EB rpi64 [default]
[bluetooth]# scan on
Discovery started
[CHG] Controller 00:15:83:0C:BF:EB Discovering: yes
[NEW] Device 18:65:90:CF:BF:80 18-65-90-CF-BF-80
[NEW] Device 00:19:6D:36:50:1B 00-19-6D-36-50-1B
[CHG] Device 18:65:90:CF:BF:80 LegacyPairing: no
[CHG] Device 18:65:90:CF:BF:80 RSSI: 127
[CHG] Device 18:65:90:CF:BF:80 Name: olediour-Mac
[CHG] Device 18:65:90:CF:BF:80 Alias: olediour-Mac
[CHG] Device 18:65:90:CF:BF:80 LegacyPairing: yes
[CHG] Device 18:65:90:CF:BF:80 RSSI is nil
[bluetooth]# quit
[DEL] Controller 00:15:83:0C:BF:EB rpi64 [default]
```

- Good post: <https://www.raspberrypi.org/forums/viewtopic.php?t=199308>
- To check: <https://www.evernote.com/shard/s188/nl/21492849/418caa6f-f527-4f37-a31d-39aee311cd1e/>

```
pi@rpi64:~ $ bluetoothctl
[NEW] Controller 00:15:83:0C:BF:EB rpi64 #1 [default]
[NEW] Device 18:65:90:CF:BF:80 olediour-Mac
[NEW] Controller B8:27:EB:97:05:FD rpi64
[NEW] Device 00:19:6D:36:50:1B OBDII
[NEW] Device 18:65:90:CF:BF:80 olediour-Mac
[NEW] Device 94:8B:C1:8C:5E:E5 Olivs-android-phone
[bluetooth]# power on
Changing power on succeeded
[bluetooth]# agent
Missing on/off/capability argument
[bluetooth]# agent on
Agent registered
[bluetooth]# agent
Missing on/off/capability argument
[bluetooth]# agent off
Agent unregistered
[bluetooth]# agent KeyboardOnly
Agent registered
[bluetooth]# pair 18:65:90:CF:BF:80
Attempting to pair with 18:65:90:CF:BF:80
[CHG] Device 18:65:90:CF:BF:80 Connected: yes
Request PIN code
[agent] Enter PIN code: 1234
[CHG] Device 18:65:90:CF:BF:80 Modalias: bluetooth:v004Cp4A3Ad1011
[CHG] Device 18:65:90:CF:BF:80 UUIDs: 00001101-0000-1000-8000-00805f9b34fb
[CHG] Device 18:65:90:CF:BF:80 UUIDs: 0000110a-0000-1000-8000-00805f9b34fb
[CHG] Device 18:65:90:CF:BF:80 UUIDs: 0000110c-0000-1000-8000-00805f9b34fb
[CHG] Device 18:65:90:CF:BF:80 UUIDs: 00001112-0000-1000-8000-00805f9b34fb
[CHG] Device 18:65:90:CF:BF:80 UUIDs: 00001117-0000-1000-8000-00805f9b34fb
[CHG] Device 18:65:90:CF:BF:80 UUIDs: 0000111f-0000-1000-8000-00805f9b34fb
[CHG] Device 18:65:90:CF:BF:80 UUIDs: 00001200-0000-1000-8000-00805f9b34fb
[CHG] Device 18:65:90:CF:BF:80 UUIDs: 02030302-1d19-415f-86f2-22a2106a0a77
[CHG] Device 18:65:90:CF:BF:80 ServicesResolved: yes
[CHG] Device 18:65:90:CF:BF:80 Paired: yes
Pairing successful
[CHG] Device 18:65:90:CF:BF:80 ServicesResolved: no
[CHG] Device 18:65:90:CF:BF:80 Connected: no
[DEL] Device 18:65:90:CF:BF:80 olediour-Mac
[bluetooth]# quit
pi@rpi64:~ $
```
