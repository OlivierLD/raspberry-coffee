// Basic demo for magnetometer readings from Adafruit LIS3MDL
// See https://learn.adafruit.com/lis3mdl-triple-axis-magnetometer?view=all
// and https://learn.adafruit.com/adafruit-sensorlab-magnetometer-calibration?view=all#

#include <Wire.h>
#include <Adafruit_LIS3MDL.h>
#include <Adafruit_Sensor.h>

Adafruit_LIS3MDL lis3mdl;
#define LIS3MDL_CLK 13
#define LIS3MDL_MISO 12
#define LIS3MDL_MOSI 11
#define LIS3MDL_CS 10

void setup(void) {
  Serial.begin(115200);
  while (!Serial) delay(10);     // will pause Zero, Leonardo, etc until serial console opens

  Serial.println("Adafruit LIS3MDL test!");
  
  // Try to initialize!
  if (! lis3mdl.begin_I2C()) {          // hardware I2C mode, can pass in address & alt Wire
  //if (! lis3mdl.begin_SPI(LIS3MDL_CS)) {  // hardware SPI mode
  //if (! lis3mdl.begin_SPI(LIS3MDL_CS, LIS3MDL_CLK, LIS3MDL_MISO, LIS3MDL_MOSI)) { // soft SPI
    Serial.println("Failed to find LIS3MDL chip");
    while (1) { delay(10); }
  }
  Serial.println("LIS3MDL Found!");

  lis3mdl.setPerformanceMode(LIS3MDL_MEDIUMMODE);
  Serial.print("Performance mode set to: ");
  switch (lis3mdl.getPerformanceMode()) {
    case LIS3MDL_LOWPOWERMODE: Serial.println("Low"); break;
    case LIS3MDL_MEDIUMMODE: Serial.println("Medium"); break;
    case LIS3MDL_HIGHMODE: Serial.println("High"); break;
    case LIS3MDL_ULTRAHIGHMODE: Serial.println("Ultra-High"); break;
  }

  lis3mdl.setOperationMode(LIS3MDL_CONTINUOUSMODE);
  Serial.print("Operation mode set to: ");
  // Single shot mode will complete conversion and go into power down
  switch (lis3mdl.getOperationMode()) {
    case LIS3MDL_CONTINUOUSMODE: 
      Serial.println("Continuous"); 
      break;
    case LIS3MDL_SINGLEMODE: 
      Serial.println("Single mode"); 
      break;
    case LIS3MDL_POWERDOWNMODE: 
      Serial.println("Power-down"); 
      break;
  }

  lis3mdl.setDataRate(LIS3MDL_DATARATE_155_HZ);
  // You can check the datarate by looking at the frequency of the DRDY pin
  Serial.print("Data rate set to: ");
  switch (lis3mdl.getDataRate()) {
    case LIS3MDL_DATARATE_0_625_HZ: Serial.println("0.625 Hz"); break;
    case LIS3MDL_DATARATE_1_25_HZ: Serial.println("1.25 Hz"); break;
    case LIS3MDL_DATARATE_2_5_HZ: Serial.println("2.5 Hz"); break;
    case LIS3MDL_DATARATE_5_HZ: Serial.println("5 Hz"); break;
    case LIS3MDL_DATARATE_10_HZ: Serial.println("10 Hz"); break;
    case LIS3MDL_DATARATE_20_HZ: Serial.println("20 Hz"); break;
    case LIS3MDL_DATARATE_40_HZ: Serial.println("40 Hz"); break;
    case LIS3MDL_DATARATE_80_HZ: Serial.println("80 Hz"); break;
    case LIS3MDL_DATARATE_155_HZ: Serial.println("155 Hz"); break;
    case LIS3MDL_DATARATE_300_HZ: Serial.println("300 Hz"); break;
    case LIS3MDL_DATARATE_560_HZ: Serial.println("560 Hz"); break;
    case LIS3MDL_DATARATE_1000_HZ: Serial.println("1000 Hz"); break;
  }
  
  lis3mdl.setRange(LIS3MDL_RANGE_4_GAUSS);
  Serial.print("Range set to: ");
  switch (lis3mdl.getRange()) {
    case LIS3MDL_RANGE_4_GAUSS: Serial.println("+-4 gauss"); break;
    case LIS3MDL_RANGE_8_GAUSS: Serial.println("+-8 gauss"); break;
    case LIS3MDL_RANGE_12_GAUSS: Serial.println("+-12 gauss"); break;
    case LIS3MDL_RANGE_16_GAUSS: Serial.println("+-16 gauss"); break;
  }

  lis3mdl.setIntThreshold(500);
  lis3mdl.configInterrupt(false, false, true, // enable z axis
                          true, // polarity
                          false, // don't latch
                          true); // enabled!
}

#define PI 3.14159265

float toDegrees(float rad) {
  return rad * 180.0 / PI;  
}

#define SERIAL_PLOTTER true

void loop() {
  lis3mdl.read();      // get X Y and Z data at once
  // Then print out the raw data
  if (!SERIAL_PLOTTER) {
    Serial.print("\nRaw: \tX:  "); Serial.print(lis3mdl.x); 
    Serial.print("  \tY:  "); Serial.print(lis3mdl.y); 
    Serial.print("  \tZ:  "); Serial.println(lis3mdl.z); 
  }
  /* Or....get a new sensor event, normalized to uTesla */
  sensors_event_t event; 
  lis3mdl.getEvent(&event);
  /* Display the results (magnetic field is measured in uTesla) */
  if (!SERIAL_PLOTTER) {
    Serial.print("uTesla\tX: "); Serial.print(event.magnetic.x);
    Serial.print(" \tY: "); Serial.print(event.magnetic.y); 
    Serial.print(" \tZ: "); Serial.print(event.magnetic.z); 
    Serial.println(" ");
  } else {
    Serial.print(event.magnetic.x);
    Serial.print("\t"); Serial.print(event.magnetic.y); 
    Serial.print("\t"); Serial.print(event.magnetic.z); 
    Serial.println("");
  }

  if (!SERIAL_PLOTTER) {
    float heading = toDegrees(atan2(event.magnetic.y, event.magnetic.x)); // Orientation in plan X-Y
    float pitch = toDegrees(atan2(event.magnetic.y, event.magnetic.z));   // Orientation in plan Y-Z
    float roll = toDegrees(atan2(event.magnetic.x, event.magnetic.z));    // Orientation in plan X-Z
  
    Serial.print("\tHeading: "); Serial.print(heading); Serial.println(" Deg");
    Serial.print("\tPitch: "); Serial.print(pitch); Serial.println(" Deg");
    Serial.print("\tRoll: "); Serial.print(roll); Serial.println(" Deg");
  }

  delay(100); 
  Serial.println();
}
