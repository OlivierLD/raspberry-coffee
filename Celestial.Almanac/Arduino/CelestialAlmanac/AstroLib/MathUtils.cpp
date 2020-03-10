// #include <Arduino.h>
#include <stdio.h> 
#include <math.h> 

#include "MathUtils.h"

MathUtils::MathUtils() {}

float MathUtils::toRadians(float deg) {
  return deg * PI / 180;
//  return (deg * 71) / 4068;
}

float MathUtils::toDegrees(float rad) {
  return rad * 180 / PI;
}

// Sine of angles in degrees
float MathUtils::sind(float x) {
  return sin(toRadians(x));
}

// Cosine of angles in degrees
float MathUtils::cosd(float x) {
  return cos(toRadians(x));
}

// Tangent of angles in degrees
float MathUtils::tand(float x) {
  return tan(toRadians(x));
}

// Normalize large angles
// Degrees
float MathUtils::norm360Deg(float x) {
  while (x < 0) {
    x += 360;
  }
  while (x > 360) {
    x -= 360;
  }
  return x;
}

// Radians
float MathUtils::norm2PiRad(float x) {
  while (x < 0) {
    x += (2 * PI);
  }
  while (x > (2 * PI)) {
    x -= (2 * PI);
  }
  return x;
}

// Cosine of normalized angle (in radians)
float MathUtils::cost(float x) {
  return cos(norm2PiRad(x));
}
