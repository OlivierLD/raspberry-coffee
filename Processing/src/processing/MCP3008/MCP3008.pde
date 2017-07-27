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
//value = (int)Math.floor(1023 * Math.random());  // Simulation
  value = MCP3008Reader.readMCP3008(ADC_CHANNEL); // Real stuff
  text(String.format("%04d", value), 10, 100);
}

void exit() {
  println("Bye!");
  MCP3008Reader.shutdownMCP3008();
  super.exit();
}