/*
 * Arduino is a SLAVE
 *
 * Wiring:
 * RasPi  | Arduino
 * -------+--------
 * GND #9 | GND
 * SDA #3 | SDA (or A4, before Rev3)
 * SLC #5 | SLC (or A5, before Rev3)
 */
#include <Wire.h>

int SLAVE_ADDRESS = 0x04; // Match i2c.comm.Arduino.java

int ledPin    = 13;
int analogPin = A0;

boolean ledOn = false;

void setup() {
  Serial.begin(9600);
  Serial.println("We're in");

  pinMode(ledPin, OUTPUT);
  Wire.begin(SLAVE_ADDRESS);
  Wire.onReceive(processMessage);
  Wire.onRequest(sendAnalogReading);
}

void loop() {
}

void processMessage(int n) {
  Serial.print("Process message "); Serial.println(n);
  char ch = Wire.read();
  Serial.print("Read from Wire:"); Serial.println(ch);
  if (ch == 'l') { // Lowercase L
    Serial.println("Toggling LED");
    toggleLED();
  } else {
    Serial.println("... Doing nothing");
  }
}

void toggleLED() {
  ledOn = ! ledOn;
  digitalWrite(ledPin, ledOn);
}

void sendAnalogReading() {
  int reading = analogRead(analogPin);
  Serial.print("Analog read:"); Serial.println(reading);
  Wire.write(reading >> 2);
}
