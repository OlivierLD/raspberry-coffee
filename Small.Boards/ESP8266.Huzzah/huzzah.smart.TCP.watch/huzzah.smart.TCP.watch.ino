/*
   Simple HTTP get webclient REST test
   for Huzzah/ESP8266.
   That one spits out data oin the Serial console (no oled screen).
*/

#include <Wire.h>
#include "ssd1306_i2c.h"
#include <ESP8266WiFi.h>

// Network, Host and request definitions, customize if necessary
const char* SSID     = "Sonic-00e0"; // "Pi-Net";
const char* PASSWORD = "67369c7831"; // "raspberrypi";

const char* HOST = "192.168.42.4"; // "192.168.127.1";
const int HTTP_PORT = 9998;        // 8080

const char* REST_REQUEST = "/mux/cache?option=txt"; // txt, not json.

// OLED Display connections and wiring
#define SDA 14
#define SCL 12
//#define RST 2

const int I2C = 0x3D;

// Initialize the oled display for address 0x3c
// 0x3D is the adafruit address...
// sda-pin=14 and sdc-pin=12
SSD1306 ssd1306(I2C, SDA, SCL);

float bsp, lat, lng, sog;
int cog, year, month, day, hour, mins, sec;
String date;

void drawDisplay(int x, int y) {
  Serial.print("Displaying...");
  ssd1306.clear();
  ssd1306.setFontScale2x2(false);
  ssd1306.setColor(BLACK);
  ssd1306.drawString(x + 1, y + 8, date); // was "Now"
  /*
  display.drawXbm(x + 7, y + 7, 50, 50, getIconFromString(weather.getCurrentIcon()));
  display.setFontScale2x2(true);
  display.drawString(64 + x, 20 + y, String(weather.getCurrentTemp()) + "F");
  display.setFontScale2x2(false);
  display.drawString(64 + x, 40 + y, String(weather.getCurrentSummary()));
  */
  ssd1306.display();
}

void setup() {

  // initialize display
  ssd1306.init();
  ssd1306.flipScreenVertically();

  // set the drawing functions
  // ssd1306.setFrameCallbacks(3, frameCallbacks);
  // how many ticks does a slide of frame take?
  // ssd1306.setFrameTransitionTicks(10);

  ssd1306.clear();
  ssd1306.display();

  Serial.begin(115200); // Console output
  delay(100);

  // Start by connecting to a WiFi network
  Serial.print("Connecting to ");
  Serial.println(SSID);
  WiFi.begin(SSID, PASSWORD);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");

    ssd1306.clear();
//  ssd1306.drawXbm(34, 10, 60, 36, WiFi_Logo_bits);
    ssd1306.setColor(INVERSE);
//  ssd1306.fillRect(10, 10, 108, 44); // ?? values ?
    ssd1306.fillRect(0, 0, 128, 64); 
    ssd1306.setColor(WHITE);
//  drawSpinner(3, counter % 3);
    ssd1306.drawString(1, 8, "Connecting");
    ssd1306.display();
  }

  ssd1306.setColor(BLACK);
  ssd1306.drawString(1, 8, "Ready");
  ssd1306.display();

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.print("Netmask: ");
  Serial.println(WiFi.subnetMask());
  Serial.print("Gateway: ");
  Serial.println(WiFi.gatewayIP());
  Serial.println("------------------------------------");
}

// For the REST response parsing
const String BSP   = "BSP";
const String LAT   = "LAT";
const String LNG   = "LNG";
const String SOG   = "SOG";
const String COG   = "COG";
const String DATE  = "DATE";
const String YEAR  = "YEAR";
const String MONTH = "MONTH";
const String DAY   = "DAY";
const String HOUR  = "HOUR";
const String MIN   = "MIN";
const String SEC   = "SEC";

const int BETWEEN_LOOPS = 5000; // 5 sec.

void loop() {
  delay(BETWEEN_LOOPS);

  Serial.print(">> connecting to ");
  Serial.print(HOST);
  Serial.print(":");
  Serial.println(HTTP_PORT);

  // Use WiFiClient class to create TCP connections
  WiFiClient client;
  if (!client.connect(HOST, HTTP_PORT)) {
    Serial.println("!! connection failed");
    return;
  }

  // Now create a URI for the REST request
  String url = REST_REQUEST;
  Serial.print("Requesting URL: ");
  Serial.println(url);

  // This will send the request to the server
  sendRequest(client, "GET", url, "HTTP/1.1", HOST);
  delay(500);

  // Read all the lines of the reply from server and print them to Serial
  // Keys are BSP, LAT, LNG, SOG, COG, DATE, YEAR, MONTH, DAY, HOUR, MIN, SEC
  while (client.available()) {
	  String line = client.readStringUntil('\n');
    // Serial.println(line);
    String key = getKey(line);
    if (key.length() > 0) {
      String value = getValue(line);
      if (key == BSP) {
        bsp = value.toFloat();
      } else if (key == LAT) {
        lat = value.toFloat();
      } else if (key == LNG) {
        lng = value.toFloat();
      } else if (key == SOG) {
        sog = value.toFloat();
      } else if (key == DATE) {
        date = value;
      } else if (key == COG) {
        cog = value.toInt();
      } else if (key == YEAR) {
        year = value.toInt();
      } else if (key == MONTH) {
        month = value.toInt();
      } else if (key == DAY) {
        day = value.toInt();
      } else if (key == HOUR) {
        hour = value.toInt();
      } else if (key == MIN) {
        mins = value.toInt();
      } else if (key == SEC) {
        sec = value.toInt();
			}
    }
  }
  char dataBuffer[128];
  sprintf(dataBuffer, "Bsp=%f, Lat=%f, Lng=%f", bsp, lat, lng);
  Serial.println(dataBuffer);
  sprintf(dataBuffer, "SOG=%f, COG=%d", sog, cog);
  Serial.println(dataBuffer);
  Serial.print("Date=");
  Serial.println(date);
  Serial.println("<< closing connection");

  drawDisplay(0, 0);
}

void sendRequest(WiFiClient client, String verb, String url, String protocol, String host) {
  String request = verb + " " + url + " " + protocol + "\r\n" +
                   "Host: " + host + "\r\n" +
                   "Connection: close\r\n" +
                   "\r\n";
  client.print(request);
}

String getKey(String line) {
  int separatorPosition = line.indexOf("=");
  if (separatorPosition == -1) {
    return "";
  }
  return line.substring(0, separatorPosition);
}

String getValue(String line) {
  int separatorPosition = line.indexOf("=");
  if (separatorPosition == -1) {
    return "";
  }
  return line.substring(separatorPosition + 1);
}
