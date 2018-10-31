/*
   Simple HTTP get webclient REST test
   for Huzzah/ESP8266
*/

#include <ESP8266WiFi.h>
// Need ArduinoJson, see https://arduinojson.org/
#include <ArduinoJson.h>

// Network and Host definitions

const char* ssid     = "Sonic-00e0";
const char* password = "67369c7831";

const char* host = "192.168.42.4";
const int httpPort = 9998;

StaticJsonBuffer<1024> jsonBuffer;

void setup() {
  Serial.begin(115200); // Console output
  delay(100);

  // Start by connecting to a WiFi network
  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.print("Netmask: ");
  Serial.println(WiFi.subnetMask());
  Serial.print("Gateway: ");
  Serial.println(WiFi.gatewayIP());
}

int value = 0;

void loop() {
  delay(5000);
  ++value; // Not used

  Serial.print("connecting to ");
  Serial.print(host);
  Serial.print(":");
  Serial.println(httpPort);

  // Use WiFiClient class to create TCP connections
  WiFiClient client;
  if (!client.connect(host, httpPort)) {
    Serial.println("connection failed");
    return;
  }

  // Now create a URI for the request
  String url = "/mux/cache?option=tiny";
  Serial.print("Requesting URL: ");
  Serial.println(url);

  // This will send the request to the server
  sendRequest(client, "GET", url, "HTTP/1.1", host);
  delay(500);

  // Read all the lines of the reply from server and print them to Serial
  while (client.available()) {
    String line = client.readStringUntil('\r');
    Serial.print("Raw Json:"); Serial.println(line); // Output here
    JsonObject& root = jsonBuffer.parseObject(line);
    if (!root.success()) {
      Serial.println("<< JSON parseObject failed");
    } else {
      Serial.println(">> Good!");
      const char* bsp = root["BSP"];
      Serial.print("BSP:");
      Serial.println(bsp);
    }
  }
  Serial.println();
  Serial.println("closing connection");
}

void sendRequest(WiFiClient client, String verb, String url, String protocol, String host) {
  String request = verb + " " + url + " " + protocol + "\r\n" +
                   "Host: " + host + "\r\n" +
                   "Connection: close\r\n" +
                   "\r\n";
  client.print(request);
}

