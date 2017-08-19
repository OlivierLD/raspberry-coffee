import i2c.sensor.LSM303;
/**
 * Using Sketch > Add File..., select I2C.SPI/build/libs/I2C.SPI-1.0-all.jar 
 */

boolean withSensor = true;

LSM303 lsm303;

int centerX = 200;
int centerY = 200;
int intRadius = 20;
int extRadius = 190;

void setup(){
//frameRate(4);
  size(400, 400);
//stroke(255);
  noStroke();
  noFill();
  textSize(10);
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

float heading = 0;

void draw(){
  background(0);
  fill(255);
  if (withSensor) {
    heading = (float)lsm303.getHeading();
  } else {
    heading += 0.25;
    heading = heading % 360;
  }
  text(String.format("%05.1f\272", heading), 5, 12);

  float _heading = heading + 90;
  for (int q=0; q<4; q++) {
    fill(255);
    triangle(centerX, 
             centerY, 
             (float)(centerX + (extRadius * Math.cos(Math.toRadians(_heading)))),
             (float)(centerY + (extRadius * Math.sin(Math.toRadians(_heading)))),
             (float)(centerX + (intRadius * Math.cos(Math.toRadians(_heading + 45)))),
             (float)(centerY + (intRadius * Math.sin(Math.toRadians(_heading + 45)))));
    fill(128);
    triangle(centerX, 
             centerY, 
             (float)(centerX + (extRadius * Math.cos(Math.toRadians(_heading)))),
             (float)(centerY + (extRadius * Math.sin(Math.toRadians(_heading)))),
             (float)(centerX + (intRadius * Math.cos(Math.toRadians(_heading - 45)))),
             (float)(centerY + (intRadius * Math.sin(Math.toRadians(_heading - 45)))));
    _heading += 90;
  }
}