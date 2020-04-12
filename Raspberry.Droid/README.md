## Android, Android Things, etc, WIP
Android (Android Things) can run on the Raspberry pi.
This section is about giving it a try...

This also illustrates the way to use some of the libraries of this project
fro an Android Application (using a Maven repository).

- [Install Android Things on Raspberry Pi](https://developer.android.com/things/hardware/raspberrypi)
- [Install Android Studio](https://developer.android.com/studio/install)

---
### Projects
- [`GraphicSample`](./GraphicSample), basic sample, shows how to _draw_ a graphic (Tide curve, or what not).
- [`FingerPaint`](.FingerPaint), another graphic sample.
- [`AstroComputer`](./AstroComputer), a phone or tablet app, displaying current time, current position, and a chosen celestial body's coordinates (elevation and azimuth)

| Screenshot |
|:-----------|
| ![Astro](./Screenshot_Astro_Computer.jpg) |

This one could use other projects' code, through Maven.

---

### Notes
- `adb` basic usage: <https://www.androidcentral.com/10-basic-terminal-commands-you-should-know>
- `adb`is in `/Users/olediour/Library/Android/sdk/platform-tools`

```
 $ export PATH=$PATH:/Users/olediour/Library/Android/sdk/platform-tools
 $ adb devices [-l]
List of devices attached
e2df64a3	device
emulator-5554	device
```
then
```
 $ adb -s e2df64a3 shell
heroqlteatt:/ $
```

### Maven
See this <https://github.com/OlivierLD/raspberry-coffee/tree/repository>

The idea is to have a Maven repo possibly containing the artifacts generated from the `raspberry-coffee` one,
so they can be referred to from a `gradle` script for Android.

When building those artifacts (before deploying them to the Maven repo), Java version will be something to keep an eye on...

#### GUI
The GUI is defined by the `xml` files in the `res` section.

Android Studio comes with a graphical editor for those files.

---
