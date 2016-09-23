# JVM-aware languages
Java runs on the Raspberry PI. Running Java means that there is a Java Virtual Machine (JVM) that can take a `.class` file and execute it.
A `.class` file contains _byte code_. To see what the _byte code_ looks like, you can use `javap` with its `-c` flag:
```
$ javap -c -cp ./build/classes/main ./build/classes/main/mainRPi.class
Compiled from "mainRPi.groovy"
public class mainRPi extends groovy.lang.Script {
  public static transient boolean __$stMC;

  public mainRPi();
    Code:
       0: aload_0
       1: invokespecial #14                 // Method groovy/lang/Script."<init>":()V
       4: invokestatic  #18                 // Method $getCallSiteArray:()[Lorg/codehaus/groovy/runtime/callsite/CallSite;
       7: astore_1
       8: return

  public mainRPi(groovy.lang.Binding);
    Code:
       0: invokestatic  #18                 // Method $getCallSiteArray:()[Lorg/codehaus/groovy/runtime/callsite/CallSite;
       3: astore_2
       4: aload_0
       5: aload_1
       6: invokespecial #23                 // Method groovy/lang/Script."<init>":(Lgroovy/lang/Binding;)V
       9: return

  public static void main(java.lang.String...);
    Code:
       0: invokestatic  #18                 // Method $getCallSiteArray:()[Lorg/codehaus/groovy/runtime/callsite/CallSite;
       3: astore_1
       4: aload_1
       5: ldc           #28                 // int 0
  ...
```
 
This does not look at all like Java, does it?

From a Java file (with a `.java` extension) you get a `.class` file by using the `javac` compiler.

But here is the trick: You can come up with your own language, and if you can write your own compiler that turns your 
language-specific files into `.class` files supported by the JVM (a library like [ASM](http://asm.ow2.org/) can help you with that), 
you can then take advantage of the features of the JVM, big ones of them being its portability and interoperability. No need to mention 
that it can use at runtime `jars` and other `class` files, whatever language they have originally be written in.

That is - in very short - what those JVM-aware languages are doing. 
We have here snippets of Scala, Groovy, and Kotlin. The list is not closed, by far! 
 
## Scala 
Coming...
### Scala REPL

## Groovy on Pi
This is a small Groovy project that shows how to use Java classes written for the `Raspberry PI`
from a Groovy script.

Groovy is similar to Scala in the sense that
* It runs on a JVM
* It knows what a `.class` file is, whatever language it has been compiled from.

Those interested will also take a look at the Java part of the project, showing how to invoke scripting languages (Groovy, JavaScript) from Java,
using the features of the JSR223. Do check it out, it's worth it.

### Read a BME280 from groovy
From the project's root:
```
$ ./scripts/groovy/run
==========
Now running some RPi stuff
Temperature: 20.87 C
Pressure   : 1017.97 hPa
Humidity   : 66.91 %
CPU Temperature   :  47.8
CPU Core Voltage  :  1.325
```

## Kotlin
Among others, Kotlin come with tools like `kotlinc` and `kotlinc-jvm`.

See below how to:
- Compile a Kotlin file
- Run it
- Run a Kotlin Script
- How to use the Kotlin REPL

After installing `kotlinc` as explained [here](https://kotlinlang.org/docs/tutorials/command-line.html),
from the project's root directory:

#### Compilation
```
$ cd src/kotlin
$ kotlinc KotlinSensors.kt -cp ../../../I2C.SPI/build/classes/main/ -include-runtime -d sensors.jar
```

#### Execution
There is a script named `runSensor`:
```
#!/bin/bash
#
# Reads a sensor from Kotlin
#
PI4J_HOME=/opt/pi4j
CP=../../../../I2C.SPI/build/classes/main/
CP=$CP:../../src/kotlin/sensors.jar
CP=$CP:$PI4J_HOME/lib/pi4j-core.jar
#
# echo $CP
#
sudo java -cp $CP KotlinSensorsKt
```
Execute it:
```
$ cd script/kotlin
$ ./runSensor 
Temp:23.418814 ºC, Press:1018.09607 hPa, Hum:64.762695 %
```

#### Run a Kotlin Script (`.kts`)
```
$ cd src/kotlin
$ kotlinc -script hello.kts
Hello Kotlin World!
$
```

Also:
```
$ cd src/kotlin
$ PI4J_HOME=/opt/pi4j
$ CP=../../../I2C.SPI/build/classes/main/
$ CP=$CP:$PI4J_HOME/lib/pi4j-core.jar
$ sudo `which kotlinc` -cp $CP -script sensors.kts
Temp= 23.80598 ºC
$ 
```

#### REPL
Kotlin come with a REPL (Read-Execute-Print-Loop), like Scala:
```
# From the project's root
#
$ PI4J_HOME=/opt/pi4j
$ CP=../I2C.SPI/build/classes/main/
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
