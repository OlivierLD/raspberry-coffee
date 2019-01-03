/**
 * Serial Event example, demonstrating the Serial communication
 * with the Raspberry Pi.
 *
 * Receives a String from the Raspberry Pi (on the Serial port),
 * Returns it reversed (on the Serial port).
 *
 * Both baud rates (Arduino & RPi0 have to be the same.
 *
 * For dev, from the Arduino Console: use "Both NL & CR"
 */

String inputString = "";         // a string to hold incoming data
boolean stringComplete = false;  // whether the string is complete

void setup() {
  // initialize serial:
  Serial.begin(115200);
  // reserve 256 bytes for the inputString:
  inputString.reserve(256);
  Serial.println("Arduino is ready");
}

void loop() {
  // print the string when a newline arrives:
  if (stringComplete) {
    // Return the string, reversed.
    String st = inputString;
    /* Reverse String Logic */
    int i = 0;
    int l = st.length();
    char ch;
    for (i=0; i<l/2; i++) {
      ch = st[i];
      st[i] = st[l-1-i];
      st[l-1-i] = ch;
    }

    Serial.println(st);
    // clear the string:
    inputString = "";
    stringComplete = false;
  } else {
    delay(1);
  }
}

/*
 * SerialEvent occurs whenever a new data comes in the
 * hardware serial RX.  This routine is run between each
 * time loop() runs, so using delay inside loop can delay
 * response.  Multiple bytes of data may be available.
 */
void serialEvent() {
  while (Serial.available()) {
    // get the new byte:
    char inChar = (char)Serial.read();
    // add it to the inputString:
    inputString += inChar;
    // if the incoming character is a newline, set a flag
    // so the main loop can do something about it:
    if (inChar == '\n') {
      stringComplete = true;
    }
  }
}


