// Adapted from Feather9x_TX
// -*- mode: C++ -*-
// Example sketch showing how to create a simple messaging client (transmitter)
// with the RH_RF95 class. RH_RF95 class does not provide for addressing or
// reliability, so you should only use RH_RF95 if you do not need the higher
// level messaging abilities.
// It is designed to work with the other example ArduinoRF95_RX

// See http://www.airspayce.com/mikem/arduino/RadioHead/

#include <SPI.h>
#include <RH_RF95.h>

/* for Arduino Uno */
#define RFM95_CS  4
#define RFM95_RST 2
#define RFM95_INT 3

// Change to 434.0 or other frequency, must match RX's freq.
#define RF95_FREQ 915.0

// Singleton instance of the radio driver
RH_RF95 rf95(RFM95_CS, RFM95_INT);

String inputString = "";         // a string to hold incoming data
boolean stringComplete = false;  // whether the string is complete

void setup() {
  pinMode(RFM95_RST, OUTPUT);
  digitalWrite(RFM95_RST, HIGH);

  Serial.begin(115200);
  while (!Serial) {
    delay(1);
  }
  // reserve 256 bytes for the inputString:
  inputString.reserve(256);

  delay(100);

  Serial.println("LORA-0001: Arduino LoRa TX Test.");

  // manual reset
  digitalWrite(RFM95_RST, LOW);
  delay(10);
  digitalWrite(RFM95_RST, HIGH);
  delay(10);

  while (!rf95.init()) {
    Serial.println("LORA-0002: LoRa radio init failed");
    while (1);
  }
  Serial.println("LORA-0003: LoRa radio init OK.");

  // Defaults after init are 434.0MHz, modulation GFSK_Rb250Fd250, +13dbM
  if (!rf95.setFrequency(RF95_FREQ)) {
    Serial.println("LORA-0004: setFrequency failed");
    while (1);
  }
  Serial.print("LORA-0005: Set Freq to: "); Serial.println(RF95_FREQ);
  Serial.println("LORA-0006: Now ready to send messages");

  // Defaults after init are 434.0MHz, 13dBm, Bw = 125 kHz, Cr = 4/5, Sf = 128chips/symbol, CRC on

  // The default transmitter power is 13dBm, using PA_BOOST.
  // If you are using RFM95/96/97/98 modules which uses the PA_BOOST transmitter pin, then
  // you can set transmitter powers from 5 to 23 dBm:
  rf95.setTxPower(23, false);
}

int startsWith(const char *pre, const char *str) {
  return strncmp(pre, str, strlen(pre)) == 0;
}

void loop() {

  if (stringComplete) {
    // Return the string coming from the Serial Port (Raspberry Pi ?)
    String st = inputString;
  // DATA Payload: read from the RaspberryPI, Serial port.
    inputString = "";       // Reset - 1
    stringComplete = false; // Reset - 2

    Serial.println("LORA-0010: Transmitting..."); // Send a message to rf95_server (receiver)

    Serial.print("LORA-0011: Sending {");
    Serial.print(st);
    Serial.print(" (");
    Serial.print(st.length());
    Serial.println(")}");

    delay(10);

//  Serial.print(">> "); Serial.println(st);
    char charBuf[st.length() + 1];
    st.toCharArray(charBuf, st.length());
    rf95.send((uint8_t *)charBuf, st.length());

    Serial.println("LORA-0012: Waiting for packet (send) to complete...");
    delay(10);
    rf95.waitPacketSent();
    // Now wait for a reply
    uint8_t buf[RH_RF95_MAX_MESSAGE_LEN];
    uint8_t len = sizeof(buf);

    Serial.println("LORA-0013: Waiting for reply...");
    if (rf95.waitAvailableTimeout(1000)) { // Wait 1s max.
      // Should be a reply message for us now
      if (rf95.recv(buf, &len)) {
        if (startsWith("LORA-0008", (char*)buf)) {
          Serial.println((char*)buf);
        } else {
          Serial.print("LORA-0014: Got reply: "); Serial.println((char*)buf);
//        Serial.print("RSSI: "); Serial.println(rf95.lastRssi(), DEC);
        }
      } else {
        Serial.println("LORA-0015: Receive failed");
      }
    } else {
      Serial.println("LORA-0016: No reply..., is there a listener around?");
    }
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
    // if the incoming character is a newline, set a flag
    // so the main loop can do something about it:
    if (inChar == '\n') {
      inputString += '\0'; // EOS
      stringComplete = true;
    } else {
      if (inChar != '\r') {
        // add it to the inputString:
        inputString += inChar;
      }
    }
  }
}

