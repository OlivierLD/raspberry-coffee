import analogdigitalconverter.mcp.MCPReader;
import com.pi4j.io.gpio.Pin;
import utils.PinUtil;
/*
 * Using Sketch > Add File..., select ADC/build/libs/ADC-1.0-all.jar 
 */

boolean SIMULATION = false; // Set to true if not on a Raspberry Pi.
int incSign = 1;
int value = 0;
int ADC_CHANNEL = MCPReader.MCP3008InputChannels.CH0.ch(); // Between 0 and 7, 8 channels on the MCP3008

void setup() {
  println(String.format("Java version: %s", System.getProperty("java.version")));
  size(400, 400); 
  stroke(255);
  noFill();
  textSize(72);  
  if (!SIMULATION) {
    // See MainMCP3008Sample for other default pin options...
    Pin miso = PinUtil.GPIOPin.GPIO_4.pin();
    Pin mosi = PinUtil.GPIOPin.GPIO_5.pin();
    Pin clk  = PinUtil.GPIOPin.GPIO_1.pin();
    Pin cs   = PinUtil.GPIOPin.GPIO_6.pin();

    // Default PINs, for diozero
    //Pin miso = PinUtil.GPIOPin.GPIO_13.pin();
    //Pin mosi = PinUtil.GPIOPin.GPIO_12.pin();
    //Pin clk  = PinUtil.GPIOPin.GPIO_14.pin();
    ////Pin cs   = PinUtil.GPIOPin.GPIO_11.pin(); // Not good for RPi 4... use --cs:16 (16: BCM => wPi 27)
    //Pin cs   = PinUtil.GPIOPin.GPIO_16.pin(); // Not good for RPi 4... use --cs:16 (16: BCM => wPi 27)

    // MCP3008Reader.initMCP3008(miso, mosi, clk, cs);
    MCPReader.initMCP(MCPReader.MCPFlavor.MCP3008, miso, mosi, clk, cs);
  }
}

void draw() { // Draw the value of the ADC (MCP3008) at each repaint
  background(0);
  
  if (SIMULATION) {
//  value = (int)Math.floor(1023 * Math.random());  // Simulation
    value += (1 * incSign);
    if (value > 1023) {
      value = 1023;
      incSign = -1;
    } else if (value < 0) {
      value = 0;
      incSign = 1;
    }
  } else {
    // value = MCP3008Reader.readMCP3008(ADC_CHANNEL); // Real stuff
    value = MCPReader.readMCP(ADC_CHANNEL);
  }
  fill(128);
  arc(width/2, height/2, 200, 200, (float)-Math.PI/2, (float)(-Math.PI/2) + radians(360 * value / 1023));
  fill(255);
  text(String.format("%04d", value), 10, 75);
}

void dispose() {
  println("Bye!");
  if (!SIMULATION) {
    // MCP3008Reader.shutdownMCP3008();
    MCPReader.shutdownMCP();
  }
}
