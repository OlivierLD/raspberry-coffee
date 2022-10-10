# Processing.org

[Processing](http://processing.org) is a framework that considerably simplifies Java development,
and provides amazing graphical features.

> See how to install it on the Raspberry Pi [here](https://www.raspberrypi.org/blog/now-available-for-download-processing/).

It seems to be compatible with the projects of this repository.

For example, here is the code that graphically displays the value returned by an ADC (MCP3008):
```
import analogdigitalconverter.mcp3008.MCPReader;
/*
 * Using Sketch > Add File..., select ADC/build/libs/ADC-1.0-all.jar
 */
int value;
int ADC_CHANNEL = MCP3008Reader.MCP3008_input_channels.CH0.ch(); // Between 0 and 7, 8 channels on the MCP3008

void setup() {
  size(200, 200);
  stroke(255);
  noFill();
  textSize(72);
  MCP3008Reader.initMCP3008();
}
void draw() { // Draw the value of the ADC (MCP3008) at each repaint
  background(0);
  fill(255);
  value = MCP3008Reader.readMCP3008(ADC_CHANNEL);
  text(String.format("%04d", value), 10, 100);
}
```
About 20 lines, only...

### To run the Sketch
- From the project root, to produce the required jar files, run
```bash
 $> ./gradlew clean shadowJar
```
  > Processing 3.0 might not like a JDK more recent than 1.8. Make sure you set 
  > your Java alternative correctly. Processing 4.0 seems to support more recent versions of Java (11, 17, etc).

#### Note about the JDK to use in Processing
In the installation directory, there is a full JDK. For example, my recent version
came with JDK 17, and I wanted to use a JDK 11, already installed on my Raspberry Pi.
The script `processing-java` explains how to change the JDK version.

To change the JDK version:
* In the installation directory, like `/tmp/processing-4.0.1`:
  * `mv java java.17`
  * `ln -s /usr/lib/jvm/java-11-openjdk-armhf ./java`  
  
Then JDK 11 will be used at runtime.  
To come back to default, unlink the `./java`, and name `java.17` back to `java`.

To see - at runtime - what version of the JDK you are using, run a script simple like this:

```java
void setup() {
    println(String.format("Java Version: %s", System.getProperty("java.version")));    
}
```
Then you'd see the java version in the console.  
Or, to see it also in the GUI:
```java
void setup() {
    println(String.format("Java Version: %s", System.getProperty("java.version")));
    
    size(600, 400);
    stroke(255);
    noFill();
    textSize(72);
    
    fill(255);
    text("Java Version:", 10, 75);
    text(String.format(" %s", System.getProperty("java.version")), 10, 150);
}
```

---




```
$ [sudo] update-alternatives --config java
$ [sudo] update-alternatives --config javac
```
- Open the sketch [MCP3008_Pie.pde](./src/processing/MCP3008_Pie/MCP3008_Pie.pde) in `Processing` (no need to copy it anywhere else)

  ![Open in Processing](./sketch.png)

- If instructed **_in the comment at the top of the sketch_**, use the menu `Sketch > Add File...` to select the appropriate archive, like here:
```java
/*
 * Using Sketch > Add File..., select ADC/build/libs/ADC-1.0-all.jar
 */
```
- Make sure your wiring is correct.
- Hit the `Run` button in `Processing`.
- Turn the button of the potentiometer, and see for yourself.

<table>
  <tr>
    <td valign="top">
      <img src="./mcp3008.png" title="MCP3008 pie">
    </td>
    <td valign="top">
      <img src="../ADC/RPi-MCP3008-Pot_bb.png" title="Wiring">
    </td>
  </tr>
</table>

### _A note_: Processing and Java
As you would notice, the code of a `sketch` looks like Java code, but it is not _exactly_ Java code...

Actually, the Processing Development Environment (aka `PDE`) wraps the code with what is missing for Java to be 100% happy.

If you have for example a sketch like that one:
```
void setup() {
  size(200, 200);
  stroke(255);
  noFill();
  textSize(72);
}

void draw() {
  background(0);
  fill(255);
  int value = (int)Math.floor(1023 * Math.random());  // Simulation
  text(String.format("%04d", value), 10, 100);
}
```
Then _this_ code will actually be compiled and executed:
```java
package your.sketch;

import processing.core.PApplet;

public class Sketch extends PApplet {
  public void setup() {
    stroke(255);
    noFill();
    textSize(72);
  }

  public void draw() {
    background(0);
    fill(255);
    int value = (int)Math.floor(1023 * Math.random());  // Simulation
    text(String.format("%04d", value), 10, 100);
  }

  public void settings() {  size(200, 200); }
  
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "your.sketch.Sketch" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }

}
```
This can be easily reproduced, on any sketch.
In the Processing IDE, go to `File` > `Export Application`, and see for yourself!

> _Note_: Using a class like above allows you to use the features of Processing, along with the Java 8 syntax and goodies (not supported by the Processing IDE).
> See about this the code in the `src` folder, `processing.sample.FullSketch.java`.
> The `libs` folder mentioned in the `build.gradle` contains archives generated by
> a `File` > `Export Application`. 

## PitchRoll.pde

Displays the pitch and roll, returned by an LSM303 (accelerometer) sensor.
<table>
  <tr>
    <td valign="top">
      <img src="./pitch.roll.01.png" title="Processing GUI">
    </td>
    <td valign="top">
      <img src="./pitch.roll.02.png" title="Processing GUI">
    </td>
  </tr>
</table>

## SSD1306 (oled display)

Mirror - synchronously - the display of the SSD1306.

<table>
  <tr>
    <td valign="top">
      <img src="./ssd1306.emulation.png" title="Processing GUI">
    </td>
    <td valign="top">
      <img src="./src/processing/LCD/SSD1306_bb.png" title="Wiring">
    </td>
  </tr>
</table>

The `RST` pin is not necessary. And yes, the `3.3` goes to `Vin`.

## MeArm GUI
[MeArm](https://shop.mime.co.uk/) is a ~$42 (2017) robotic arm, using four micro servos.
- One to move the arm up and down (located at the LEFT of the arm)
- One to move the arm back and forth (located at the RIGHT of the arm)
- One to move the arm left and right (located at the BOTTOM of the arm)
- One to open and close the claw (located on the CLAW)

In this example, each servo is driven by a slide bar displayed on the Processing GUI.
<table>
  <tr>
    <td valign="top">
      <img src="./src/processing/MeArmGUI/MeArm_bb.png" title="MeArm Wiring">
    </td>
    <td valign="top">
      <img src="./src/processing/MeArmGUI/MeArmGUIpde.png" title="Processing GUI">
    </td>
  </tr>
</table>

----------------------------------------------

## Notes
To know what version of Java Processing is using, just write a sketch like
```java
noLoop();
println(String.format("Using Java version %s", System.getProperty("java.version")));
```
and run it. The result is shown in the console:
```
Using Java version 1.8.0_202
```

> Even if you're using Java 8 and above, some limitations seem to exist, regarding
> lambdas and streaming APIs...  
> This worked though, on a laptop running Java 11...
```java
import java.util.*;

void setup() {
}

void draw() {
  noLoop();
  println(String.format("Using Java version %s", System.getProperty("java.version")));
  
  println("-------------");
  List list = List.of("Akeu", "Coucou", "Larigou");
  list.forEach(el -> println(el));
  println("-------------");
  list.forEach(System.out::println);
  println("-------------");
}
```
Console output:
```
Using Java version 11.0.12
-------------
Akeu
Coucou
Larigou
-------------
Akeu
Coucou
Larigou
-------------
```

And more to come.

---
