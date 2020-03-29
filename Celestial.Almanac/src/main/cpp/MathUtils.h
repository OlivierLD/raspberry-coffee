#ifndef MATH_UTILS
#define MATH_UTILS

#define PI 3.14159265

class MathUtils {
  public:
    MathUtils();
    static double toRadians(double deg);
    static double toDegrees(double rad);
    static double sind(double x);
    static double cosd(double x);
    static double tand(double x);
    static double norm360Deg(double x);
    static double norm2PiRad(double x);
    static double cost(double x);
};    // Warning!!!! Do not forget the semicolon!!

#endif
