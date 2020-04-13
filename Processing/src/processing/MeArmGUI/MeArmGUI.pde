/**
 * 4 Scrollbars to drive a MeArm robotic arm..
 * Move the scrollbars left and right to change the positions of associated servos.
 *
 * Using Sketch > Add File..., select I2C.SPI/build/libs/I2C-SPI-1.0-all.jar
 */
// import i2c.samples.mearm.MeArmPilot; // Not mandatory in Processing.

HScrollbar hs1, // Up and Down
           hs2, // Left and Right
           hs3, // Back and Forth
           hs4; // Open and Close

PImage bg;
float imgFactor;

void setup() {
  size(640, 320);
  noStroke();
  textSize(20);
  hs1 = new HScrollbar(10, (height/4) - 16, width - 20, 16, 16);
  hs2 = new HScrollbar(10, (height/2) - 16, width - 20, 16, 16);
  hs3 = new HScrollbar(10, (3 * height/4) - 16, width - 20, 16, 16);
  hs4 = new HScrollbar(10, (height) - 16, width - 20, 16, 16);

  bg = loadImage("MeArm.jpg");
  imgFactor = Math.max((bg.width / 640f), (bg.height / 320f));

  int left   = MeArmPilot.DEFAULT_LEFT_SERVO_CHANNEL;
  int right  = MeArmPilot.DEFAULT_RIGHT_SERVO_CHANNEL;
  int claw   = MeArmPilot.DEFAULT_CLAW_SERVO_CHANNEL;
  int bottom = MeArmPilot.DEFAULT_BOTTOM_SERVO_CHANNEL;

  try {
    MeArmPilot.initContext(left, claw, bottom, right);
  } catch (I2CFactory.UnsupportedBusNumberException oops) {
    println(">> Ooops!, wrong bus... Moving on anyway, but without the board.");
  }

  // Initializing MeArm pos
  MeArmPilot.runMacro(MeArmPilot.initStop());
  MeArmPilot.runMacro(MeArmPilot.initialPosition());
}

void draw() {
  background(255);
  // right aligned.
  image(bg, (width) - ((bg.width / imgFactor)), 0, (bg.width / imgFactor), (bg.height / imgFactor));

  float upDownPos = hs1.getPos() - ((width) / 2);
  float leftRightPos = hs2.getPos() - ((width) / 2);
  float backForthPos = hs3.getPos() - ((width) / 2);
  float openClosePos = hs4.getPos() - ((width) / 2);
  fill(255);

  hs1.update();
  hs2.update();
  hs3.update();
  hs4.update();

  hs1.display();
  hs2.display();
  hs3.display();
  hs4.display();

  stroke(0);
  line(0, 28, width, 28);
  noStroke();
  fill(0, 0, 255);
  text("Drive a MeArm robotic arm", 10, 25);

  fill(0);
  textSize(20f);
  text(String.format("Up and Down   : %+03.0f", upDownPos / 3.09), 100, (height / 4) - 32);
  textSize(10f);
  text("Down", 10, (height / 4) - 32);
  text("Up", width - 40, (height / 4) - 32);
  textSize(20f);
  text(String.format("Left and Right: %+03.0f", leftRightPos / 3.09), 100, (height / 2) - 32);
  textSize(10f);
  text("Right", 10, (height / 2) - 32);
  text("Left", width - 40, (height / 2) - 32);
  textSize(20f);
  text(String.format("Back and Forth: %+03.0f", backForthPos / 3.09), 100, (3 * height / 4) - 32);
  textSize(10f);
  text("Back", 10, (3 * height / 4) - 32);
  text("Forth", width - 40, (3 * height / 4) - 32);
  textSize(20f);
  text(String.format("Open and Close: %+03.0f", openClosePos / 3.09), 100, (height) - 32);
  textSize(10f);
  text("Opened", 10, height - 32);
  text("Closed", width - 40, height - 32);

  // Drive the servos here.
  int leftSliderValue = (int)Math.round(upDownPos / 3.09);
  int clawSliderValue = (int)Math.round(openClosePos / 3.09);
  int bottomSliderValue = (int)Math.round(leftRightPos / 3.09);
  int rightSliderValue = (int)Math.round(backForthPos / 3.09);
  // The SLIDE command is defined in MeArmPilot
  MeArmPilot.runMacro(String.format("SLIDE: %s, %d", MeArmPilot.LEFT, leftSliderValue));
  MeArmPilot.runMacro(String.format("SLIDE: %s, %d", MeArmPilot.RIGHT, rightSliderValue));
  MeArmPilot.runMacro(String.format("SLIDE: %s, %d", MeArmPilot.CLAW, clawSliderValue));
  MeArmPilot.runMacro(String.format("SLIDE: %s, %d", MeArmPilot.BOTTOM, bottomSliderValue));
}

void dispose() {
  println("Parking the MeArm");

  MeArmPilot.runMacro(MeArmPilot.initialPosition());
  MeArmPilot.runMacro("WAIT:1000");
  MeArmPilot.runMacro(MeArmPilot.closeClaw());
  MeArmPilot.runMacro("WAIT:500");
  MeArmPilot.runMacro(MeArmPilot.initStop());

  println("Bye.");
}

class HScrollbar {
  int swidth, sheight;    // width and height of bar
  float xpos, ypos;       // x and y position of bar
  float spos, newspos;    // x position of slider
  float sposMin, sposMax; // max and min values of slider
  int loose;              // how loose/heavy
  boolean over;           // is the mouse over the slider?
  boolean locked;
  float ratio;

  HScrollbar (float xp, float yp, int sw, int sh, int l) {
    swidth = sw;
    sheight = sh;
    int widthtoheight = sw - sh;
    ratio = (float)sw / (float)widthtoheight;
    xpos = xp;
    ypos = yp-sheight/2;
    spos = xpos + swidth/2 - sheight/2;
    newspos = spos;
    sposMin = xpos;
    sposMax = xpos + swidth - sheight;
    loose = l;
  }

  void update() {
    if (overEvent()) {
      over = true;
    } else {
      over = false;
    }
    if (mousePressed && over) {
      locked = true;
    }
    if (!mousePressed) {
      locked = false;
    }
    if (locked) {
      newspos = constrain(mouseX-sheight/2, sposMin, sposMax);
    }
    if (abs(newspos - spos) > 1) {
      spos = spos + (newspos-spos)/loose;
    }
  }

  float constrain(float val, float minv, float maxv) {
    return min(max(val, minv), maxv);
  }

  boolean overEvent() {
    if (mouseX > xpos && mouseX < xpos+swidth &&
       mouseY > ypos && mouseY < ypos+sheight) {
      return true;
    } else {
      return false;
    }
  }

  void display() {
    noStroke();
    fill(204, 128);
    rect(xpos, ypos, swidth, sheight);
    if (over || locked) {
      fill(0, 0, 0);
    } else {
      fill(102, 102, 102);
    }
    rect(spos, ypos, sheight, sheight);
  }

  float getPos() {
    // Convert spos to be values between
    // 0 and the total width of the scrollbar
    return spos * ratio;
  }
}
