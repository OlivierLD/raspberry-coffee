/*
 * Celestial Almanac Computation.
 * Requires the date (and deltaT) as input parameters.
 */

#include "CelestStruct.h"
#include <MathUtils.h>

// MathUtils caMu;

float nb = 0;

void setup() {
  Serial.begin(9600);
  Serial.println("Setup completed");
}

const int BETWEEN_LOOPS = 500; // in milli-sec.
const String EOS = "\r\n";
String receivedSentence = "";

//char dataBuffer[128];

void loop() {

  nb += 1;
  //sprintf(dataBuffer, "nb=%f, Rad=%f", nb, degToRadians(nb));
  //Serial.println(dataBuffer);
  Serial.print("nb="); Serial.print(nb);
  Serial.print(", rad="); Serial.println(MathUtils::toRadians(nb));

  float deltaT = 69.2201;
  int year = 2020, month = 3, day = 5, hour = 9, minute = 15, second = 0;
  ComputedData * data = calculate(year, month, day, hour, minute, second, deltaT);
  Serial.print("JulianDate:"); Serial.println(data->JDE);

  delay(BETWEEN_LOOPS);
}
