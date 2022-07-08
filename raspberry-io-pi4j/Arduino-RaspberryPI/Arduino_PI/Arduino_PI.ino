/*
 * Serial Read and Write.
 *
 * Read data from the RPi (0, or 1) from the Serial port
 * Generates NMEA-like messages, emitted on the Serial port
 */
const String prefix = "OS";
const String id     = "MSG";
const int LED       = 13;

void setup() {
  pinMode(LED, OUTPUT);
  digitalWrite(LED, LOW);
  Serial.begin(9600);
}

int idx = 0;

void loop() {
  idx++;
  if (idx > 0xFFFF) {
    idx = 0;
  }
  String payload = String(idx) + ", Message from Arduino";
  String nmea = generateNMEAString(payload, prefix, id);
  Serial.println(nmea);  // Send message to the Raspberry
  
  if (Serial.available()) { // Raspberry sent a message, read it
    int fromRPI = Serial.read();
    fromRPI -= '0';
    Serial.print("Read:"); Serial.println(fromRPI);
    if (fromRPI == 0)
      digitalWrite(LED, LOW);
    else
      digitalWrite(LED, HIGH);
  }
  
  delay(500);
}

int checksum(String s) {
  int cs = 0;
  int len = s.length() + 1; // Yes, +1
  char ca[len];
  s.toCharArray(ca, len);
//cs = ca[0];
//String mess = "\tCS[0]:" + String(cs, HEX);
//Serial.println(mess);
  for (int i=0; i<len; i++) {
    cs ^= ca[i]; // XOR
//  mess = "\tCS[" + String(i) + "] (" + ca[i] + "):" + String(cs, HEX);
//  Serial.println(mess);
  }
  return cs;
}

String generateNMEAString(String payload, String prefix, String id) {
  String nmea = "";
  if (prefix.length() != 2) {
    return nmea; // ("Bad prefix [" + prefix + "], must be 2 character long.");
  }
  if (id.length() != 3) {
    return nmea; // ("Bad ID [" + id + "], must be 3 character long.");
  }
  nmea = prefix + id + "," + payload;
  int cs = checksum(nmea);
  String cks = String(cs, HEX);
  cks.toUpperCase();
  if (cks.length() < 2) {
    cks = "0" + cks;
  }
  nmea += ("*" + cks);
  return "$" + nmea;
}

