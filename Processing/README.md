# Processing.org

[Processing](http://processing.org) is a framework that considerably simplifies Java development.

It seems to be compatible with the projects of this repository.

For example, here is the code that graphically displays the value returned by an ADC (MCP3008):
```java
import analogdigitalconverter.mcp3008.MCP3008Reader;
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
About 20 lines...

### To run the Sketch
- From the project root (Processing), to produce the required jar files, run
```bash
 $> ./gradlew clean shadowJar
```
- Open the sketch [MCP3008_Pie.pde](./src/processing/MCP3008_Pie/MCP3008_Pie.pde) in `Processing` (no need to copy it anywhere else)

  ![Open in Processing](./sketch.png)

- If instructed in the comment at the top of the sketch, use the menu `Sketch > Add File...` to select the appropriate archive, like here:
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

And more to come.
