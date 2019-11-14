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
 * and SoftwareSerial(2, 3) for Bluetooth. Make sure it is wired correctly.
 */

#include <SoftwareSerial.h>

#define CONSOLE_BR 9600
#define BT_BR    115200

#define ledPin LED_BUILTIN
int state = 0; // This is the character code.

#define rxPin 2
#define txPin 3
SoftwareSerial btSerial = SoftwareSerial(rxPin, txPin);

void setup() {
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);
  btSerial.begin(BT_BR); // Communication rate of the Bluetooth module
  Serial.begin(CONSOLE_BR);
}

#define DEBUG 1

void loop() {

  boolean gotSome = false;
  int nb = 0;
  while (btSerial.available() > 0) { // Checks whether data is coming from the serial port
    state = btSerial.read();         // Reads the data from the serial port
    if (DEBUG) {
      gotSome = true;
      if (nb == 0) {
        Serial.print("Received (HEX): ");
      }
      nb += 1;
//      Serial.print("("); Serial.print(nb); Serial.print(") ");
      Serial.print(state, HEX); Serial.print(" ");
    }
  }
  if (DEBUG) {
    if (gotSome) {
      Serial.println();
    }
  }

  if (state == '0') { // 0x30
    digitalWrite(ledPin, LOW); // Turn LED OFF
    btSerial.println("LED: OFF"); // Send back to the client, the String "LED: ON"
    state = 0; // sort of reset
  } else if (state == '1') { // 0x31
    digitalWrite(ledPin, HIGH);
    btSerial.println("LED: ON");
    state = 0; // sort of reset
  } else if (gotSome) {
    if (DEBUG) {
      Serial.println("\t(No 0, no 1)");
    }
    Serial.println("\tTalking back");
    btSerial.println("Tagada");
  }
}
