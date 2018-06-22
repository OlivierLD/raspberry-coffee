/*
 * Pressure Display, demo.
 */

int centerX = 200;
int centerY = 200;
int intRadius =  20;
int extRadius = 180;

void setup() {
  size(400, 400);
  frameRate(240); // Default is 60
  noStroke();
  noFill();
  textSize(10);
}

float pressure = 960.0f;
int sign = 1;

void draw() {
  background(0); // Black
  // Bounce on the edges
  if (pressure > 1040) {       // High
  	pressure = 1040;
    sign = -1;
  } else if (pressure < 960) { // Low
  	pressure = 960;
    sign = 1;
  }
  float increment = 0;
  if (!mousePressed) {
    increment = 0.01;
  } else {
//  println(String.format("Mouse: X=%d, Y=%d", mouseX, mouseY));
    int diffX = mouseX - centerX;
    increment = diffX / 100f;
  }
  pressure += (sign * increment);

  textSize(10);
  fill(255);
  text(String.format("%05.2f hPa", pressure), 5, 12);

  // The ticks
  stroke(255, 204, 0); // Goldish
  for (int p=960; p<=1040; p+=1) {
    strokeWeight((p % 10 == 0 ? 3 : 1));
    float _p = ((p - 1000f) * (260f / 80f)) - 90f;
    line(centerX + ((extRadius + 5) * (float)Math.cos(Math.toRadians(_p))),
         centerY + ((extRadius + 5) * (float)Math.sin(Math.toRadians(_p))),
         centerX + ((extRadius - (p % 10 == 0 ? 20 : 10)) * (float)Math.cos(Math.toRadians(_p))),
         centerY + ((extRadius - (p % 10 == 0 ? 20 : 10)) * (float)Math.sin(Math.toRadians(_p))));
  }
  strokeWeight(1);
  stroke(255);
  // 1000 is when the hand is centered, vertical, degree 0.
  // Drawing the hand
  float _pressure = ((pressure - 1000f) * (260f / 80f)) - 90f;
//println(String.format("Pressure: %.02f, Angle: %f", pressure, _pressure));
  fill(255); // White
  triangle(centerX,
           centerY,
           (float)(centerX + (extRadius * Math.cos(Math.toRadians(_pressure)))),
           (float)(centerY + (extRadius * Math.sin(Math.toRadians(_pressure)))),
           (float)(centerX + (intRadius * Math.cos(Math.toRadians(_pressure + 45)))),
           (float)(centerY + (intRadius * Math.sin(Math.toRadians(_pressure + 45)))));
  fill(128); // Gray
  triangle(centerX,
           centerY,
           (float)(centerX + (extRadius * Math.cos(Math.toRadians(_pressure)))),
           (float)(centerY + (extRadius * Math.sin(Math.toRadians(_pressure)))),
           (float)(centerX + (intRadius * Math.cos(Math.toRadians(_pressure - 45)))),
           (float)(centerY + (intRadius * Math.sin(Math.toRadians(_pressure - 45)))));

  // knob
  fill(180); // Grey'ish
  ellipse(centerX, centerY, intRadius * 2, intRadius * 2);

  // print the Value
  textSize(32);
  fill(255, 204, 0); // Gold'ish
  String str = String.format("%05.2f hPa", pressure);
  float strW = textWidth(str);
  text(str, (width / 2) - (strW / 2), height - 100);
}
