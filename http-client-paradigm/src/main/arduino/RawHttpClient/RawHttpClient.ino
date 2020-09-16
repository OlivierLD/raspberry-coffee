#include <SPI.h>
#include <WiFi.h>
#include <M5StickC.h>

#include <ArduinoJson.h>

/*
   Make REST requests - It's a REST client
   Basic REST mechanism. NO GUI.
   Output go on the Serial Console.

   Requests are like GET http://19.168.42.6:8080/lis3mdl/cache
   ------------------
   RST Button: top right
   HOME Button: the big one with M5 written on it.

   Methods to look at:
   - getData -> Will invoke the REST endpoint, with resource and verb
   - makeRequest -> Low level plumbing, send the request, and get the response

   Inspired by https://m5stack.hackster.io/Ahork/m5stickc-for-pilot-hue-daf304

   Some doc for M5.lcd at http://forum.m5stack.com/topic/41/lesson-1-1-lcd-graphics

   JSON Objects management: https://github.com/bblanchon/ArduinoJson
*/

// change values below to fit your settings
const char* SSID = "Sonic-00e0_EXT";        // your network SSID (name)
const char* PASSWORD = "67369c7831";        // your network password
const char* SERVER_NAME = "192.168.42.6";   // For REST requests, Server IP

//const char* SSID = "RPi-Gateway";           // your network SSID (name)
//const char* PASSWORD = "raspberrypi";       // your network password
//const char* SERVER_NAME = "192.168.50.10";  // For REST requests, Server IP

// IPAddress server(192, 168, 42, 13);
const int SERVER_PORT = 8080;               // Server port, for REST requests

const boolean DEBUG = true;

int status = WL_IDLE_STATUS;

WiFiClient client;

void setup() {
  M5.begin();
  Serial.begin(9600);

  status = WiFi.begin(SSID, PASSWORD);
  // Wait for connection
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    M5.Lcd.printf(".");
    Serial.print(".");
  }

  M5.Lcd.printf(" Connected to wifi");
  Serial.println("\nConnected to wifi");

  // for the example. Not used.
  pinMode(M5_BUTTON_HOME, INPUT);
  pinMode(M5_BUTTON_RST, INPUT);
}

void loop() {
  if (DEBUG) {
    Serial.println("Getting data...");
  }
  getData();
  if (DEBUG) {
    Serial.println("Got the data!");
  }
  delay(200); // Was 1000
}


void getData() {
  Serial.println("\nMaking request...");
  String cache = makeRequest("GET", "/lis3mdl/cache");
  Serial.println("Request came back:");
  Serial.println("-----------");
  Serial.println(cache);
  Serial.println("-----------");
  if (DEBUG) {
    Serial.println(cache);
  }
  /*
   * The cache/json object looks like: 
   * {
      "x": -21.221864951768488,
      "y": 62.087109032446655,
      "z": -50.570008769365685
     }
   */
  StaticJsonDocument<1024> doc; // Size could be tuned...
  DeserializationError error = deserializeJson(doc, cache);
  if (error) {
    Serial.print(F("deserializeJson() failed: "));
//    Serial.println(error);
    return;
  }

  double x = doc["x"];
  double y = doc["y"];
  double z = doc["z"];
  Serial.print("X:"); Serial.println(x);
  Serial.print("Y:"); Serial.println(y);
  Serial.print("Z:"); Serial.println(z);
}

String makeRequest(String verb, String request) {
  if (DEBUG) {
    Serial.println("\tConnecting...");
  }
  if (client.connect(SERVER_NAME, SERVER_PORT)) {
    if (DEBUG) {
      Serial.println("\tConnected!");
    }
    // Make a HTTP/REST request: Headers and Body need some tweaks, depending on the request.
    String restRequest =
      verb + " " + request + " HTTP/1.1\r\n" +
      "Host: " + SERVER_NAME + ":" + String(SERVER_PORT) + "\r\n" +
      "Connection: close\r\n" +
      // "Accept: text/plain;charset=UTF-8\r\n" +
      "\r\n"; //  + body + "\r\n";
    if (DEBUG) {
      Serial.println(restRequest);
    }
    client.print(restRequest);
  } else {
    Serial.println("Not connected...");
  }

  delay(500);

  String response = "";
  while (client.available()) { // Response?
    if (DEBUG) {
      Serial.println("\tReading response");
    }
    String line = client.readStringUntil('\n');
    if (DEBUG) {
      Serial.print(line); // debug
    }
    response = response + "\n" + line;
  }
  if (DEBUG) {
    Serial.println("\tDone reading response");
  }
  // if the server's disconnected, stop the client:
  if (!client.connected()) {
    if (DEBUG) {
      Serial.println("\tStopping client");
    }
    client.stop();
  }
  if (DEBUG) {
    Serial.println("\tDone making request");
  }

  return response;
}
