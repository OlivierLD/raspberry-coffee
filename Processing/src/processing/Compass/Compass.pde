import i2c.sensor.LSM303;
/**
 * Numeric value returned by an LSM303.
 * Using Sketch > Add File..., select I2C.SPI/build/libs/I2C-SPI-1.0-all.jar
 */

boolean withSensor = true;

LSM303 lsm303;

void setup(){
  size(275, 200);
  stroke(255);
  noFill();
  textSize(72);
  if (withSensor) {
    try {
      lsm303 = new LSM303(LSM303.EnabledFeature.MAGNETOMETER);
    } catch (Exception ex) {
      ex.printStackTrace();
      withSensor = false;
      println("-----------------------------------------------");
      println(" No sensor found, moving on in simulation mode.");
      println("-----------------------------------------------");
    }
  }
  if (!withSensor) {
    println("----------------------------------------------------");
    println(" Move the mouse left and right to change the heading");
    println("----------------------------------------------------");
  }
}

float heading = -90; // -PI / 4;

void draw(){
  background(0);
  fill(255);
  if (withSensor) {
    heading = (float)lsm303.getHeading();
  } else {
    int diffX = mouseX - (275 / 2);
    float increment = diffX / 10_000f;
    println(String.format(">> diffX:%d, inc:%f", diffX, increment));
    heading += increment;
    while (heading < 0) {
      heading += 360;
    }
    heading = heading % 360;
  }
  text(String.format("%05.1f\272", heading), 10, 100);
}
