import processing.serial.*;

Serial serialPort;

void setup() {
  // List available ports
  printArray(Serial.list());
  // Trying /dev/tty.usbserial
  String portName = "/dev/tty.usbmodem14242401";
  // String portName = "/dev/tty.usbserial";
  serialPort = new Serial(this, portName, 4800);
}

void draw() {
  while (serialPort.available() > 0) {
    int serialByte = serialPort.read();
    println(serialByte);
  }
}
