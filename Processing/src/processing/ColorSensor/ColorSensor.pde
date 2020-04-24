import i2c.sensor.TCS34725;
/**
 * Using Sketch > Add File..., select I2C.SPI/build/libs/I2C-SPI-1.0-all.jar
 */

boolean withSensor = true;

TCS34725 sensor;
int colorThreshold = 1_000;

void setup(){
  size(200, 200);
//noStroke();
//noFill();
  smooth(4);
  textSize(18);
  frameRate(20);
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

int prevR = 0, prevG = 0, prevB = 0;

void draw(){
  background(0); // 0:Black, 255:White
  if (withSensor) {
    try {
      sensor.setInterrupt(false); // turn sensor's led on
      delay(60);
      //try {
      //  Thread.sleep(60);
      //} catch (InterruptedException ie) {
      //} // Takes 50ms to read, see above
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
      // Paint the color ball
      if (r > colorThreshold || g > colorThreshold || b > colorThreshold) {
        // This calculation deserves improvements
        redVol = Math.max(Math.min((int) ((r - colorThreshold) / 100), 100), 0);
        greenVol = Math.max(Math.min((int) ((g - colorThreshold) / 100), 100), 0);
        blueVol = Math.max(Math.min((int) ((b - colorThreshold) / 100), 100), 0);
        // Draw the color here. Make it glow.
        drawColorBall(redVol, greenVol, blueVol);
        String mess = String.format("RGB (%d, %d, %d)", redVol, greenVol, blueVol);
        fill(255);
        text(mess, 10, 20);
        println(mess);
        prevR = redVol;
        prevG = greenVol;
        prevB = blueVol;
      } else {
        // Black
        drawColorBall(0, 0, 0);
        String mess = String.format("RGB (%d, %d, %d)", 0, 0, 0);
        fill(255);
        text(mess, 10, 20);
        println(mess);
        prevR = 0;
        prevG = 0;
        prevB = 0;
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  } else {
    // Simulation. Random values.
    int redVol = (int)Math.round(Math.random() * 255);
    int greenVol = (int)Math.round(Math.random() * 255);
    int blueVol = (int)Math.round(Math.random() * 255);
    // New color
    drawColorBall(redVol, greenVol, blueVol);
    fill(255);
    text("- No sensor -", 10, 20);
    prevR = redVol;
    prevG = greenVol;
    prevB = blueVol;
  }
}

boolean glowing = true;

void drawColorBall(int r, int g, int b) {
  stroke(r, g, b);
  if (!glowing) {
    fill(r, g, b);
    ellipse(100, 100, 150, 150);
  } else {
    glow(prevR, r, prevG, g, prevB, b);
  }
}

void glow(int fromR, int toR, int fromG, int toG, int fromB, int toB) {
  noStroke();
  int nbsteps = 20;
  for (int i=0; i<nbsteps; i++) {
    int r = stepValue(fromR, toR, nbsteps, i);
    int g = stepValue(fromG, toG, nbsteps, i);
    int b = stepValue(fromB, toB, nbsteps, i);
//  println(String.format("r:%d g:%d b:%d", r, g, b));
    fill(r, g, b, 100);
    ellipse(100, 100, 150 - (150 / nbsteps) * i, 150 - (150 / nbsteps) * i);
  //delay(50);
  }
}

int stepValue(int from, int to, int nbStep, int step) {
  int delta = to - from;
  float progress = ((float)delta / (float)nbStep) * step;
  return from + (int)Math.round(progress);
}

void dispose() {
  println("Bye!");
  if (sensor != null) {
    try {
      sensor.setInterrupt(true);
      sensor.disable();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
