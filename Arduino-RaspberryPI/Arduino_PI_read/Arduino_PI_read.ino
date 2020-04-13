/*
 * Uses the Serial port to communicate with the Raspberry Pi
 * Read from the Serial port
 * Write to the Serial port
 *
 * Send '0' to turn the led off.
 * Anything else will turn it on.
 *
 * One led on pin 13. Grounded with a 10k resistor
 * One photo resistor on pin A0.
 *
 * The led goes on when the photo-resistor goes below 40 (THRESHOLD).
 * It can be overriden by an input from the serial port 0 (off), or 1 (on).
 */
const int LED = 13;
const int THRESHOLD = 40;

void setup()
{
  pinMode(LED, OUTPUT);
  digitalWrite(LED, LOW); // turn it off by default
  Serial.begin(9600);
}

int val = 0,
    previous = 0;
const String PREFIX =  "OS"; // Device Prefix
const String ID     = "MSG"; // Sentence ID

void loop()
{
  if (Serial.available())
  {
    int fromRPI = Serial.read(); // Read one character
    fromRPI -= '0'; // Possibly comes from the key-pad connected to the RPi
    if (fromRPI == 0)
      digitalWrite(LED, LOW);
    else
      digitalWrite(LED, HIGH);
  }

  val = analogRead(A0);

  if (abs(previous - val) > 3)
  {
    String payload = "LR," + String(val); // LR: Light Resistor
    String nmea = generateNMEAString(payload, PREFIX, ID);
    Serial.println(nmea);

    if (val < THRESHOLD)
      digitalWrite(LED, HIGH);
    else
      digitalWrite(LED, LOW);
    previous = val;
  }
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


