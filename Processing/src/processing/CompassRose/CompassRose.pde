import i2c.sensor.LSM303;
/**
 * Value returned by an LSM303, graphically.
 *
 * Using Sketch > Add File..., select I2C.SPI/build/libs/I2C-SPI-1.0-all.jar
 */

boolean withSensor = true;

LSM303 lsm303;

int centerX = 200;
int centerY = 200;
int intRadius =  20;
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
    println(" Drag the mouse left and right to change the heading");
    println("----------------------------------------------------");
  }
}

float heading = 0;

void draw(){
  background(0); // Black
  if (withSensor) {
    heading = (float)lsm303.getHeading();
  } else {
    float increment = 0;
    if (!mousePressed) {
      increment = 0.25;
    } else {
//    println(String.format("Mouse: X=%d, Y=%d", mouseX, mouseY));
      int diffX = mouseX - centerX;
      increment = diffX / 100f;
    }
    heading += increment;
    heading = heading % 360;
  }
  textSize(10);
  fill(255);
  text(String.format("%05.1f\272", heading), 5, 12);

  // Drawing the rose
  float _heading = heading + 90;
  for (int q=0; q<4; q++) {
    fill(255); // White
    triangle(centerX,
             centerY,
             (float)(centerX + (extRadius * Math.cos(Math.toRadians(_heading)))),
             (float)(centerY + (extRadius * Math.sin(Math.toRadians(_heading)))),
             (float)(centerX + (intRadius * Math.cos(Math.toRadians(_heading + 45)))),
             (float)(centerY + (intRadius * Math.sin(Math.toRadians(_heading + 45)))));
    fill(128); // Gray
    triangle(centerX,
             centerY,
             (float)(centerX + (extRadius * Math.cos(Math.toRadians(_heading)))),
             (float)(centerY + (extRadius * Math.sin(Math.toRadians(_heading)))),
             (float)(centerX + (intRadius * Math.cos(Math.toRadians(_heading - 45)))),
             (float)(centerY + (intRadius * Math.sin(Math.toRadians(_heading - 45)))));
    _heading += 90;
  }
  // print the North
  textSize(32);
  fill(255, 204, 0); // Goldish
  pushMatrix();
  translate(centerX + (extRadius * (float)Math.cos(Math.toRadians(heading - 90))),
            centerY + (extRadius * (float)Math.sin(Math.toRadians(heading - 90))));
  rotate((float)Math.toRadians(heading));
  String north = "N";
  float w = textWidth(north);
  text(north, -w / 2, 40);
  popMatrix();
}
