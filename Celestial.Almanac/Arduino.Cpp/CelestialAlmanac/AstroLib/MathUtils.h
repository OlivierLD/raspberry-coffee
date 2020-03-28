#ifndef MATH_UTILS
#define MATH_UTILS

#define PI 3.14159265

class MathUtils {
  public:
    MathUtils();
    static float toRadians(float deg);
    static float toDegrees(float rad);
    static float sind(float x);
    static float cosd(float x);
    static float tand(float x);
    static float norm360Deg(float x);
    static float norm2PiRad(float x);
    static float cost(float x);
};    // Warning!!!! Do not forget the semicolon!!

#endif
