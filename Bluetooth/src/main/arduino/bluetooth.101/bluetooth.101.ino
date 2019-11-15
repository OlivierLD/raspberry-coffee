/*
 * Use the LED_BUILTIN, 
 * no resistor needed, no extra led.
 * 
 * Expects to receive '0'or '1', as characters.
 */
 
#define ledPin LED_BUILTIN
int state = 0; // This is the character code.

void setup() {
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);
  Serial.begin(9600); // 38400); // Default communication rate of the Bluetooth module
}

void loop() {
  if (Serial.available() > 0) { // Checks whether data is comming from the serial port
    state = Serial.read(); // Reads the data from the serial port
  }

  if (state == '0') {
    digitalWrite(ledPin, LOW); // Turn LED OFF
    Serial.println("LED: OFF"); // Send back to the client, the String "LED: ON"
    state = 0;
  } else if (state == '1') {
    digitalWrite(ledPin, HIGH);
    Serial.println("LED: ON");
    state = 0;
  }
}
