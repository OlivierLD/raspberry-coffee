import analogdigitalconverter.mcp.MCPReader;
import com.pi4j.io.gpio.Pin;
import utils.PinUtil;
/*
 * Using Sketch > Add File..., select ADC/build/libs/ADC-1.0-all.jar 
 */
int value;
int ADC_CHANNEL = MCPReader.MCP3008InputChannels.CH0.ch(); // Between 0 and 7, 8 channels on the MCP3008

void setup() {
  size(200, 200); 
  stroke(255);
  noFill();
  textSize(72);
  
  // See MainMCP3008Sample for other default pin options...
  Pin miso = PinUtil.GPIOPin.GPIO_4.pin();
  Pin mosi = PinUtil.GPIOPin.GPIO_5.pin();
  Pin clk  = PinUtil.GPIOPin.GPIO_1.pin();
  Pin cs   = PinUtil.GPIOPin.GPIO_6.pin();
  
  // MCP3008Reader.initMCP3008(miso, mosi, clk, cs);
  MCPReader.initMCP(MCPReader.MCPFlavor.MCP3008, miso, mosi, clk, cs);

}

void draw() { // Draw the value of the ADC (MCP3008) at each repaint
  background(0);
  fill(255);
//value = (int)Math.floor(1023 * Math.random());  // Simulation
  // value = MCP3008Reader.readMCP3008(ADC_CHANNEL); // Real stuff
  value = MCPReader.readMCP(ADC_CHANNEL);

  text(String.format("%04d", value), 10, 100);
}

void dispose() {
  println("Bye!");
  // MCP3008Reader.shutdownMCP3008();
  MCPReader.shutdownMCP();
}
