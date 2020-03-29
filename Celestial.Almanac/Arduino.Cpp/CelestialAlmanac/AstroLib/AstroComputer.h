#ifndef ASTRO_COMPUTER
#define ASTRO_COMPUTER

#include "CelestStruct.h"

ComputedData * calculate(int year, int month, int day, int hour, int minute, int second, double delta_t);
char * outHA(double x);
char * outRA(double x);
char * outEoT(double x);

#endif
