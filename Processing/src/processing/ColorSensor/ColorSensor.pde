import i2c.sensor.TCS34725;
/**
 * Using Sketch > Add File..., select I2C.SPI/build/libs/I2C.SPI-1.0-all.jar 
 */

boolean withSensor = true;

TCS34725 sensor;
int colorThreshold = 4_000;

void setup(){
  size(200, 200);
  noStroke();
  noFill();
  textSize(18);
  if (withSensor) {
    try {
      sensor = new TCS34725(TCS34725.TCS34725_INTEGRATIONTIME_50MS, TCS34725.TCS34725_GAIN_4X);
    } catch (Exception ex) {
      ex.printStackTrace();
      withSensor = false;
      println("-----------------------------------------------");
      println(" No sensor found, moving on in simulation mode.");
      println("-----------------------------------------------");
      stroke(255);
    }
  }
}

void draw(){
  background(0); // Black
  if (withSensor) {
    try {
      sensor.setInterrupt(false); // turn sensor's led on
      try {
        Thread.sleep(60);
      } catch (InterruptedException ie) {
      } // Takes 50ms to read, see above
      TCS34725.TCSColor rgb = sensor.getRawData();
      sensor.setInterrupt(true); // turn sensor's led off
      int r = rgb.getR(),
          g = rgb.getG(),
          b = rgb.getB();
      int greenVol = 0,
          blueVol = 9,
          redVol = 0;
      // Display the color on the screen accordingly
      println("Read color R:" + r + " G:" + g + " B:" + b);
      // Send to 3-color led. The output is digital!! Not analog.
      if (r > colorThreshold || g > colorThreshold || b > colorThreshold) {
        // This calculation deserves improvements
        redVol = Math.max(Math.min((int) ((r - colorThreshold) / 100), 100), 0);
        greenVol = Math.max(Math.min((int) ((g - colorThreshold) / 100), 100), 0);
        blueVol = Math.max(Math.min((int) ((b - colorThreshold) / 100), 100), 0);
        // Draw the color here. Make it glow.
        //greenPin.adjustPWMVolume(greenVol);
        //bluePin.adjustPWMVolume(blueVol);
        //redPin.adjustPWMVolume(redVol);
        String mess = String.format("RGB (%d, %d, %d)", redVol, greenVol, blueVol);
        text(mess, 10, 20);
        println(mess);
      } else {
        // Black
        //redPin.low();
        //greenPin.low();
        //bluePin.low();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  } else {
    text("No sensor", 10, 20);
  }
}

void dispose() {
  println("Bye!");
  if (sensor != null) {
    try {
      sensor.disable();
    } catch (Exception ex) {
      //
    }
  }
}