/*
 * Echoes what it receives.
 * Duh.
 */

void setup() {
  Serial.begin(9600);
}

void loop() {
  int nb = 0;
  int data = -1;
  while (Serial.available() > 0) { // Checks whether data is coming from the serial port
    nb++;
    data = Serial.read(); // Reads the data from the serial port
    Serial.print(data);   // Send it back to the client
  }
  if (nb > 0) {
    Serial.println(); // EOL
  }
}
