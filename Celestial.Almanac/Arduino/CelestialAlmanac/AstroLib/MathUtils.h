#ifndef MATH_UTILS
#define MATH_UTILS

#define PI 3.14159265

class MathUtils {
  public:
    MathUtils();
    float toRadians(float deg);
    float toDegrees(float rad);
    float sind(float x);
    float cosd(float x);
    float tand(float x);
    float norm360Deg(float x);
    float norm2PiRad(float x);
    float cost(float x);
};    // Warning!!!! Do not forget the semicolon!!

#endif
