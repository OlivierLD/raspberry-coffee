import processing.serial.*;

Serial serialPort;

void setup() {
  // List available ports
  printArray(Serial.list());
  // Trying /dev/tty.usbserial
  // serialPort = new Serial(this, "/dev/tty.usbserial", 4800);
  
  String portName = "/dev/tty.usbmodem14242401";
  // String portName = "/dev/tty.usbserial";
  serialPort = new Serial(this, portName, 4800);

  
  size(800, 100);
  stroke(255);
  noFill();
  textSize(16);
}

final boolean DEBUG = true;
final char START_CHARACTER = '$';

StringBuffer sb = new StringBuffer();
char previousChar = ' ';

void draw() {
  while (serialPort.available() > 0) { //<>//
    int serialByte = serialPort.read();
    char currentChar = (char)serialByte;
    if (DEBUG) {
      println(String.format("%d 0x%02X %s", serialByte, serialByte, currentChar));
    }
    sb.append(currentChar);
    if (currentChar == START_CHARACTER && DEBUG) {
      println("\tStart of sentence detected");
    }
    
    //println(sb.toString());
    
    if (currentChar == '\n' && previousChar == '\r') {
      String sentence = sb.toString(); //<>//
      if (DEBUG) {
        println("Sentence detected.");
      }
      background(0);
      fill(255);
      text(sentence, 10, 40);
      sb.delete(0, sb.length());
    }
    previousChar = currentChar;
  }
}
