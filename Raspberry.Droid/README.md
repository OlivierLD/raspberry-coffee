## Android, Android Things, etc, WIP
Android (Android Things) can run on the Raspberry pi.
This section is about giving it a try...

- [Install Android Things on Raspberry Pi](https://developer.android.com/things/hardware/raspberrypi)
- [Install Android Studio](https://developer.android.com/studio/install)

---
### Projects
- [`AstroComputer`](./AstroComputer), a phone or tablet app, displaying current time, current position, and Sun coordinates (Elevation and azimuth)
![Astro](./Screenshot_Astro_Computer.jpg)

---

### Notes
- `adb` basic usage: <https://www.androidcentral.com/10-basic-terminal-commands-you-should-know>
- `adb`is in `/Users/olediour/Library/Android/sdk/platform-tools`

```
 $ export PATH=$PATH:/Users/olediour/Library/Android/sdk/platform-tools
 $ adb devives [-l]
List of devices attached
e2df64a3	device
emulator-5554	device
```
then
```
 $ adb -s e2df64a3 shell
heroqlteatt:/ $ 
```
