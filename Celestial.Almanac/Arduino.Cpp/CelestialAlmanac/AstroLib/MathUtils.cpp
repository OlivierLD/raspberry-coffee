// #include <Arduino.h>
#include <stdio.h>
#include <math.h>

#include "MathUtils.h"

MathUtils::MathUtils() {}

double MathUtils::toRadians(double deg) {
  return deg * PI / 180;
//  return (deg * 71) / 4068;
}

double MathUtils::toDegrees(double rad) {
  return rad * 180 / PI;
}

// Sine of angles in degrees
double MathUtils::sind(double x) {
  return sin(toRadians(x));
}

// Cosine of angles in degrees
double MathUtils::cosd(double x) {
  return cos(toRadians(x));
}

// Tangent of angles in degrees
double MathUtils::tand(double x) {
  return tan(toRadians(x));
}

// Normalize large angles
// Degrees
double MathUtils::norm360Deg(double x) {
  while (x < 0) {
    x += 360;
  }
  while (x > 360) {
    x -= 360;
  }
  return x;
}

// Radians
double MathUtils::norm2PiRad(double x) {
  while (x < 0) {
    x += (2 * PI);
  }
  while (x > (2 * PI)) {
    x -= (2 * PI);
  }
  return x;
}

// Cosine of normalized angle (in radians)
double MathUtils::cost(double x) {
  return cos(norm2PiRad(x));
}
