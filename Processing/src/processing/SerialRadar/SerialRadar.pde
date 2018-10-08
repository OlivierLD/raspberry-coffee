import java.util.function.Consumer;
import java.util.Map;
import processing.serial.*;

/**
 * Reads serial input, from RasPiSerialRadar
 * Change the SERIAL_PORT variable below to match your context.
 *
 * Ascii table at http://www.asciitable.com/
 *
 * The serial read happens in its own thread, see the serialReader variable.
 */
final int NEW_LINE = 10;
final int BAUD_RATE = 9600;
// final String SERIAL_PORT = "/dev/tty.usbserial";
final String SERIAL_PORT = "/dev/cu.wchusbserial1420";
final int MAX_RANGE = 150; // Max range on the display, in cm.

Map<Integer, Double> echos = new HashMap<Integer, Double>(181);
Serial serialPort;

int inc = 1;
int bearing = 0;
double dist = 0;
int hitExtremity = 0;

color bgcolor = color (0, 0, 0);
color gridcolor = color (0, 0, 0);
color sweepercolor = color (102, 250, 81);

boolean keepReadingSerialPort = true;
Thread serialReader = null;

boolean verbose = false;

void setup() {

  println(String.format("Running from [%s]", System.getProperty("user.dir")));

  size(960, 480);
  // List all the available serial ports:
  printArray(Serial.list());

  String portName = SERIAL_PORT; // Serial.list()[0];
  println(String.format("Port is %s", portName));
  serialPort = new Serial(this, portName, BAUD_RATE);
  frameRate(20f); // 20 per second

  // Start serial reader thread
  serialReader = new Thread() {
    public void run() {
      // Expected structure is CSV: direction;range\n
      while (keepReadingSerialPort) {
        if ( serialPort.available() > 0) {  // If data is available,
          StringBuffer sb = new StringBuffer();
          String sentence = null;
          boolean keepReading = true;
          while (keepReading) {
            int character = serialPort.read();
            if (character != -1) {
      //      println(">> Read character #" + character);
              if (character != NEW_LINE) {
                sb.append((char)character);
              } else {
                keepReading = false;
                sentence = sb.toString();
              }
            } else {
              if (true || verbose) { // Enforced
                println(""); // "\t\tWaiting...");
              }
            }
          }
          // Parse and put in echos.
          if (sentence != null) {
            if (verbose) {
              println(String.format("Read %s", sentence));
            }
            String[] data = sentence.split(";");
            if (data.length == 2) {
              try {
                bearing = Integer.parseInt(data[0]);
                dist = Double.parseDouble(data[1]);
                synchronized (echos) {
                  echos.put(bearing, dist);
                }
              } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
              }
            }
          }
        }
        try {
          Thread.sleep(100);
        } catch (InterruptedException ie) {
        }
      }
      println("Done reading serial port");
    }
  };
  serialReader.start();
}

void draw() {
  background(bgcolor);
  grid();
  sweeper();
  circle();
  synchronized (echos) {
    for (Integer key : echos.keySet()) {
      double range = echos.get(key);
      double scale = (height / (double)MAX_RANGE); // full radius: MAX_RANGE
      int x = (int)(Math.round(range * Math.cos(Math.toRadians(key + 90)) * scale));
      int y = (int)(Math.round(range * Math.sin(Math.toRadians(key + 90)) * scale));
      plotEcho((width / 2) + x, height - y);
    }
  }
  textSize(16);
  fill(255);
  text(String.format("%s%02d\272, range %.02f cm", (bearing < 0 ? "-" : "+"), Math.abs(bearing), dist), 10, 20);
}

// Fill the circle
void circle(){
  fill(color(102, 250, 81, 60));
  ellipse(width/2, height, width, 2 * height);
}

void grid(){
  stroke(color(250, 247, 247, 50)); // color(250, 247, 247, 50) = #faf7f7, .5
  strokeWeight(2);
  line(width/2, height, width/2, 0);       // vertical axis
  line(0, height - 1, width, height - 1);  // horizontal axis
  strokeWeight(1);
  noFill();
  for (int i = 1; i <=10; i++) {
    ellipse(width/2, height, i * (width / 10), i * (2 * height / 10));
  }
}

void sweeper(){
  float beam = (float)Math.toRadians(bearing + 90); // map(millis(), 0, 2000, 0, PI);
  strokeWeight(7);
  float f = 0.01;
  for (int i=38; i>=1; i--) {
    stroke(sweepercolor, 2*i);
    line(width/2, height, (width/2 + cos(beam - (f / 2)) * (height * 0.98)), (height - sin(beam - (f / 2)) * (height * 0.98)));
    f += 0.01;
  }
}

void plotEcho(int x, int y){
  ellipse(x, y, 10, 10);
}

void dispose() {
  if (serialReader != null) {
    keepReadingSerialPort = false;
    serialReader.interrupt();
  }
  println("Bye");
}
