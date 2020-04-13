import i2c.sensor.VL53L0X;
/**
 * Numeric value returned by a VL53L0X.
 * Using the menu Sketch > Add File..., select I2C.SPI/build/libs/I2C-SPI-1.0-all.jar
 */

boolean withSensor = true;

VL53L0X vl53l0x;

void setup(){
  size(300, 300);
  stroke(255);
  noFill();
  textSize(30);
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
    distance = (int)(1000f * ((float)distFromMiddle / (float)(width / 2)));
  }
  // For 1000mm: 1 pixel of radius, for 1mm: height/2 radius.
  float ratio = (float)Math.min(distance, 1000) / 1000f;
  float radius = Math.max(2, (float)(width / 2) * (1f - ratio) * 1.0f);
  println(String.format("Distance: %d mm, ratio: %f, Radius: %f", distance, ratio, radius));
  fill(128);
  ellipse(width/2, height/2, 2 * radius, 2 * radius);
  fill(255);
//textSize(26);
  text(String.format("%d mm", distance), 10, 100);
}
