/*
 * Use the LED_BUILTIN, 
 * no resistor needed, no extra led.
 * 
 * Two serial ports.
 * - One for BT
 * - One for the Serial Monitor
 * 
 * Default Serial uses something equivalent to SoftwareSerial(0, 1); 
 * Here, we use SoftwareSerial(0, 1) for the Serial monitor,
 * and SoftwareSerial(2, 3) for Bluetooth.
 */

#include <SoftwareSerial.h>

#define CONSOLE_BR 9600
#define BT_BR      9600
 
#define ledPin LED_BUILTIN

#define rxPin 2
#define txPin 3

#define NULL 0

#define true 1
#define false 0
#define VERBOSE true

String EOS = "\r\n";
String receivedSentence = "";
SoftwareSerial btSerial = SoftwareSerial(rxPin, txPin); 

void setup() {
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);
  btSerial.begin(BT_BR);    // Communication rate of the Bluetooth module
  Serial.begin(CONSOLE_BR); // Serial Monitor

  initMorseAlphabet();
}

void loop() {

  int data = -1;
  while (btSerial.available() > 0) { // Checks whether data is coming from the serial port
    data = btSerial.read();          // Reads the data from the serial port
    receivedSentence.concat((char)data);
  }
  // Received a String
  if (receivedSentence.length() > 0) {
    if (receivedSentence.endsWith(EOS)) {
      receivedSentence = receivedSentence.substring(0, receivedSentence.length() - EOS.length());
    }
    receivedSentence.toUpperCase();
    Serial.print("Translating: ");
    Serial.println(receivedSentence);
    String fullTranslation = "";
    for (int i = 0; i < receivedSentence.length(); i++) {
      String morse = renderCode(receivedSentence.charAt(i));
      fullTranslation.concat(morse);
      delay(100); // between letters
      fullTranslation.concat("/ ");
    }
    btSerial.println(fullTranslation); // Back to client
  }
  receivedSentence = ""; // Reset
  delay(500);

}
