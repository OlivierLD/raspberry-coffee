/*
 * Reads a light-resistor (pin A0).
 * See circuit in the book, p63.
 * "Getting started with Arduino"
 * Generates NMEA-like messages, emitted on the Serial port
 * A Raspberry Pi is at the other end of the serial cable.
 */
void setup()
{
  Serial.begin(9600);
}

int val      = 0,
    previous = 0;

const String PREFIX =  "OS"; // Device Prefix
const String ID     = "MSG"; // Sentence ID

void loop()
{
  val = analogRead(A0);
  if (val != previous)
  {
    String payload = "LR," + String(val); // LR: Light Resistor
    String nmea = generateNMEAString(payload, PREFIX, ID);
    Serial.println(nmea);
  }
  previous = val;
  delay(250);
}

int checksum(String s)
{
  int cs = 0;
  int len = s.length() + 1; // Yes, +1
  char ca[len];
  s.toCharArray(ca, len);
  for (int i=0; i<len; i++)
    cs ^= ca[i]; // XOR
  return cs;
}

String generateNMEAString(String payload, String prefix, String id)
{
  String nmea = "";
  if (prefix.length() != 2)
    return nmea; // ("Bad prefix [" + prefix + "], must be 2 character long.");
  if (id.length() != 3)
    return nmea; // ("Bad ID [" + id + "], must be 3 character long.");
  nmea = prefix + id + "," + payload;
  int cs = checksum(nmea);
  String cks = String(cs, HEX);
  cks.toUpperCase();
  if (cks.length() < 2) // lpad '0'
    cks = "0" + cks;
  nmea += ("*" + cks);  // *FF
  return "$" + nmea;    // Prefixed with $
}

