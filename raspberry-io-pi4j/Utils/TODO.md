## Stuff to work on...

- `PushButtonController`, this below happens on a single click.
```
Since last release of [Top-Button]: 1,659,107,284,534 ms.
Button [Top-Button] was down for 0 ms.
Button [Top-Button]: betweenClicks: 0 ms, pushedTime: 1,659,107,284,536 ms, releaseTime: 1,659,107,284,536, previousReleaseTime: 0 
Since last release of [Top-Button]: 0 ms.
Button [Top-Button] was down for 36 ms.
Button [Top-Button]: betweenClicks: 0 ms, pushedTime: 1,659,107,284,536 ms, releaseTime: 1,659,107,284,572, previousReleaseTime: 1,659,107,284,536 
Since last release of [Top-Button]: 6 ms.
Button [Top-Button] was down for 1 ms.
Since last release of [Top-Button]: 4 ms.
Button [Top-Button]: betweenClicks: 6 ms, pushedTime: 1,659,107,284,578 ms, releaseTime: 1,659,107,284,579, previousReleaseTime: 1,659,107,284,572 
++++ Setting maybeDoubleClick to false
>> Double click on button 1
Displaying local menu items
++++ maybeDoubleClick found false, it WAS a double click
Button [Top-Button] was down for 183 ms.
Button [Top-Button]: betweenClicks: 4 ms, pushedTime: 1,659,107,284,583 ms, releaseTime: 1,659,107,284,766, previousReleaseTime: 1,659,107,284,579 
++++ Setting maybeDoubleClick to false
>> Double click on button 1
++++ maybeDoubleClick found false, it WAS a double click
```
Seems to be related to the presence of _**two**_ buttons...

- When loading the `small-server-extended`:
```
WARNING: Illegal reflective access by com.pi4j.io.file.LinuxFile (file:/home/pi/nmea-dist/build/libs/small-server-extended-1.0-all.jar) to field java.nio.Buffer.address
WARNING: Please consider reporting this to the maintainers of com.pi4j.io.file.LinuxFile
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release
java.lang.NoClassDefFoundError: sun/misc/SharedSecrets
	at com.pi4j.io.file.LinuxFile.getFileDescriptor(LinuxFile.java:215)
	at com.pi4j.io.file.LinuxFile.ioctl(LinuxFile.java:103)
	at com.pi4j.io.i2c.impl.I2CBusImpl.selectBusSlave(I2CBusImpl.java:291)
	at com.pi4j.io.i2c.impl.I2CBusImpl.runBusLockedDeviceAction(I2CBusImpl.java:258)
	at com.pi4j.io.i2c.impl.I2CBusImpl.readByte(I2CBusImpl.java:153)
	at com.pi4j.io.i2c.impl.I2CDeviceImpl.read(I2CDeviceImpl.java:205)
	at i2c.sensor.utils.EndianReaders.readU8(EndianReaders.java:20)
	at i2c.sensor.utils.EndianReaders.readU16(EndianReaders.java:54)
	at i2c.sensor.utils.EndianReaders.readU16LE(EndianReaders.java:46)
	at i2c.sensor.BME280.readU16LE(BME280.java:150)
	at i2c.sensor.BME280.readCalibrationData(BME280.java:159)
	at i2c.sensor.BME280.<init>(BME280.java:128)
	at i2c.sensor.BME280.<init>(BME280.java:110)
	at nmea.consumers.reader.BME280Reader.<init>(BME280Reader.java:33)
	at nmea.mux.MuxInitializer.setup(MuxInitializer.java:485)
	at nmea.mux.GenericNMEAMultiplexer.<init>(GenericNMEAMultiplexer.java:185)
	at mux.MultiplexerWithTwoButtons.<init>(MultiplexerWithTwoButtons.java:578)
	at mux.MultiplexerWithTwoButtons.main(MultiplexerWithTwoButtons.java:803)
Caused by: java.lang.ClassNotFoundException: sun.misc.SharedSecrets
	at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:583)
	at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:178)
	at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:521)
	... 18 more
```

---