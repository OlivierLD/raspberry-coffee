/**
   Simple HTTP get webclient REST test
   for Huzzah/ESP8266.

   Main class of what will be the TCP watch.

   Sends REST requests to the NavServer to get navigation data.
   That one spits out data on the Serial console, and on an oled screen.

   @author Olivier LeDiouris
*/
#include <Wire.h>
#include "ssd1306_i2c.h"
#include <ESP8266WiFi.h>

// #define DEBUG // Uncomment for more Serial output.

/* ----- Customizable Data ----- */
// Network and Host Names definitions, customize if necessary
// #define __RASPI_LOGGER__  // 192.168.127.1 on Pi-Net
#undef __RASPI_LOGGER__      // 192.168.42.4 on Sonic-00e0

#include "custom_values.h" // Contains _SSID, _PASSWORD, _HOST, _HTTP_PORT

const char* SSID     = _SSID;
const char* PASSWORD = _PASSWORD;

const char* HOST = _HOST;
const int HTTP_PORT = _HTTP_PORT;

const char* REST_REQUEST = "/mux/cache?option=txt"; // txt, not json.

const int BETWEEN_LOOPS = 1000; // in milli-sec.
/* ----- End of Customizable Data ----- */

// SSD1306 OLED Display connections and wiring
#define SDA 14
#define SCL 12
//#define RST 2

const int I2C = 0x3D;

// Initialize the oled display for address 0x3c
// 0x3D is the adafruit address.
// sda-pin=14 and sdc-pin=12
SSD1306 ssd1306(I2C, SDA, SCL);

// Data from REST server
float bsp, lat, lng, sog;
int cog, year, month, day, hour, mins, sec;
String date;

const int NS = 1;
const int EW = 2;

char* toDegMin(float data, int type) {
  int sign = (data >= 0) ? 1 : -1;
  float absData = data >= 0 ? data : -data; // abs returns an int...
  int intPart = (int)absData;
  float minSec = (absData - intPart) * 60;
#ifdef DEBUG
  Serial.print("Raw:"); Serial.print(data);Serial.print(" -> "); Serial.println(absData);
  Serial.print("Int:"); Serial.println(intPart);
  Serial.print("MinSec:"); Serial.println(minSec);
#endif
  char degMinVal[64];
  sprintf(degMinVal, "%c %d %.2f'", (type == NS ? (sign == 1 ? 'N' : 'S') : (sign == 1 ? 'E' : 'W')), intPart, minSec);
#ifdef DEBUG
  Serial.print("Deg Minutes:");
  Serial.println(degMinVal);
#endif
  return degMinVal;
}

void repaint(int x, int y) {
  ssd1306.clear();
  ssd1306.setColor(BLACK);
  ssd1306.fillRect(1, 1, 128, 64);
  ssd1306.setColor(WHITE);
  ssd1306.setFontScale2x2(false);

  char dataBuffer[128];
  int yOffset = 8;
  ssd1306.drawString(1 + x, 0, "--- Nav Data ---");
//  sprintf(dataBuffer, "BSP: %.2f kts", bsp);
//  ssd1306.drawString(1 + x, yOffset + y, dataBuffer);
//  yOffset += 8;
  sprintf(dataBuffer, "L: %s", toDegMin(lat, NS));
  ssd1306.drawString(1 + x, yOffset + y, dataBuffer);
  yOffset += 8;
  sprintf(dataBuffer, "G: %s", toDegMin(lng, EW));
  ssd1306.drawString(1 + x, yOffset + y, dataBuffer);

  yOffset += 8;
  sprintf(dataBuffer, "SOG: %.2f kts", sog);
  ssd1306.drawString(1 + x, yOffset + y, dataBuffer);
  yOffset += 8;
  sprintf(dataBuffer, "COG: %d", cog);
  ssd1306.drawString(1 + x, yOffset + y, dataBuffer);
  yOffset += 8;
  ssd1306.drawString(1 + x, yOffset + y, "----------------");

  ssd1306.display();
}

void setup() {
  // initialize display
  ssd1306.init();
  ssd1306.flipScreenVertically();

  ssd1306.clear();
  ssd1306.display();

  Serial.begin(115200); // Console output
  delay(100);

  // Start by connecting to a WiFi network
  Serial.print("Connecting to ");
  Serial.println(SSID);
  WiFi.begin(SSID, PASSWORD);

  int count = 0;
  char dataBuffer[128];

  // Testing the screen
  ssd1306.clear();
  ssd1306.setColor(BLACK);
  ssd1306.fillRect(1, 1, 128, 64);
  ssd1306.setColor(WHITE);
  ssd1306.setFontScale2x2(false);
#ifdef LINE_TEST
  for (int line = 0; line < 8; line++) {
    sprintf(dataBuffer, "Line #%d. For tests", (line + 1));
    ssd1306.drawString(0, line * 8, dataBuffer);
  }
#endif
  ssd1306.drawString(0, 8, "Test:");
  sprintf(dataBuffer, "L:%s", toDegMin(37.7489, NS));
  ssd1306.drawString(0, 16, dataBuffer);
  sprintf(dataBuffer, "G:%s", toDegMin(-122.5070, EW));
  ssd1306.drawString(0, 24, dataBuffer);

  ssd1306.display();
  delay(5000);

  // Trying to connect to the server
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");

    ssd1306.clear();
    ssd1306.setColor(BLACK);
    ssd1306.fillRect(1, 1, 128, 64);
    ssd1306.setColor(WHITE);
    ssd1306.setFontScale2x2(false);
    sprintf(dataBuffer, "%d. %s...", count, SSID);
    ssd1306.drawString(count % 56, (count % 56)/* + 8*/, dataBuffer); // "Connecting...");
    ssd1306.display();
    count++;
  }
  // Connected!
  ssd1306.clear();
  ssd1306.setColor(BLACK);
  ssd1306.fillRect(1, 1, 128, 64);
  ssd1306.setColor(WHITE);
  ssd1306.setFontScale2x2(false);
  ssd1306.drawString(1, 8, "Connection to");
  ssd1306.drawString(1, 16, SSID);
  ssd1306.drawString(1, 24, "Ready");
  ssd1306.display();

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.print("Netmask: ");
  Serial.println(WiFi.subnetMask());
  Serial.print("Gateway: ");
  Serial.println(WiFi.gatewayIP());
}

/* 
 * The keys of the sentences returned by the REST request, 
 * like 'key=value'
 */
struct KEYS {
	const String BSP = "BSP";
	const String LAT = "LAT";
	const String LNG = "LNG";
	const String SOG = "SOG";
	const String COG = "COG";
	const String DATE = "DATE";
	const String YEAR = "YEAR";
	const String MONTH = "MONTH";
	const String DAY = "DAY";
	const String HOUR = "HOUR";
	const String MIN = "MIN";
	const String SEC = "SEC";
} keys;

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
      if (key == keys.BSP) {
        bsp = value.toFloat();
      } else if (key == keys.LAT) {
        lat = value.toFloat();
      } else if (key == keys.LNG) {
        lng = value.toFloat();
      } else if (key == keys.SOG) {
        sog = value.toFloat();
      } else if (key == keys.DATE) {
        date = value;
      } else if (key == keys.COG) {
        cog = value.toInt();
      } else if (key == keys.YEAR) {
        year = value.toInt();
      } else if (key == keys.MONTH) {
        month = value.toInt();
      } else if (key == keys.DAY) {
        day = value.toInt();
      } else if (key == keys.HOUR) {
        hour = value.toInt();
      } else if (key == keys.MIN) {
        mins = value.toInt();
      } else if (key == keys.SEC) {
        sec = value.toInt();
      }
    }
  }
  char dataBuffer[128];
  sprintf(dataBuffer, "BSP=%f, LAT=%f, LNG=%f", bsp, lat, lng);
  Serial.println(dataBuffer);
  sprintf(dataBuffer, "SOG=%f, COG=%d", sog, cog);
  Serial.println(dataBuffer);
  Serial.print("Date=");
  Serial.println(date);
  Serial.println("<< closing connection");

  repaint(0, 0); // Display data on screen
}

void sendRequest(WiFiClient client, String verb, String url, String protocol, String host) {
  String request = verb + " " + url + " " + protocol + "\r\n" +
                   "Host: " + host + "\r\n" +
                   "Connection: close\r\n" +
                   "\r\n";
  client.print(request);
}

/*
 * Reads the Key in "Key=Value"
 */
String getKey(String line) {
  int separatorPosition = line.indexOf("=");
  if (separatorPosition == -1) {
    return "";
  }
  return line.substring(0, separatorPosition);
}

/*
 * Reads the Value in "Key=Value"
 */
String getValue(String line) {
  int separatorPosition = line.indexOf("=");
  if (separatorPosition == -1) {
    return "";
  }
  return line.substring(separatorPosition + 1);
}

