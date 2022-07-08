#ifndef ASTRO_COMPUTER
#define ASTRO_COMPUTER

#include "CelestStruct.h"

ComputedData * calculate(int year, int month, int day, int hour, int minute, int second, double delta_t);
char * outHA(double x, char * data);
char * outRA(double x, char * data);
char * outEoT(double x, char * data);
char * outDec(double x, char * data);
char * outSdHp(double x, char * data);
char * outECL(double x, char * data);

double calculateDeltaT(int year, int month);

#endif
