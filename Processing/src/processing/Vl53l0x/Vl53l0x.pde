import i2c.sensor.VL53L0X;
/**
 * Numeric value returned by an LSM303.
 * Using Sketch > Add File..., select I2C.SPI/build/libs/I2C.SPI-1.0-all.jar
 */

boolean withSensor = true;

VL53L0X vl53l0x;

void setup(){
  size(275, 200);
  stroke(255);
  noFill();
  textSize(72);
  if (withSensor) {
    try {
      vl53l0x = new VL53L0X();
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
    println(" Move the mouse left and right to change the radius ");
    println("----------------------------------------------------");
  }
}

int distance = 0;

void draw(){
  background(0);
  fill(255);
  if (withSensor) {
    try {
    distance = vl53l0x.range();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  } else {
    int distFromMiddle = Math.abs(mouseX - (width / 2));
    if (distFromMiddle > 0) {
     distance = (int)(1000f * (1 / distFromMiddle));
    } else {
      distance = 0;
    }
  }
  // For 1000mm: 1 pixel of radius, for 1mm: height/2 radius.
  float radius = (height / 2) * (1 / distance); 
  fill(128);
  ellipse(width/2, height/2, radius, radius);
  fill(255);
  text(String.format("%d mm", distance), 10, 100);
}