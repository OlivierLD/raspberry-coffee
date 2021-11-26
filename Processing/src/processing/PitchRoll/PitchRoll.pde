import i2c.sensor.LSM303;
/**
 * Using Sketch > Add File..., select I2C.SPI/build/libs/I2C-SPI-1.0-all.jar
 */

// These are the points drawing the boat.
float[][] keel = new float[][] {
  { -3, 0, 1 },
  { -2, 0, 0 },
  { -1, 0, -0.6 },
  {  0, 0, -0.75 },
  {  1, 0, -0.75 },
  {  2, 0, -0.5 },
  {  3, 0, -0.3 }
};
float[][] chine = new float[][] {
  { -3, 0, 1 },
  { -2, 0.6, 0.4 },
  { -1, 1.1, 0 },
  {  0, 1.3, -0.2 },
  {  1, 1.25, -0.2 },
  {  2, 1.1, -0.1 },
  {  3, 0.8, 0.2 },
};
float[][] rail = new float[][] {
  { -3, 0, 1 },
  { -2, 0.75, 0.95 },
  { -1, 1.2, 0.9 },
  {  0, 1.47, 0.9 },
  {  1, 1.5, 0.9 },
  {  2, 1.425, 0.93 },
  {  3, 1.2, 1 }
};

int pts = keel.length; // TODO Verify they all have the same length

// for shaded or wireframe rendering
boolean isWireFrame = false;

boolean withSensor = false;

LSM303 lsm303;

void setup(){
  size(640, 360, P3D);
  if (withSensor) {
    try {
      lsm303 = new LSM303(LSM303.EnabledFeature.ACCELEROMETER);
    } catch (java.lang.Exception ex) {
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
float pitch = 0;
float roll = 0;

int pitchSign = 1;
int rollSign = 1;

void draw(){
  background(50, 64, 42);

  // basic lighting setup
  lights();
  // 2 rendering styles
  // wireframe or solid
  if (isWireFrame) {
    stroke(255, 255, 150);
    noFill();
  } else {
    noStroke();
    fill(150, 195, 125);
  }
  // center
  translate(width/2, height/2, -100);

  if (withSensor) {
    roll = (float)lsm303.getRoll();
    pitch = (float)lsm303.getPitch();
  } else {
    roll += (rollSign * 0.1);
    pitch += (pitchSign * 0.05);
    if (roll > 15 || roll < -15) {
      rollSign *= -1;
    }
    if (pitch > 15 || pitch < -15) {
      pitchSign *= -1;
    }
  }

// Heading given by the mouse (left-right)
  float newXmag = mouseX / float(width) * 180;
  heading = - newXmag;

  rotateX((roll * PI / 180) + (PI / 2)); // Roll PI/2: 0 Roll
  rotateY(pitch * (PI / 180)); // Pitch
  rotateZ(heading * (PI / 180)); // Heading. -PI/2: facing

  // Draw boat shape. Still a work in progress.
  // Good doc at https://processing.org/reference/beginShape_.html
  scale(75);
  pushMatrix();
  beginShape(QUAD_STRIP);
  // One side
  for (int i=0; i<pts; i++) {
    vertex(keel[i][0], keel[i][1], keel[i][2]);
    vertex(chine[i][0], chine[i][1], chine[i][2]);
  }
  for (int i=0; i<pts; i++) {
    vertex(chine[i][0], chine[i][1], chine[i][2]);
    vertex(rail[i][0], rail[i][1], rail[i][2]);
  }
  // Other side
  for (int i=0; i<pts; i++) {
    vertex(keel[i][0], -keel[i][1], keel[i][2]);
    vertex(chine[i][0], -chine[i][1], chine[i][2]);
  }
  for (int i=0; i<pts; i++) {
    vertex(chine[i][0], -chine[i][1], chine[i][2]);
    vertex(rail[i][0], -rail[i][1], rail[i][2]);
  }
  endShape();
  popMatrix();

  pushMatrix();
  // Transom
  beginShape();
  vertex(keel[pts-1][0], keel[pts-1][1], keel[pts-1][2]);
  vertex(chine[pts-1][0], chine[pts-1][1], chine[pts-1][2]);
  vertex(rail[pts-1][0], rail[pts-1][1], rail[pts-1][2]);

  vertex(rail[pts-1][0], -rail[pts-1][1], rail[pts-1][2]);
  vertex(chine[pts-1][0], -chine[pts-1][1], chine[pts-1][2]);

  endShape();
  popMatrix();

  // Deck
  pushMatrix();
  beginShape();
  for (int i=0; i<pts; i++) {
    vertex(rail[i][0], rail[i][1], rail[i][2]);
  }
  for (int i=pts-1; i>=0; i--) {
    vertex(rail[i][0], -rail[i][1], rail[i][2]);
  }
  endShape();
  popMatrix();
}
