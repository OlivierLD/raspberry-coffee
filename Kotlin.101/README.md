## Kotlin Samples. More to come!

Among others, Kotlin come with tools like `kotlinc` and `kotlinc-jvm`.

See below how to:
- Compile a Kotlin file
- Run it
- Run a Kotlin Script
- How to use the Kotlin REPL

After installing `kotlinc` as explained [here](https://kotlinlang.org/docs/tutorials/command-line.html),
from the directory `Kotlin.101/src/main/kotlin`:

#### Compilation
```
$ kotlinc KotlinSensors.kt -cp ../../../../I2C.SPI/build/classes/main/ -include-runtime -d sensors.jar
```

#### Execution
There is a script named `runSensor`:
```
#!/bin/bash
PI4J_HOME=/opt/pi4j
CP=../../../../I2C.SPI/build/classes/main/
CP=$CP:sensors.jar
CP=$CP:$PI4J_HOME/lib/pi4j-core.jar
#
sudo java -cp $CP KotlinSensorsKt
```
Execute it:
```
$ ./runSensor 
Temp:23.418814 ºC, Press:1018.09607 hPa, Hum:64.762695 %
```

#### Run a Kotlin Script (`.kts`)
```
$ kotlinc -script hello.kts
Hello Kotlin World!
$
```

Also:
```
$ PI4J_HOME=/opt/pi4j
$ CP=../../../../I2C.SPI/build/classes/main/
$ CP=$CP:$PI4J_HOME/lib/pi4j-core.jar
$ sudo `which kotlinc` -cp $CP -script sensors.kts
Temp= 23.80598 ºC
$ 
```

#### REPL
Kotlin come with a REPL (Read-Execute-Print-Loop), like Scala:
```
$ PI4J_HOME=/opt/pi4j
$ CP=../../../../I2C.SPI/build/classes/main/
$ CP=$CP:$PI4J_HOME/lib/pi4j-core.jar
$ sudo `which kotlinc-jvm` -cp $CP
Welcome to Kotlin version 1.0.3 (JRE 1.8.0_65-b17)
Type :help for help, :quit for quit
>>> import i2c.sensor.BME280
>>> val bme280 = BME280()
>>> val temp = bme280.readTemperature()
>>> println("Temp= $temp \u00baC")
Temp= 23.80598 ºC
>>> :quit
$ 
```

Cool, hey?
