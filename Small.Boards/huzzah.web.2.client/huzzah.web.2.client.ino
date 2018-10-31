/*
   Simple HTTP get webclient REST test
   for Huzzah/ESP8266.
   That one spits out data oin the Serial console (no oled screen).
*/

#include <ESP8266WiFi.h>

// Network and Host definitions
const char* ssid     = "Sonic-00e0";
const char* password = "67369c7831";

const char* host = "192.168.42.4";
const int httpPort = 9998;

void setup() {
  Serial.begin(115200); // Console output
  delay(100);

  // Start by connecting to a WiFi network
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

float bsp, lat, lng, sog;
int cog, year, month, day, hour, mins, sec;
String date;

void loop() {
  delay(5000); // 5 seconds?

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

  // Now create a URI for the REST request
  String url = "/mux/cache?option=txt"; // txt, not json.
  Serial.print("Requesting URL: ");
  Serial.println(url);

  // This will send the request to the server
  sendRequest(client, "GET", url, "HTTP/1.1", host);
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
  Serial.println("closing connection");
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

