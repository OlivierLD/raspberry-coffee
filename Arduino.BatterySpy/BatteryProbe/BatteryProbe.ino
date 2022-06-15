/***************************************************
 Adapted from Adafruit's FONATest.ino
 Uses Adafruit_FONA.h (see the #defines in there)
  and Adafruit_FONA.cpp

 Communicates through structured sentences.
 Suitable for the Raspberry Pi.

 Commands are:

  a   read the ADC 2.8V max (FONA800 & 808)
  b   read the Battery V and % charged
  C   read the SIM CCID
  i   read RSSI
  n   read network status

  N   Number of SMSs
  r|x Read SMS # x
  d|x Delete SMS # x
  s|<dest number>|<mess payload> Send SMS  to <dest number>

 ****************************************************/

#include "Adafruit_FONA.h"

#define FONA_RX  2
#define FONA_TX  3
#define FONA_RST 4

// this is a large buffer for replies
char replybuffer[255];

// This is to handle the absence of software serial on platforms
// like the Arduino Due. Modify this code if you are using different
// hardware serial port, or if you are using a non-avr platform
// that supports software serial.
#ifdef __AVR__
#include <SoftwareSerial.h>
SoftwareSerial fonaSS = SoftwareSerial(FONA_TX, FONA_RX);
SoftwareSerial *fonaSerial = &fonaSS;
#else
HardwareSerial *fonaSerial = &Serial1;
#endif

const int ANALOG_INPUT = A0;    // Battery Probe
const float DATA_TO_VOLTS = 0.0203389830508474576271186440678;

float voltage = 0.0; // The one to monitor

// Use this for FONA 800 and 808s
Adafruit_FONA fona = Adafruit_FONA(FONA_RST);
// Use this one for FONA 3G
//Adafruit_FONA_3G fona = Adafruit_FONA_3G(FONA_RST);

uint8_t readSerialLine(char *buff, uint8_t maxbuff, uint16_t timeout = 0);
uint8_t readFonaLine(char *buff, uint8_t maxbuff, uint16_t timeout = 0);

bool fonaOK = false;

void setup() {

  pinMode(ANALOG_INPUT, INPUT);

  while (!Serial);

  Serial.begin(9600); // Console
  Serial.println(F("Battery Probe, reachable by SMS"));
  Serial.println(F("By OlivSoft"));
  Serial.println(F("--------------------------------"));
  Serial.println(F("Initializing....(May take ~3 seconds)"));

  fonaSerial->begin(4800); // Arduino <-> FONA

  if (! fona.begin(*fonaSerial)) {
    Serial.println(F("Couldn't find FONA"));
//  printMenu();
//  while (1);
  } else {
    fonaOK = true;
  //Serial.println(F("FONA is OK"));
  }

  // Print SIM card IMEI number.
  char imei[15] = {0}; // MUST use a 16 character buffer for IMEI!
  uint8_t imeiLen = fonaOK ? fona.getIMEI(imei) : 0;
//if (imeiLen > 0) {
//  Serial.print("SIM card IMEI: "); Serial.println(imei);
//}
  if (fonaOK) {
    Serial.println(">> FONA READY");
  }
//printMenu(); // Optional
}

void printMenu(void) {
  Serial.println(F("-------------------------------------"));
  Serial.println(F("[?] Print this menu"));
  Serial.println(F("[a] read the ADC 2.8V max (FONA800 & 808)"));
  Serial.println(F("[b] read the Battery V and % charged"));
  Serial.println(F("[C] read the SIM CCID"));
  Serial.println(F("[i] read RSSI"));
  Serial.println(F("[n] read network status"));
  // SMS
  Serial.println(F("[N] Number of SMSs"));
  Serial.println(F("[r] Read SMS #"));
  Serial.println(F("[d] Delete SMS #"));
  Serial.println(F("[s] Send SMS"));
  Serial.println(F("-------------------------------------"));
  Serial.println(F(""));
}

void loop() {

  int val = analogRead(ANALOG_INPUT);
  voltage = val * DATA_TO_VOLTS;

//Serial.print("Voltage:"); Serial.print(voltage); Serial.println(" V");

  char smsQueryString[128];
  for (int i=0; i<128; i++) {
    smsQueryString[i] = '\0';
  }
  // Serial.print(F("FONA> "));
  String command = "";
  if (fonaOK && fona.available()) {
    // Command input
    readFonaLine(smsQueryString, 128); // From Fona
    Serial.print("Received:"); Serial.println(smsQueryString);
    if (strlen(smsQueryString) > 0) {
      String fonaQ = String(smsQueryString);
      if (fonaQ.startsWith("+CMTI:")) {
        int idx = fonaQ.indexOf(",") + 1;
        String messNum = fonaQ.substring(idx);
        command = "r|" + messNum; // Will tell the 'appLogic' to read the message, and process it.
        Serial.print("Will execute: "); Serial.println(command);
      }
    }
  }
//Serial.print("Query len:"); Serial.println(strlen(smsQueryString));
  // For debugging
  if (Serial.available()) {
    readSerialLine(smsQueryString, 128); // From Serial port
    Serial.print("Received from Serial:"); Serial.println(smsQueryString);
    command = String(smsQueryString);
  }

  if (command.length() > 0) {
    appLogic(command);
  } else {
    delay(500);
  }
}

void appLogic(String command) {
  Serial.println("-- App Logic ---");
  Serial.println(command);
  Serial.println("----------------");
  // Parse the received command here, see if it means anything.
  String query = command; // String(smsQueryString);
  String QUERY = String(query);
  QUERY.toUpperCase();
  String meaning = "";
  if (query.equals("a")) {
    readADC();
  } else if (query.equals("b")) {
    readBattery();
  } else if (query.equals("C")) {
    readCCID();
  } else if (query.equals("i")) {
    readRSSI();
  } else if (query.equals("n")) {
    readNetworkStatus();
  } else if (query.equals("N")) {
    readNumberOfMessages();
  } else if (query.startsWith("d|")) {
    String smsn = getElem(query, '|', 1);
    char _1[6];
    smsn.toCharArray(_1, 6);
    int num = atoi(_1);
    deleteMessNum(num);
  } else if (query.startsWith("r|")) {
    String smsn = getElem(query, '|', 1);
    char _1[6];
    smsn.toCharArray(_1, 6);
    int num = atoi(_1);
    readMessNum(num);
  } else if (query.startsWith("s|")) {
    String to   = getElem(query, '|', 1);
    String mess = getElem(query, '|', 2);
    Serial.print("Sending ["); Serial.print(mess); Serial.print("] to ["); Serial.print(to); Serial.println("]");
    if (!to.equals("FAKE")) {
      sendSMS(to, mess);
    }
  } else if (query.startsWith("?|")) {
    String to   = getElem(query, '|', 1);
    String menu = "Menu: ? for Menu, b for battery";
    Serial.print("Sending ["); Serial.print(menu); Serial.print("] to ["); Serial.print(to); Serial.println("]");
    if (!to.equals("FAKE")) {
      sendSMS(to, menu);
    }
  } else if (QUERY.startsWith("BAT|")) { // TODO QUERY - Uppercase
    String to = getElem(query, '|', 1);
    Serial.print("Will (BAT) reply to "); Serial.println(to);
    String payload = String(voltage, 2) + " V";
    Serial.print("Sending ["); Serial.print(payload); Serial.print("] to ["); Serial.print(to); Serial.println("]");
    if (!to.equals("FAKE")) {
      sendSMS(to, payload);
    }
    Serial.println("Bat, done.");
  } else {
    if (query.length() > 0) {
      meaning = query + ":Unknown";
    }
  }
  if (meaning.length() > 0) {
    Serial.println(meaning);
  }
}

String getElem(String str, char sep, int idx) {
  String ret = "";

  int start = -1, end = -1;
  int nbSep = 0;
  for (int i=0; i<str.length(); i++) {
    if (str.charAt(i) == sep) {
      nbSep++;
      if (nbSep == idx && start == -1) {
        start = i + 1;
      }
      if (nbSep > idx && end == -1) {
        end = i;
        break;
      }
    }
  }
  if (start > -1) {
    if (end > -1) {
      ret = str.substring(start, end);
    } else {
      ret = str.substring(start);
    }
  }
  return ret;
}

void readADC() {
  uint16_t adc;
  if (! fona.getADCVoltage(&adc)) {
    Serial.println(F(">> ADC FAILED"));
  } else {
    Serial.print(">> ADC:");
    Serial.println(adc); // mV
  }
}

void readBattery() {
  uint16_t vbat, vpercent;
  if (! fona.getBattVoltage(&vbat)) {
    Serial.println(F(">> BATTERY READ FAILED"));
  } /* else {
    Serial.print(F("VBat = ")); Serial.print(vbat); Serial.println(F(" mV"));
  } */


  if (! fona.getBattPercent(&vpercent)) {
    Serial.println(F(">> BATTERY READ FAILED"));
  } /* else {
    Serial.print(F("VPct = ")); Serial.print(vpercent); Serial.println(F("%"));
  } */
  String payload = String(vbat);
  payload.concat(",");
  payload.concat(String(vpercent));
  Serial.print(">> BAT:"); Serial.println(payload); // mV, %
}

void readCCID() {
  fona.getSIMCCID(replybuffer);  // make sure replybuffer is at least 21 bytes!
  Serial.print(">> CCID:");
  Serial.println(replybuffer);
}

void readRSSI() {
  uint8_t n = fona.getRSSI();
  int8_t r;

  if (n == 0) r = -115;
  if (n == 1) r = -111;
  if (n == 31) r = -52;
  if ((n >= 2) && (n <= 30)) {
    r = map(n, 2, 30, -110, -54);
  }
  Serial.print(">> RSSI:"); Serial.print(n); Serial.print(","); Serial.println(r); // level,  dBm
}

void readNetworkStatus() {
  // read the network/cellular status
  uint8_t n = fona.getNetworkStatus();
  Serial.print(">> NETW:"); Serial.print(n);
  Serial.print(F(","));
  if (n == 0) Serial.println(F("Not registered"));
  if (n == 1) Serial.println(F("Registered (home)"));
  if (n == 2) Serial.println(F("Not registered (searching)"));
  if (n == 3) Serial.println(F("Denied"));
  if (n == 4) Serial.println(F("Unknown"));
  if (n == 5) Serial.println(F("Registered roaming"));
}

void readNumberOfMessages() {
  int8_t smsnum = fona.getNumSMS();
  if (smsnum < 0) {
    Serial.println(F(">> NUMMESS FAILED"));
  } else {
    Serial.print(">> MESS:");
    Serial.println(smsnum);
  }
}

void readMessNum(int smsn) {
  // read an SMS
  flushSerial();
  // Retrieve SMS sender address/phone number.
  if (! fona.getSMSSender(smsn, replybuffer, 250)) {
    Serial.println(">> READ MESS FAILED. 1.");
    return;
  }
  Serial.print(">> MESSNUM:"); Serial.print(smsn); Serial.print(F("|FROM:")); Serial.print(replybuffer);
  String sender = replybuffer;

  // Retrieve SMS value.
  uint16_t smslen;
  if (! fona.readSMS(smsn, replybuffer, 250, &smslen)) { // pass in buffer and max len!
    Serial.println(">> READ MESS FAILED. 2.");
    return;
  }
  String content = replybuffer;
  Serial.print("|"); Serial.print(smslen); Serial.print(F("|"));
  Serial.print(replybuffer);
  Serial.println(F("|"));

  String command = content + "|" + sender;
  Serial.print("After reading, sending command to process: ["); Serial.print(command); Serial.println("]");

  appLogic(command);
}

void deleteMessNum(uint8_t smsn) {
  // delete an SMS
  flushSerial();
  if (fona.deleteSMS(smsn)) {
    Serial.println(F(">> DEL OK"));
  } else {
    Serial.println(F(">> DEL FAILED"));
  }
}

void sendSMS(String to, String mess) {
  // send an SMS!
  char sendto[21], message[141];
  flushSerial();
  to.toCharArray(sendto, to.length() + 1);
  mess.toCharArray(message, mess.length() + 1);

  Serial.print(F("Send to #")); Serial.print(to); Serial.print(", "); Serial.println(mess);

  if (fonaOK) {
    if (!fona.sendSMS(sendto, message)) {
      Serial.println(F(">> SEND FAILED"));
    } else {
      Serial.println(F(">> SEND OK"));
    }
  }
}

void flushSerial() {
  while (Serial.available()) {
    Serial.read();
  }
}

/**
 * Read line from Serial channel.
 */
uint8_t readSerialLine(char *buff, uint8_t maxbuff, uint16_t timeout) {
  uint16_t buffidx = 0;
  boolean timeoutvalid = true;
  if (timeout == 0) timeoutvalid = false;

  while (true) {
    if (buffidx > maxbuff) {
      //Serial.println(F("SPACE"));
      break;
    }

    while (Serial.available()) {
      char c =  Serial.read();

      //Serial.print(c, HEX); Serial.print("#"); Serial.println(c);

      if (c == '\r') continue;
      if (c == 0xA) {
        if (buffidx == 0)   // the first 0x0A is ignored
          continue;

        timeout = 0;         // the second 0x0A is the end of the line
        timeoutvalid = true;
        break;
      }
      buff[buffidx] = c;
      buffidx++;
    }

    if (timeoutvalid && timeout == 0) {
      //Serial.println(F("TIMEOUT"));
      break;
    }
    delay(1);
  }
  buff[buffidx] = 0;  // null term
  return buffidx;
}

uint8_t readFonaLine(char *buff, uint8_t maxbuff, uint16_t timeout) {
  uint16_t buffidx = 0;
  boolean timeoutvalid = true;
  if (timeout == 0) timeoutvalid = false;

  while (true) {
    if (buffidx > maxbuff) {
      //Serial.println(F("SPACE"));
      break;
    }

    while (fona.available()) {
      char c =  fona.read();

      //Serial.print(c, HEX); Serial.print("#"); Serial.println(c);

      if (c == '\r') continue;
      if (c == 0xA) {
        if (buffidx == 0)   // the first 0x0A is ignored
          continue;

        timeout = 0;         // the second 0x0A is the end of the line
        timeoutvalid = true;
        break;
      }
      buff[buffidx] = c;
      buffidx++;
    }

    if (timeoutvalid && timeout == 0) {
      //Serial.println(F("TIMEOUT"));
      break;
    }
    delay(1);
  }
  buff[buffidx] = 0;  // null term
  return buffidx;
}

