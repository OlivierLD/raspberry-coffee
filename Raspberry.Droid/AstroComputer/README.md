## Android Astronomical Computer
This is a basic Android application.

What it does:
- Reads the position from the internal GPS
- Get the time from the internal clock
- Computes the chosen body's position and displays for the current position and time
    - the chosen body's elevation
    - the chosen body's azimuth
- Available bodies are
    - Sun
    - Moon
    - Venus
    - Mars
    - Jupiter
    - Saturn

Uses permissions, and dependencies on other projects.

Much better managed with `Android Studio`. See [here](https://developer.android.com/studio/install).

Also implements some logging features.
GPS Data would be logged in `GPS_DATA.csv`, located in `/Android/data/oliv.android.astrocomputer/files`.

> To get to the logged data:

> You can use the Android Debugging Bridge (aka `adb`), located in `$HOME/Library/Android/sdk/platform-tools` if you have installed Android Studio.

```
 $ export PATH=$PATH:$HOME/Library/Android/sdk/platform-tools
```

To get the connected devices ID's:
```
 $ adb devices -l
   List of devices attached
   e2df64a3               device usb:337641472X product:heroqlteuc model:SAMSUNG_SM_G930A device:heroqlteatt transport_id:58
```

To get the log file:
```
 $ adb pull sdcard/Android/data/oliv.android.astrocomputer/files/GPS_DATA.csv .
 sdcard/Android/data/oliv.android.astrocomputer/files/GPS_DATA.csv: 1 file pulled. 0.6 MB/s (2342 bytes in 0.004s)
```
The log file named `GPS_DATA.log` is in your current directory, it can be opened as a spreadsheet.

> Note: It is your responsibility to remove the log file when needed. Data would be **appened** to it if it exists.
> To do so:
```
 $ adb shell
heroqlteatt:/ $ rm sdcard/Android/data/oliv.android.astrocomputer/files/GPS_DATA.csv
```

You can also - if you do not want to deal with the command line interface - use a tool like [Android File Transfer](https://www.android.com/filetransfer/).

### Permissions
Requires `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` for the GPS data.
And `READ_EXTERNAL_STORAGE` and `WRITE_EXTERNAL_STORAGE` for the logging.

On the phone, go to `Settings` > `Apps` > `Astro Computer` > `Permissions`, and turn `Location` on.

### Dependencies
Will use the Celestial resources from other projects (in [`raspberry.coffee`](https://github.com/OlivierLD/raspberry-coffee)), for the calculations.

> Note: those resources are **copied** for now, as they include packages not supported on Android (like `awt`).

#### To refer to a maven-installed package
Install it on your local maven repo:
```
 $ cd [some.where]/raspberry-coffee/common-utils
 $ ../gradlew clean install
```
Then its content can be referred to as in:
```groovy
 dependencies {
    . . .
    implementation 'oliv.raspi.coffee:common-utils:1.0'
    . . .
 }
```

> Note: Android may require Java 8, not 9.

## Layout Editor
- See <https://developer.android.com/training/basics/firstapp/building-ui>
