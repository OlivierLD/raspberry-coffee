/*
 * Echoes what it recevies.
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
    // You can use Serial.print(data, HEX); in the Serial console, NOT if another process is expecting the response
    Serial.print(data); // Send back to the client
  }
  if (nb > 0) {
    Serial.println();
  }
}
