/*
 * Sample.
 * For I2C communication with the Raspberry Pi.
 * Arduino is a SLAVE
 *
 * Wiring:
 * RasPi    Arduino
 * ----------------
 * GND #9   GND
 * SDA #3   SDA (or A4, before Rev3)
 * SLC #5   SLC (or A5, before Rev3)
 */
#include <Wire.h>

int SLAVE_ADDRESS = 0x04;

int END_OF_MESSAGE = 0x00;
int STRING_REQUEST = 0x10; // Master wants a String message
int STRING_RECEIVE = 0x11; // Master will send a String message

int PING           = 0x20;
int PONG           = 0x21;

char * FROM_ARDUINO = "From Arduino";
int strIdx = 0;
char fromMaster[128];

int ONE_BYTE = 1;
int ONE_STRING = 2;

int replyType = 0;
int readingExpected = 0;
boolean incomingStringMessage = false;

void setup() {
  Serial.begin(9600);
  Serial.println("We're in");

  Wire.begin(SLAVE_ADDRESS);
  Wire.onReceive(processMessage); // Received from master
  Wire.onRequest(sendReply);      // Master is reading
}

void loop() {
}

void processMessage(int n) {
  Serial.print("Process message "); Serial.println(n);
  char ch = Wire.read();
  Serial.print("Read from Wire:"); Serial.println(ch);
  if (incomingStringMessage) {
    if (ch != END_OF_MESSAGE) {
      fromMaster[strIdx++] = ch;
    } else {
      fromMaster[strIdx] = '\0';
      incomingStringMessage = false;
      Serial.print("Received from Master: [");Serial.print(fromMaster);Serial.println("]");
    }
  } else if (ch == PING) {
    Serial.print("Received a ping, sending a pong [");Serial.print(PONG);Serial.println("]");
    replyType = ONE_BYTE;
    readingExpected = PONG;
  } else if (ch == STRING_REQUEST) {
    Serial.println("Received a String Request.");
    replyType = ONE_STRING;
    strIdx = 0;
    readingExpected = FROM_ARDUINO[strIdx++];
  } else if (ch == STRING_RECEIVE) {
    strIdx = 0;
    incomingStringMessage = true;
  } else {
    Serial.println("... Doing nothing");
  }
}

void sendReply() {
  int reading = readingExpected;
  Serial.print("Replying:"); Serial.println(reading);
  Wire.write(reading);
  if (replyType == ONE_STRING) {
    if (strIdx > strlen(FROM_ARDUINO))
      readingExpected = END_OF_MESSAGE;
    else
      readingExpected = FROM_ARDUINO[strIdx++];
  }
}
