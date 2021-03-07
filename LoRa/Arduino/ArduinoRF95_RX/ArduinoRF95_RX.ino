// Adapted from Feather9x_RX
// -*- mode: C++ -*-
// Example sketch showing how to create a simple messaging client (receiver)
// with the RH_RF95 class. RH_RF95 class does not provide for addressing or
// reliability, so you should only use RH_RF95 if you do not need the higher
// level messaging abilities.
// It is designed to work with the other example ArduinoRF95_TX

// See http://www.airspayce.com/mikem/arduino/RadioHead/

#include <SPI.h>
#include <RH_RF95.h>

/* for Arduino Uno */
#define RFM95_CS  4
#define RFM95_RST 2
#define RFM95_INT 3

// Change to 434.0 or other frequency, must match TX's freq.
#define RF95_FREQ 915.0

// Singleton instance of the radio driver
RH_RF95 rf95(RFM95_CS, RFM95_INT);

void setup() {
  pinMode(RFM95_RST, OUTPUT);
  digitalWrite(RFM95_RST, HIGH);

  while (!Serial);
  Serial.begin(115200);
  delay(100);

  Serial.println("LORA-0001: Arduino LoRa RX Test, started");

  // manual reset
  digitalWrite(RFM95_RST, LOW);
  delay(10);
  digitalWrite(RFM95_RST, HIGH);
  delay(10);

  while (!rf95.init()) {
    Serial.println("LORA-0002: LoRa radio init failed..");
    // delay(100);
    while (1);
  }
  Serial.println("LORA-0003: LoRa radio init no OK!");

  // Defaults after init are 434.0MHz, modulation GFSK_Rb250Fd250, +13dbM
  if (!rf95.setFrequency(RF95_FREQ)) {
    Serial.println("LORA-0004: setFrequency failed");
    while (1);
  }
  Serial.print("LORA-0005: Set Freq to: "); Serial.println(RF95_FREQ);
  Serial.println("LORA-0006: Now waiting for messages");

  // Defaults after init are 434.0MHz, 13dBm, Bw = 125 kHz, Cr = 4/5, Sf = 128chips/symbol, CRC on

  // The default transmitter power is 13dBm, using PA_BOOST.
  // If you are using RFM95/96/97/98 modules which uses the PA_BOOST transmitter pin, then
  // you can set transmitter powers from 5 to 23 dBm:
  rf95.setTxPower(23, false);
}

void loop() {
  if (rf95.available()) {
    // Should be a message for us now
    uint8_t buf[RH_RF95_MAX_MESSAGE_LEN];
    uint8_t len = sizeof(buf);

    if (rf95.recv(buf, &len)) {
//    RH_RF95::printBuffer("Received: ", buf, len);
      // Data received
      Serial.print("LORA-0007: ("); 
      Serial.print(len);
      Serial.print(") ");
      Serial.println((char*)buf);
//    Serial.print("RSSI: "); Serial.println(rf95.lastRssi(), DEC);

      // Send a reply
      uint8_t data[] = "LORA-0008"; 
      rf95.send(data, sizeof(data));
      rf95.waitPacketSent();
//    Serial.println("...Sent a reply");
    } else {
      Serial.println("LORA-0009: Receive failed");
    }
  }
}

