#include <SHT1x.h>

#define dataPin  6  // Blue wire,   NodeMCU pin D6 (was 12 for ESP8266)
#define clockPin 7  // Yellow Wire, NodeMCU pin D5 (was 14 for ESP8266)

SHT1x sht1x(dataPin, clockPin); // instantiate SHT1x object

void setup() {
   Serial.begin(38400); // Open serial connection to report values to host
   Serial.println("Starting up");
}

void loop() {
  float temp_c;
  float temp_f;
  float humidity;

  temp_c = sht1x.readTemperatureC(); // Read values from the sensor
  temp_f = sht1x.readTemperatureF();
  humidity = sht1x.readHumidity();

  Serial.print("Temperature: "); // Print the values to the serial port
  Serial.print(temp_c, DEC);
  Serial.print(" C / ");
  Serial.print(temp_f, DEC);
  Serial.print(" F. Humidity: ");
  Serial.print(humidity);
  Serial.println("%");

  delay(2000);
}
