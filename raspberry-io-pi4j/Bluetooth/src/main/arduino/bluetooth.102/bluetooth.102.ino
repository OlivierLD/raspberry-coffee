/*
 * Use in standalone through the Serial Console.
 * 
 * Echoes what it receives.
 * Duh.
 */

void setup() {
  Serial.begin(9600);
}

String EOS = "\r\n";
String receivedSentence = "";

void loop() {
  int data = -1;
  while (Serial.available() > 0) { // Checks whether data is coming from the serial port
    data = Serial.read(); // Reads the data from the serial port
    // You can use Serial.print(data, HEX); in the Serial console, NOT if another process is expecting the response
    // Wait for an EOS, like \r\n. Accumulate.
    receivedSentence.concat((char)data);
    if (receivedSentence.endsWith(EOS)) {
      Serial.println(receivedSentence); // Send back to the client
      receivedSentence = ""; // Reset
    }
    delay(10); // Required?
  }
}
