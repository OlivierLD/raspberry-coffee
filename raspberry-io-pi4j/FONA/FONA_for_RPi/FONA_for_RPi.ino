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

// Use this for FONA 800 and 808s
Adafruit_FONA fona = Adafruit_FONA(FONA_RST);
// Use this one for FONA 3G
//Adafruit_FONA_3G fona = Adafruit_FONA_3G(FONA_RST);

uint8_t readline(char *buff, uint8_t maxbuff, uint16_t timeout = 0);

void setup() {
  while (!Serial);

  Serial.begin(115200); // Arduino <-> Raspberry
  Serial.println(F("FONA Oliv's test"));
  Serial.println(F("Initializing....(May take 3 seconds)"));

  fonaSerial->begin(4800); // Arduino <-> FONA

  if (! fona.begin(*fonaSerial)) {
    Serial.println(F("Couldn't find FONA"));
    printMenu();
    while (1);
  }
//Serial.println(F("FONA is OK"));

  // Print SIM card IMEI number.
  char imei[15] = {0}; // MUST use a 16 character buffer for IMEI!
  uint8_t imeiLen = fona.getIMEI(imei);
//if (imeiLen > 0) {
//  Serial.print("SIM card IMEI: "); Serial.println(imei);
//}
  Serial.println(">> FONA READY");
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
  // Serial.print(F("FONA> "));
  while (! Serial.available() ) {
    if (fona.available()) {
      Serial.write(fona.read());
    }
  }
  // Command input
  char queryString[128];
  readline(queryString, 128);
  // Parse the received command here, see if it means anything.
  String query = String(queryString);
  String meaning = "";
  if (query.equals("a"))
    readADC();
  else if (query.equals("b"))
    readBattery();
  else if (query.equals("C"))
    readCCID();
  else if (query.equals("i"))
    readRSSI();
  else if (query.equals("n"))
    readNetworkStatus();
  else if (query.equals("N"))
    readNumberOfMessages();
  else if (query.startsWith("d|"))
  {
    String smsn = getElem(query, '|', 1);
    char _1[6];
    smsn.toCharArray(_1, 6);
    int num = atoi(_1);
    deleteMessNum(num);
  }
  else if (query.startsWith("r|"))
  {
    String smsn = getElem(query, '|', 1);
    char _1[6];
    smsn.toCharArray(_1, 6);
    int num = atoi(_1);
    readMessNum(num);
  }
  else if (query.startsWith("s|"))
  {
    String to   = getElem(query, '|', 1);
    String mess = getElem(query, '|', 2);
    sendSMS(to, mess);
  }
  else
    meaning = "Unknown";
  if (meaning.length() > 0)
    Serial.println(meaning);
}

String getElem(String str, char sep, int idx)
{
  String ret = "";

  int start = -1, end = -1;
  int nbSep = 0;
  for (int i=0; i<str.length(); i++)
  {
    if (str.charAt(i) == sep)
    {
      nbSep++;
      if (nbSep == idx && start == -1)
        start = i + 1;
      if (nbSep > idx && end == -1)
      {
        end = i;
        break;
      }
    }
  }
  if (start > -1)
  {
    if (end > -1)
      ret = str.substring(start, end);
    else
      ret = str.substring(start);
  }
  return ret;
}

void readADC()
{
  uint16_t adc;
  if (! fona.getADCVoltage(&adc)) {
    Serial.println(F(">> ADC FAILED"));
  } else {
    Serial.print(">> ADC:");
    Serial.println(adc); // mV
  }
}

void readBattery()
{
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

void readCCID()
{
  fona.getSIMCCID(replybuffer);  // make sure replybuffer is at least 21 bytes!
  Serial.print(">> CCID:");
  Serial.println(replybuffer);
}

void readRSSI()
{
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

void readNetworkStatus()
{
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

void readNumberOfMessages()
{
  int8_t smsnum = fona.getNumSMS();
  if (smsnum < 0) {
    Serial.println(F(">> NUMMESS FAILED"));
  } else {
    Serial.print(">> MESS:");
    Serial.println(smsnum);
  }
}

void readMessNum(int smsn)
{
  // read an SMS
  flushSerial();
  // Retrieve SMS sender address/phone number.
  if (! fona.getSMSSender(smsn, replybuffer, 250)) {
    Serial.println(">> READ MESS FAILED. 1.");
    return;
  }
  Serial.print(">> MESSNUM:"); Serial.print(smsn); Serial.print(F("|FROM:")); Serial.print(replybuffer);

  // Retrieve SMS value.
  uint16_t smslen;
  if (! fona.readSMS(smsn, replybuffer, 250, &smslen)) { // pass in buffer and max len!
    Serial.println(">> READ MESS FAILED. 2.");
    return;
  }
  Serial.print("|"); Serial.print(smslen); Serial.print(F("|"));
  Serial.print(replybuffer);
  Serial.println(F("|"));
}

void deleteMessNum(uint8_t smsn)
{
  // delete an SMS
  flushSerial();
  if (fona.deleteSMS(smsn)) {
    Serial.println(F(">> DEL OK"));
  } else {
    Serial.println(F(">> DEL FAILED"));
  }
}

void sendSMS(String to, String mess)
{
  // send an SMS!
  char sendto[21], message[141];
  flushSerial();
  to.toCharArray(sendto, to.length() + 1);
  mess.toCharArray(message, mess.length() + 1);

//Serial.print(F("Send to #"));
//Serial.println(sendto);
//Serial.println(message);
  if (!fona.sendSMS(sendto, message)) {
    Serial.println(F(">> SEND FAILED"));
  } else {
    Serial.println(F(">> SEND OK"));
  }
}

void flushSerial() {
  while (Serial.available())
    Serial.read();
}

uint8_t readline(char *buff, uint8_t maxbuff, uint16_t timeout) {
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

