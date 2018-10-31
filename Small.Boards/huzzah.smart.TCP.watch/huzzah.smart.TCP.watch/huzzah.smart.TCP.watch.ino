#include <Wire.h>
#include <Ticker.h>
// Need ArduinoJson, see https://arduinojson.org/
#include <ArduinoJson.h>

#include <ESP8266WiFi.h>

#include "ssd1306_i2c.h"
#include "icons.h"

#define SDA 14
#define SCL 12
//#define RST 2

#define I2C 0x3D

const char* ssid = "Sonic-00e0"; // "RPi-Net"
const char* password = "67369c7831"; // "raspberrypi"

const char* MULTIPLEXER_SERVER_NAME = "192.168.42.4";
const char* REST_RESOURCE = "/mux/cache?option=tiny";

// Initialize the oled display for address 0x3c
// 0x3D is the adafruit address....
// sda-pin=14 and sdc-pin=12
//SSD1306 display(I2C, SDA, SCL);
Ticker ticker; // Invokes a method at a given interval

void drawFrame1(int x, int y) {
//display.setFontScale2x2(false);
//display.drawString(65 + x, 8 + y, "Now");
//display.drawXbm(x + 7, y + 7, 50, 50, getIconFromString(weather.getCurrentIcon()));
//display.setFontScale2x2(true);
//display.drawString(64 + x, 20 + y, String(weather.getCurrentTemp()) + "F");
//display.setFontScale2x2(false);
//display.drawString(64 + x, 40 + y, String(weather.getCurrentSummary()));
}

void drawFrame2(int x, int y) {
//display.setFontScale2x2(false);
//display.drawString(65 + x, 0 + y, "Today");
//display.drawXbm(x, y, 60, 60, xbmtemp);
//display.setFontScale2x2(true);
//display.drawString(64 + x, 14 + y, String(weather.getCurrentTemp()) + "F");
//display.setFontScale2x2(false);
//display.drawString(66 + x, 40 + y, String(weather.getMinTempToday()) + "F/" + String(weather.getMaxTempToday()) + "F");
}

void drawFrame3(int x, int y) {
//display.drawXbm(x + 7, y + 7, 50, 50, getIconFromString(weather.getIconTomorrow()));
//display.setFontScale2x2(false);
//display.drawString(65 + x, 7 + y, "Tomorrow");
//display.setFontScale2x2(true);
//display.drawString(64 + x, 20 + y, String(weather.getMaxTempTomorrow()) + "F");
}

// this array keeps function pointers to all frames
// frames are the single views that slide from right to left
void (*frameCallbacks[3])(int x, int y) = {drawFrame1, drawFrame2, drawFrame3};

// how many frames are there?
int frameCount = 3;
// on frame is currently displayed
int currentFrame = 0;

// flag changed in the ticker function every 10 minutes
bool readyForUpdate = true;

void setup() {
  delay(500);
  //ESP.wdtDisable();

  // initialize display
//display.init();
//display.flipScreenVertically();
  // set the drawing functions
//display.setFrameCallbacks(3, frameCallbacks);
  // how many ticks does a slide of frame take?
//display.setFrameTransitionTicks(10);

//display.clear();
//display.display();

  Serial.begin(115200);
  delay(500);

  Serial.println();
  Serial.println();
  // We start by connecting to a WiFi network
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);

  int counter = 0;
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");

//  display.clear();
//  display.drawXbm(34, 10, 60, 36, WiFi_Logo_bits);
//  display.setColor(INVERSE);
//  display.fillRect(10, 10, 108, 44);
//  display.setColor(WHITE);
//  drawSpinner(3, counter % 3);
//  display.display();

    counter++;
  }
  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.print("Netmask: ");
  Serial.println(WiFi.subnetMask());
  Serial.print("Gateway: ");
  Serial.println(WiFi.gatewayIP());

  // update the weather information every 10 mintues only
  // forecast.io only allows 1000 calls per day
  ticker.attach(60 * 10, setReadyForUpdate);
  //ESP.wdtEnable();
}

void loop() {
  if (readyForUpdate /*&& display.getFrameState() == display.FRAME_STATE_FIX*/) {
    readyForUpdate = false;

    Serial.print("connecting to ");
    Serial.println(MULTIPLEXER_SERVER_NAME);

    // Use WiFiClient class to create TCP connections
    WiFiClient client;
    const int httpPort = 80;
    if (!client.connect(MULTIPLEXER_SERVER_NAME, httpPort)) {
      Serial.println("connection failed");
      return;
    }

    // We now create a URI for the request
    String url = REST_RESOURCE;
    Serial.print("Requesting URL: ");
    Serial.println(url);

    // This will send the request to the server
    // TODO Make it more generic, like a REST request
    //  client.print(String("GET ") + url + " HTTP/1.1\r\n" +
    //               "Host: " + host + "\r\n" +
    //               "Connection: close\r\n\r\n");
    sendRequest(client, "GET", url, "HTTP/1.1", MULTIPLEXER_SERVER_NAME);
    delay(500);

    // Read all the lines of the reply from server and print them to Serial
    while (client.available()) {
      String line = client.readStringUntil('\r');
      Serial.print(line); // Output here
    }

    Serial.println();
    Serial.println("closing connection");

  }
//display.clear();
//display.nextFrameTick();
//display.display();
}

void setReadyForUpdate() {
  readyForUpdate = true;
}

const char* getIconFromString(String icon) {
  //"clear-day, clear-night, rain, snow, sleet, wind, fog, cloudy, partly-cloudy-day, or partly-cloudy-night"
  if (icon == "clear-day") {
    return clear_day_bits;
  } else if (icon == "clear-night") {
    return clear_night_bits;
  } else if (icon == "rain") {
    return rain_bits;
  } else if (icon == "snow") {
    return snow_bits;
  } else if (icon == "sleet") {
    return sleet_bits;
  } else if (icon == "wind") {
    return wind_bits;
  } else if (icon == "fog") {
    return fog_bits;
  } else if (icon == "cloudy") {
    return cloudy_bits;
  } else if (icon == "partly-cloudy-day") {
    return partly_cloudy_day_bits;
  } else if (icon == "partly-cloudy-night") {
    return partly_cloudy_night_bits;
  }
  return cloudy_bits;
}

void drawSpinner(int count, int active) {
  for (int i = 0; i < count; i++) {
    const char *xbm;
    if (active == i) {
      xbm = active_bits;
    } else {
      xbm = inactive_bits;
    }
//  display.drawXbm(64 - (12 * count / 2) + 12 * i, 56, 8, 8, xbm);
  }
}

void sendRequest(WiFiClient client, String verb, String url, String protocol, String host) {
  String request = verb + " " + url + " " + protocol + "\r\n" +
                   "Host: " + host + "\r\n" +
                   "Connection: close\r\n" +
                   "\r\n";
  client.print(request);
}


