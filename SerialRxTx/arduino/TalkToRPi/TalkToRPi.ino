/*
 * Serial Read and Write.
 */

void setup() {
  Serial.begin(115200);
  Serial.println("Ready");
}

int count = 0;
boolean verbose = false;

void loop() {

  if (Serial.available()) { // Raspberry sent a message, read it
    int fromRPI = Serial.read();
    fromRPI -= '0';
    Serial.print("Read:"); Serial.println(fromRPI);
    if (fromRPI == 0) {
      Serial.println("Zero");
    } else {
      Serial.println("Not Zero");
    }
  }
  if (verbose) {
    if (count == 10000) {
      Serial.println("Loop"); 
      count = 0;
    }
    count++;
  }
}

