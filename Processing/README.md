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

The wiring looks like this:
![MCP3008 with Pot](../ADC/RPi-MCP3008-Pot_bb.png)

---

And more to come.
