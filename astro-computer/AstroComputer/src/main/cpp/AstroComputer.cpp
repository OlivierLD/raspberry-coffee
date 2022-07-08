#include <iostream>
#include <math.h>
#include <stdio.h>
#include <string.h>

#include "CelestStruct.h"

#include "MathUtils.h"
#include "Earth.h"
#include "Venus.h"
#include "Mars.h"
#include "Jupiter.h"
#include "Saturn.h"

#include "AstroComputer.h"

#define DEBUG false

void calculateJulianDate(int year, int month, int day, int hour, int minute, int second, double delta_t);
void calculateNutation();
void calculateAberration();
void calculateAries();
void calculateSun();
void calculateVenus();
void calculateMars();
void calculateJupiter();
void calculateSaturn();
void calculateMoon();
void calculatePolaris();
void calculateMoonPhase();
void calculateWeekDay();

double calculateDeltaT(int year, int month);

// const MathUtils cuMu;
// const Earth earth; // No (), this would confuse the compiler.

bool isLeapYear(int year) {
  bool ly = false;
  if (year % 4 == 0) {
    ly = true;
  }
  if (year % 100 == 0) {
    ly = false;
  }
  if (year % 400 == 0) {
    ly = true;
  }
  return ly;
}

char dataBuffer[128];

// Output Sidereal Time
char * outSideralTime(double x, char * data) {
	double GMSTdecimal = x / 15;
	int GMSTh = floor(GMSTdecimal);
	double GMSTmdecimal = 60 * (GMSTdecimal - GMSTh);
	int GMSTm = floor(GMSTmdecimal);
	double GMSTsdecimal = 60 * (GMSTmdecimal - GMSTm);
	int GMSTs = round(1000 * GMSTsdecimal) / 1000;
	sprintf(data, "%dh %dm %ds", GMSTh, GMSTm, GMSTs);
	return data;
}

// Output Hour Angles
char * outHA(double x, char * data) {
  if (DEBUG) {
    fprintf(stdout, "Output HA for %f\n", x);
  }
  int GHAdeg = floor(x);
  int GHAmin = floor(60 * (x - GHAdeg));
  int GHAsec = round(3600 * ((double)(x - GHAdeg) - ((double)GHAmin / 60)));
  if (GHAsec == 60) {
    GHAsec = 0;
    GHAmin += 1;
  }
  if (GHAmin == 60) {
    GHAmin = 0;
    GHAdeg += 1;
  }
  sprintf(data, "%d° %d' %d\"", GHAdeg, GHAmin, GHAsec);
  return data;
}

// Output Right ascension
char * outRA(double x, char * data) {
  if (DEBUG) {
    fprintf(stdout, "Output RA for %f\n", x);
  }
  double t = x / 15;
  int RAh = floor(t);
  int RAmin = floor(60 * (t -RAh));
  int RAsec = round(10 * (3600 * ((t - (double)RAh) - ((double)RAmin / 60)))) / 10;
  if (RAsec == 60) {
    RAsec = 0;
    RAmin += 1;
  }

  if (RAmin == 60) {
    RAmin = 0;
    RAh += 1;
  }
  sprintf(data, "%dh %dm %ds", RAh, RAmin, RAsec);
  return data;
}

char * outEoT(double x, char * data) {
    double EoT = abs(x);
    int EOTmin = floor(EoT);
    int EOTsec = round(600 * (EoT - EOTmin)) / 10;

    if (EOTmin == 0) {
      sprintf(data, "%s %ds", (x < 0 ? "-" : "+"), EOTsec);
    } else {
      sprintf(data, "%s %dm %ds", (x < 0 ? "-" : "+"), EOTmin, EOTsec);
    }
    return data;
}

char * outDec(double x, char * data) {
	double DEC = abs(x);
	int DECdeg = floor(DEC);
	int DECmin = floor(60 * (DEC - DECdeg));
	int DECsec = round(3600 * ((DEC - DECdeg) - ((double)DECmin / 60)));
	if (DECsec == 60) {
		DECsec = 0;
		DECmin += 1;
	}
	if (DECmin == 60) {
		DECmin = 0;
		DECdeg += 1;
	}
	sprintf(data, "%s %d°%d'%d\"", (x < 0 ? "S" : "N"), DECdeg, DECmin, DECsec);
	return data;
}

// Output SD and HP
char * outSdHp(double x, char * data) {
	x = round(10 * x) / 10;

	sprintf(data, "%f\"", x);
	return data;
}

// Output Obliquity of Ecliptic
char * outECL(double x, char * data) {
	int ECLdeg = floor(x);
	int ECLmin = floor(60 * (x - ECLdeg));
	double ECLsec = round(3600000 * ((x - ECLdeg) - ((double)ECLmin / 60))) / 1000;
	if (ECLsec == 60) {
		ECLsec = 0;
		ECLmin += 1;
	}
	if (ECLmin == 60) {
		ECLmin = 0;
		ECLdeg += 1;
	}
	sprintf(data, "%d° %d' %f\"", ECLdeg, ECLmin, ECLsec);
	return data;
}


// Placeholder for all computed data
ComputedData * data = (ComputedData *) calloc(1, sizeof(ComputedData));

/**
 * Main function
 * @param year Number, UTC year
 * @param month Number, UTC month, [1..12]
 * @param day Number, UTC day of month
 * @param hour Number, UTC hour
 * @param minute Number, UTC minute
 * @param second Number, UTC second
 * @param delta_t Number, DeltaT
 */
ComputedData * calculate(int year, int month, int day, int hour, int minute, int second, double delta_t) {
  calculateJulianDate(year, month, day, hour, minute, second, delta_t);
  calculateNutation();
  calculateAberration();
  calculateAries();
  calculateSun();
  calculateVenus();
  calculateMars();
  calculateJupiter();
  calculateSaturn();
  calculateMoon();
  calculatePolaris();
  calculateMoonPhase();
  calculateWeekDay();

  return data; // declared above
}

/**
 * Input data conversion and reworking
 * All data are UTC data (except deltaT)
 **
 * @param year Number, UTC year
 * @param month Number, UTC month, [1..12]
 * @param day Number, UTC day of month
 * @param hour Number, UTC hour
 * @param minute Number, UTC minute
 * @param second Number, UTC second
 * @param delta_t Number, DeltaT
 */
void calculateJulianDate(int year, int month, int day, int hour, int minute, int second, double delta_t) {

  data->dayFraction = ((double)hour + (double)minute / 60 + (double)second / 3600) / 24;
  if (data->dayFraction < 0 || data->dayFraction > 1) {
    fprintf(stdout, "Time out of range! Restart calculation.");
    return;
  }
  data->deltaT = delta_t;

  // Calculating Julian date, century, and millennium

  // Julian date (UT1)
  if (month <= 2) {
    year -= 1;
    month += 12;
  }
  double A = floor((double)year / 100);
  double B = 2 - A + floor(A / 4);
  data->JD0h = floor(365.25 * ((double)year + 4716)) + floor(30.6001 * ((double)month + 1)) + (double)day + B - 1524.5;

  data->JD = data->JD0h + data->dayFraction;

  // Julian centuries (UT1) from 2000 January 0.5
  data->T = (data->JD - 2451545) / 36525;
  data->T2 = data->T * data->T;
  data->T3 = data->T * data->T2;
  data->T4 = data->T * data->T3;
  data->T5 = data->T * data->T4;

  // Julian ephemeris date (TDT)
  data->JDE = data->JD + data->deltaT / 86400;

  // Julian centuries (TDT) from 2000 January 0.5
  data->TE = (data->JDE - 2451545) / 36525;
  data->TE2 = data->TE * data->TE;
  data->TE3 = data->TE * data->TE2;
  data->TE4 = data->TE * data->TE3;
  data->TE5 = data->TE * data->TE4;

  // Julian millenniums (TDT) from 2000 January 0.5
  data->Tau = 0.1 * data->TE;
  data->Tau2 = data->Tau * data->Tau;
  data->Tau3 = data->Tau * data->Tau2;
  data->Tau4 = data->Tau * data->Tau3;
  data->Tau5 = data->Tau * data->Tau4;

  if (DEBUG) {
    fprintf(stdout, "DayFraction: %f, JD0h: %f, JD: %f\n", data->dayFraction, data->JD0h, data->JD);
  }
}

// Nutation, obliquity of the ecliptic
void calculateNutation() {
  // IAU 1980 calculateNutation theory:

  // Mean anomaly of the Moon
  double Mm = 134.962981389 + 198.867398056 * data->TE + MathUtils::norm360Deg(477000 * data->TE) + 0.008697222222 * data->TE2 + data->TE3 / 56250;

  // Mean anomaly of the Sun
  double M = 357.527723333 + 359.05034 * data->TE + MathUtils::norm360Deg(35640 * data->TE) - 0.0001602777778 * data->TE2 - data->TE3 / 300000;

  // Mean distance of the Moon from ascending node
  double F = 93.271910277 + 82.017538055 * data->TE + MathUtils::norm360Deg(483120 * data->TE) - 0.0036825 * data->TE2 + data->TE3 / 327272.7273;

  // Mean elongation of the Moon
  double D = 297.850363055 + 307.11148 * data->TE + MathUtils::norm360Deg(444960 * data->TE) - 0.001914166667 * data->TE2 + data->TE3 / 189473.6842;

  // Longitude of the ascending node of the Moon
  double omega = 125.044522222 - 134.136260833 * data->TE - MathUtils::norm360Deg(1800 * data->TE) + 0.002070833333 * data->TE2 + data->TE3 / 450000;

  // Periodic terms for nutation
  const int nutRows = 106,
            nutColumns = 9;
  const double nut[nutRows][nutColumns] = {
    {  0,  0,  0,  0,  1, -171996, -174.2,  92025,  8.9 },
    {  0,  0,  2, -2,  2,  -13187,   -1.6,   5736, -3.1 },
    {  0,  0,  2,  0,  2,   -2274,   -0.2,    977, -0.5 },
    {  0,  0,  0,  0,  2,    2062,    0.2,   -895,  0.5 },
    {  0, -1,  0,  0,  0,   -1426,    3.4,     54, -0.1 },
    {  1,  0,  0,  0,  0,     712,    0.1,     -7,  0.0 },
    {  0,  1,  2, -2,  2,    -517,    1.2,    224, -0.6 },
    {  0,  0,  2,  0,  1,    -386,   -0.4,    200,  0.0 },
    {  1,  0,  2,  0,  2,    -301,    0.0,    129, -0.1 },
    {  0, -1,  2, -2,  2,     217,   -0.5,    -95,  0.3 },
    { -1,  0,  0,  2,  0,     158,    0.0,     -1,  0.0 },
    {  0,  0,  2, -2,  1,     129,    0.1,    -70,  0.0 },
    { -1,  0,  2,  0,  2,     123,    0.0,    -53,  0.0 },
    {  1,  0,  0,  0,  1,      63,    0.1,    -33,  0.0 },
    {  0,  0,  0,  2,  0,      63,    0.0,     -2,  0.0 },
    { -1,  0,  2,  2,  2,     -59,    0.0,     26,  0.0 },
    { -1,  0,  0,  0,  1,     -58,   -0.1,     32,  0.0 },
    {  1,  0,  2,  0,  1,     -51,    0.0,     27,  0.0 },
    { -2,  0,  0,  2,  0,     -48,    0.0,      1,  0.0 },
    { -2,  0,  2,  0,  1,      46,    0.0,    -24,  0.0 },
    {  0,  0,  2,  2,  2,     -38,    0.0,     16,  0.0 },
    {  2,  0,  2,  0,  2,     -31,    0.0,     13,  0.0 },
    {  2,  0,  0,  0,  0,      29,    0.0,     -1,  0.0 },
    {  1,  0,  2, -2,  2,      29,    0.0,    -12,  0.0 },
    {  0,  0,  2,  0,  0,      26,    0.0,     -1,  0.0 },
    {  0,  0,  2, -2,  0,     -22,    0.0,      0,  0.0 },
    { -1,  0,  2,  0,  1,      21,    0.0,    -10,  0.0 },
    {  0,  2,  0,  0,  0,      17,   -0.1,      0,  0.0 },
    {  0,  2,  2, -2,  2,     -16,    0.1,      7,  0.0 },
    { -1,  0,  0,  2,  1,      16,    0.0,     -8,  0.0 },
    {  0,  1,  0,  0,  1,     -15,    0.0,      9,  0.0 },
    {  1,  0,  0, -2,  1,     -13,    0.0,      7,  0.0 },
    {  0, -1,  0,  0,  1,     -12,    0.0,      6,  0.0 },
    {  2,  0, -2,  0,  0,      11,    0.0,      0,  0.0 },
    { -1,  0,  2,  2,  1,     -10,    0.0,      5,  0.0 },
    {  1,  0,  2,  2,  2,      -8,    0.0,      3,  0.0 },
    {  0, -1,  2,  0,  2,      -7,    0.0,      3,  0.0 },
    {  0,  0,  2,  2,  1,      -7,    0.0,      3,  0.0 },
    {  1,  1,  0, -2,  0,      -7,    0.0,      0,  0.0 },
    {  0,  1,  2,  0,  2,       7,    0.0,     -3,  0.0 },
    { -2,  0,  0,  2,  1,      -6,    0.0,      3,  0.0 },
    {  0,  0,  0,  2,  1,      -6,    0.0,      3,  0.0 },
    {  2,  0,  2, -2,  2,       6,    0.0,     -3,  0.0 },
    {  1,  0,  0,  2,  0,       6,    0.0,      0,  0.0 },
    {  1,  0,  2, -2,  1,       6,    0.0,     -3,  0.0 },
    {  0,  0,  0, -2,  1,      -5,    0.0,      3,  0.0 },
    {  0, -1,  2, -2,  1,      -5,    0.0,      3,  0.0 },
    {  2,  0,  2,  0,  1,      -5,    0.0,      3,  0.0 },
    {  1, -1,  0,  0,  0,       5,    0.0,      0,  0.0 },
    {  1,  0,  0, -1,  0,      -4,    0.0,      0,  0.0 },
    {  0,  0,  0,  1,  0,      -4,    0.0,      0,  0.0 },
    {  0,  1,  0, -2,  0,      -4,    0.0,      0,  0.0 },
    {  1,  0, -2,  0,  0,       4,    0.0,      0,  0.0 },
    {  2,  0,  0, -2,  1,       4,    0.0,     -2,  0.0 },
    {  0,  1,  2, -2,  1,       4,    0.0,     -2,  0.0 },
    {  1,  1,  0,  0,  0,      -3,    0.0,      0,  0.0 },
    {  1, -1,  0, -1,  0,      -3,    0.0,      0,  0.0 },
    { -1, -1,  2,  2,  2,      -3,    0.0,      1,  0.0 },
    {  0, -1,  2,  2,  2,      -3,    0.0,      1,  0.0 },
    {  1, -1,  2,  0,  2,      -3,    0.0,      1,  0.0 },
    {  3,  0,  2,  0,  2,      -3,    0.0,      1,  0.0 },
    { -2,  0,  2,  0,  2,      -3,    0.0,      1,  0.0 },
    {  1,  0,  2,  0,  0,       3,    0.0,      0,  0.0 },
    { -1,  0,  2,  4,  2,      -2,    0.0,      1,  0.0 },
    {  1,  0,  0,  0,  2,      -2,    0.0,      1,  0.0 },
    { -1,  0,  2, -2,  1,      -2,    0.0,      1,  0.0 },
    {  0, -2,  2, -2,  1,      -2,    0.0,      1,  0.0 },
    { -2,  0,  0,  0,  1,      -2,    0.0,      1,  0.0 },
    {  2,  0,  0,  0,  1,       2,    0.0,     -1,  0.0 },
    {  3,  0,  0,  0,  0,       2,    0.0,      0,  0.0 },
    {  1,  1,  2,  0,  2,       2,    0.0,     -1,  0.0 },
    {  0,  0,  2,  1,  2,       2,    0.0,     -1,  0.0 },
    {  1,  0,  0,  2,  1,      -1,    0.0,      0,  0.0 },
    {  1,  0,  2,  2,  1,      -1,    0.0,      1,  0.0 },
    {  1,  1,  0, -2,  1,      -1,    0.0,      0,  0.0 },
    {  0,  1,  0,  2,  0,      -1,    0.0,      0,  0.0 },
    {  0,  1,  2, -2,  0,      -1,    0.0,      0,  0.0 },
    {  0,  1, -2,  2,  0,      -1,    0.0,      0,  0.0 },
    {  1,  0, -2,  2,  0,      -1,    0.0,      0,  0.0 },
    {  1,  0, -2, -2,  0,      -1,    0.0,      0,  0.0 },
    {  1,  0,  2, -2,  0,      -1,    0.0,      0,  0.0 },
    {  1,  0,  0, -4,  0,      -1,    0.0,      0,  0.0 },
    {  2,  0,  0, -4,  0,      -1,    0.0,      0,  0.0 },
    {  0,  0,  2,  4,  2,      -1,    0.0,      0,  0.0 },
    {  0,  0,  2, -1,  2,      -1,    0.0,      0,  0.0 },
    { -2,  0,  2,  4,  2,      -1,    0.0,      1,  0.0 },
    {  2,  0,  2,  2,  2,      -1,    0.0,      0,  0.0 },
    {  0, -1,  2,  0,  1,      -1,    0.0,      0,  0.0 },
    {  0,  0, -2,  0,  1,      -1,    0.0,      0,  0.0 },
    {  0,  0,  4, -2,  2,       1,    0.0,      0,  0.0 },
    {  0,  1,  0,  0,  2,       1,    0.0,      0,  0.0 },
    {  1,  1,  2, -2,  2,       1,    0.0,     -1,  0.0 },
    {  3,  0,  2, -2,  2,       1,    0.0,      0,  0.0 },
    { -2,  0,  2,  2,  2,       1,    0.0,     -1,  0.0 },
    { -1,  0,  0,  0,  2,       1,    0.0,     -1,  0.0 },
    {  0,  0, -2,  2,  1,       1,    0.0,      0,  0.0 },
    {  0,  1,  2,  0,  1,       1,    0.0,      0,  0.0 },
    { -1,  0,  4,  0,  2,       1,    0.0,      0,  0.0 },
    {  2,  1,  0, -2,  0,       1,    0.0,      0,  0.0 },
    {  2,  0,  0,  2,  0,       1,    0.0,      0,  0.0 },
    {  2,  0,  2, -2,  1,       1,    0.0,     -1,  0.0 },
    {  2,  0, -2,  0,  1,       1,    0.0,      0,  0.0 },
    {  1, -1,  0, -2,  0,       1,    0.0,      0,  0.0 },
    { -1,  0,  0,  1,  1,       1,    0.0,      0,  0.0 },
    { -1, -1,  0,  2,  1,       1,    0.0,      0,  0.0 },
    {  0,  1,  0,  1,  0,       1,    0.0,      0,  0.0 }
  };

  // Reading periodic terms
  double fMm, fM, fF, fD, f_omega, dp = 0, de = 0;
  int x = 0;
  while (x < nutRows) {
    fMm = nut[x][0];
    fM = nut[x][1];
    fF = nut[x][2];
    fD = nut[x][3];
    f_omega = nut[x][4];
    dp += (nut[x][5] + data->TE * nut[x][6]) * MathUtils::sind(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega);
    de += (nut[x][7] + data->TE * nut[x][8]) * MathUtils::cosd(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega);
    x++;
  }

  // Corrections (Herring, 1987)
  const int corrRows = 4,
            corrColumns = 9;
  const double corr[corrRows][corrColumns] = {
    { 0, 0, 0, 0, 1,-725, 417, 213, 224 },
    { 0, 1, 0, 0, 0, 523,  61, 208, -24 },
    { 0, 0, 2,-2, 2, 102,-118, -41, -47 },
    { 0, 0, 2, 0, 2, -81,   0,  32,   0 }
  };
  x = 0;
  while (x < corrRows) {
    fMm = corr[x][0];
    fM = corr[x][1];
    fF = corr[x][2];
    fD = corr[x][3];
    f_omega = corr[x][4];
    dp += 0.1 * (corr[x][5] * MathUtils::sind(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega) + corr[x][6] * MathUtils::cosd(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega));
    de += 0.1 * (corr[x][7] * MathUtils::cosd(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega) + corr[x][8] * MathUtils::sind(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega));
    x++;
  }


  // calculateNutation in longitude
  data->deltaPsi = dp / 36000000;

  // calculateNutation in obliquity
  data->deltaEps = de / 36000000;

  // Mean obliquity of the ecliptic
  data->eps0 = (84381.448 - 46.815 * data->TE - 0.00059 * data->TE2 + 0.001813 * data->TE3) / 3600;

  // True obliquity of the ecliptic
  data->eps = data->eps0 + data->deltaEps;
}

void calculateAberration() {
  data->kappa = MathUtils::toRadians(20.49552 / 3600);
  data->pi0 = MathUtils::toRadians(102.93735 + 1.71953 * data->TE + 0.00046 * data->TE2);
  data->e = 0.016708617 - 0.000042037 * data->TE - 0.0000001236 * data->TE2;
}

// GHA Aries, GAST, GMST, equation of the equinoxes
void calculateAries() {
	// Mean GHA Aries
	data->GHAAmean = MathUtils::norm360Deg(280.46061837 + 360.98564736629 * (data->JD - 2451545) + 0.000387933 * data->T2 - data->T3 / 38710000);

	// GMST
	strcpy(data->SidTm, outSideralTime(data->GHAAmean, dataBuffer));

	// True GHA Aries
	data->GHAAtrue = MathUtils::norm360Deg(data->GHAAmean + data->deltaPsi * MathUtils::cosd(data->eps));

	// GAST
  strcpy(data->SidTa, outSideralTime(data->GHAAtrue, dataBuffer));

	// Equation of the equinoxes
	data->EoE = 240 * data->deltaPsi * MathUtils::cosd(data->eps);
	data->EoEout = round(1000 * data->EoE) / 1000;
	// EoEout = " " + EoEout + "s";
}

// Calculations for the Sun
void calculateSun() {
	// Mean longitude of the Sun. TODO Not used?
	double Lsun_mean = MathUtils::norm360Deg(280.4664567 + 360007.6982779 * data->Tau + 0.03032028 * data->Tau2 + data->Tau3 / 49931 - data->Tau4 / 15299 - data->Tau5 / 1988000);

	// Heliocentric longitude of the Earth
    data->Le = Earth::lEarth(data->Tau);

	// Geocentric longitude of the Sun
	data->Lsun_true = MathUtils::norm360Deg(data->Le + 180 - 0.000025);

	// Heliocentric latitude of Earth
	data->Be = Earth::bEarth(data->Tau);

	// Geocentric latitude of the Sun
	double beta = MathUtils::norm360Deg(- data->Be);

	// Corrections
	data->Lsun_prime = MathUtils::norm360Deg(data->Le + 180 - 1.397 * data->TE - 0.00031 * data->TE2);

	beta = beta + 0.000011 * (MathUtils::cosd(data->Lsun_prime) - MathUtils::sind(data->Lsun_prime));

	// Distance Earth-Sun
	data->Re = Earth::rEarth(data->Tau);
	data->dES = 149597870.691 * data->Re;

	// Apparent longitude of the Sun
	data->lambdaSun = MathUtils::norm360Deg(data->Lsun_true + data->deltaPsi - 0.005691611 / data->Re);

	// Right ascension of the Sun, apparent
	data->RASun = MathUtils::toDegrees(MathUtils::norm2PiRad(atan2((MathUtils::sind(data->lambdaSun) * MathUtils::cosd(data->eps) - MathUtils::tand(beta) * MathUtils::sind(data->eps)), MathUtils::cosd(data->lambdaSun))));

	// Declination of the Sun, apparent
	data->DECSun = MathUtils::toDegrees(asin(MathUtils::sind(beta) * MathUtils::cosd(data->eps) + MathUtils::cosd(beta) * MathUtils::sind(data->eps) * MathUtils::sind(data->lambdaSun)));

	// GHA of the Sun
	data->GHASun = MathUtils::norm360Deg(data->GHAAtrue - data->RASun);

	// Semi-diameter of the Sun
	data->SDSun = 959.63 / data->Re;

	//Horizontal parallax of the Sun
	data->HPSun = 8.794 / data->Re;

	// Equation of time
	// EoT = 4*(Lsun_mean-0.0057183-0.0008-RASun+deltaPsi*cosd(eps));
	data->EoT = 4 * data->GHASun + 720 - 1440 * data->dayFraction;
	if (data->EoT > 20) {
		data->EoT -= 1440;
	}
	if (data->EoT < -20) {
		data->EoT += 1440;
	}
}

// Calculations for Venus
void calculateVenus() {
	// Heliocentric spherical coordinates
	double L = Venus::lVenus(data->Tau);
	double B = Venus::bVenus(data->Tau);
	double R = Venus::rVenus(data->Tau);

	// Rectangular coordinates
	double x = R * MathUtils::cosd(B) * MathUtils::cosd(L) - data->Re * MathUtils::cosd(data->Be) * MathUtils::cosd(data->Le);
	double y = R * MathUtils::cosd(B) * MathUtils::sind(L) - data->Re * MathUtils::cosd(data->Be) * MathUtils::sind(data->Le);
	double z = R * MathUtils::sind(B) - data->Re * MathUtils::sind(data->Be);

	// Geocentric spherical coordinates
	double lambda = atan2(y, x);
	double beta = atan(z / sqrt(x * x + y * y));

	// Distance from Earth / light time
	double d = sqrt(x * x + y * y + z * z);
	double lt = 0.0057755183 * d;

	// Time correction
	double Tau_corr = (data->JDE - lt - 2451545) / 365250;

	// Coordinates corrected for light time
	L = Venus::lVenus(Tau_corr);
	B = Venus::bVenus(Tau_corr);
	R = Venus::rVenus(Tau_corr);
	x = R * MathUtils::cosd(B) * MathUtils::cosd(L) - data->Re * MathUtils::cosd(data->Be) * MathUtils::cosd(data->Le);
	y = R * MathUtils::cosd(B) * MathUtils::sind(L) - data->Re * MathUtils::cosd(data->Be) * MathUtils::sind(data->Le);
	z = R * MathUtils::sind(B) - data->Re * MathUtils::sind(data->Be);

	lambda = atan2(y, x);
	beta = atan(z / sqrt(x * x + y * y));

	// aberration
	double dlambda = (data->e * data->kappa * cos(data->pi0 - lambda) - data->kappa * cos(MathUtils::toRadians(data->Lsun_true) - lambda)) / cos(beta);
	double dbeta = -data->kappa * sin(beta) * (sin(MathUtils::toRadians(data->Lsun_true) - lambda) - data->e * sin(data->pi0 - lambda));

	lambda += dlambda;
	beta += dbeta;

	// FK5
	double lambda_prime = lambda - MathUtils::toRadians(1.397) * data->TE - MathUtils::toRadians(0.00031) * data->TE2;

	dlambda = MathUtils::toRadians(-0.09033) / 3600 + MathUtils::toRadians(0.03916) / 3600 * (cos(lambda_prime) + sin(lambda_prime)) * tan(beta);
	dbeta = MathUtils::toRadians(0.03916) / 3600 * (cos(lambda_prime) - sin(lambda_prime));

	lambda += dlambda;
	beta += dbeta;

	// calculateNutation in longitude
	lambda += MathUtils::toRadians(data->deltaPsi);

	// Right ascension, apparent
	data->RAVenus = MathUtils::toDegrees(MathUtils::norm2PiRad(atan2((sin(lambda) * MathUtils::cosd(data->eps) - tan(beta) * MathUtils::sind(data->eps)), cos(lambda))));

	// Declination of Venus, apparent
	data->DECVenus = MathUtils::toDegrees(asin(sin(beta) * MathUtils::cosd(data->eps) + cos(beta) * MathUtils::sind(data->eps) * sin(lambda)));

	// GHA of Venus
	data->GHAVenus = MathUtils::norm360Deg(data->GHAAtrue - data->RAVenus);

	// Semi-diameter of Venus (including cloud layer)
	data->SDVenus = 8.41 / d;

	// Horizontal parallax of Venus
	data->HPVenus = 8.794 / d;

	// Illumination of the planet's disk
	double k = 100 * (1 + ((R - data->Re * MathUtils::cosd(B) * MathUtils::cosd(L - data->Le)) / d)) / 2;
	data->illumVenus = round(10 * k) / 10;
}

// Calculations for Mars
void calculateMars() {
	// Heliocentric coordinates
	double L = Mars::lMars(data->Tau);
	double B = Mars::bMars(data->Tau);
	double R = Mars::rMars(data->Tau);

	// Rectangular coordinates
	double x = R * MathUtils::cosd(B) * MathUtils::cosd(L) - data->Re * MathUtils::cosd(data->Be) * MathUtils::cosd(data->Le);
	double y = R * MathUtils::cosd(B) * MathUtils::sind(L) - data->Re * MathUtils::cosd(data->Be) * MathUtils::sind(data->Le);
	double z = R * MathUtils::sind(B) - data->Re * MathUtils::sind(data->Be);

	// Geocentric coordinates
	double lambda = atan2(y, x);
	double beta = atan(z / sqrt(x * x + y * y));

	// Distance from earth / light time
	double d = sqrt(x * x + y * y + z * z);
	double lt = 0.0057755183 * d;

	// Time correction
	double Tau_corr = (data->JDE - lt - 2451545) / 365250;

	// Coordinates corrected for light time
	L = Mars::lMars(Tau_corr);
	B = Mars::bMars(Tau_corr);
	R = Mars::rMars(Tau_corr);
	x = R * MathUtils::cosd(B) * MathUtils::cosd(L) - data->Re * MathUtils::cosd(data->Be) * MathUtils::cosd(data->Le);
	y = R * MathUtils::cosd(B) * MathUtils::sind(L) - data->Re * MathUtils::cosd(data->Be) * MathUtils::sind(data->Le);
	z = R * MathUtils::sind(B) - data->Re * MathUtils::sind(data->Be);

	lambda = atan2(y, x);
	beta = atan(z / sqrt(x * x + y * y));

	// aberration
	double dlambda = (data->e * data->kappa * cos(data->pi0 - lambda) - data->kappa * cos(MathUtils::toRadians(data->Lsun_true) - lambda)) / cos(beta);
	double dbeta = -data->kappa * sin(beta) * (sin(MathUtils::toRadians(data->Lsun_true) - lambda) - data->e * sin(data->pi0 - lambda));

	lambda += dlambda;
	beta += dbeta;

	// FK5
	double lambda_prime = lambda - MathUtils::toRadians(1.397) * data->TE -  MathUtils::toRadians(0.00031) * data->TE2;

	dlambda =  MathUtils::toRadians(-0.09033) / 3600 +  MathUtils::toRadians(0.03916) / 3600 * (cos(lambda_prime) + sin(lambda_prime)) * tan(beta);
	dbeta =  MathUtils::toRadians(0.03916) / 3600 * (cos(lambda_prime) - sin(lambda_prime));

	lambda += dlambda;
	beta += dbeta;

	// calculateNutation in longitude
	lambda +=  MathUtils::toRadians(data->deltaPsi);

	// Right ascension, apparent
	data->RAMars = MathUtils::toDegrees(MathUtils::norm2PiRad(atan2((sin(lambda) * MathUtils::cosd(data->eps) - tan(beta) * MathUtils::sind(data->eps)), cos(lambda))));

	// Declination of Mars, apparent
	data->DECMars = MathUtils::toDegrees(asin(sin(beta) * MathUtils::cosd(data->eps) + cos(beta) * MathUtils::sind(data->eps) * sin(lambda)));

	//GHA of Mars
	data->GHAMars = MathUtils::norm360Deg(data->GHAAtrue - data->RAMars);

	// Semi-diameter of Mars
	data->SDMars = 4.68 / d;

	// Horizontal parallax of Mars
	data->HPMars = 8.794 / d;

	// Illumination of the planet's disk
	double k = 100 * (1 + ((R - data->Re * MathUtils::cosd(B) * MathUtils::cosd(L - data->Le)) / d)) / 2;
	data->illumMars = round(10 * k) / 10;
}

// Calculations for Jupiter
void calculateJupiter() {
	// Heliocentric coordinates
	double L = Jupiter::lJupiter(data->Tau);
	double B = Jupiter::bJupiter(data->Tau);
	double R = Jupiter::rJupiter(data->Tau);

	// Rectangular coordinates
	double x = R * MathUtils::cosd(B) * MathUtils::cosd(L) - data->Re * MathUtils::cosd(data->Be) * MathUtils::cosd(data->Le);
	double y = R * MathUtils::cosd(B) * MathUtils::sind(L) - data->Re * MathUtils::cosd(data->Be) * MathUtils::sind(data->Le);
	double z = R * MathUtils::sind(B) - data->Re * MathUtils::sind(data->Be);

	// Geocentric coordinates
	double lambda = atan2(y, x);
	double beta = atan(z / sqrt(x * x + y * y));

	// Distance from earth / light time
	double d = sqrt(x * x + y * y + z * z);
	double lt = 0.0057755183 * d;

	// Time correction
	double Tau_corr = (data->JDE - lt - 2451545) / 365250;

	// Coordinates corrected for light time
	L = Jupiter::lJupiter(Tau_corr);
	B = Jupiter::bJupiter(Tau_corr);
	R = Jupiter::rJupiter(Tau_corr);
	x = R * MathUtils::cosd(B) * MathUtils::cosd(L) - data->Re * MathUtils::cosd(data->Be) * MathUtils::cosd(data->Le);
	y = R * MathUtils::cosd(B) * MathUtils::sind(L) - data->Re * MathUtils::cosd(data->Be) * MathUtils::sind(data->Le);
	z = R * MathUtils::sind(B) - data->Re * MathUtils::sind(data->Be);

	lambda = atan2(y, x);
	beta = atan(z / sqrt(x * x + y * y));

	// aberration
	double dlambda = (data->e * data->kappa * cos(data->pi0 - lambda) - data->kappa * cos(MathUtils::toRadians(data->Lsun_true) - lambda)) / cos(beta);
	double dbeta = -data->kappa * sin(beta) * (sin(MathUtils::toRadians(data->Lsun_true) - lambda) - data->e * sin(data->pi0 - lambda));

	lambda += dlambda;
	beta += dbeta;

	// FK5
	double lambda_prime = lambda - MathUtils::toRadians(1.397) * data->TE - MathUtils::toRadians(0.00031) * data->TE2;

	dlambda = MathUtils::toRadians(-0.09033) / 3600 + MathUtils::toRadians(0.03916) / 3600 * (cos(lambda_prime) + sin(lambda_prime)) * tan(beta);
	dbeta = MathUtils::toRadians(0.03916) / 3600 * (cos(lambda_prime) - sin(lambda_prime));

	lambda += dlambda;
	beta += dbeta;

	// calculateNutation in longitude
	lambda += MathUtils::toRadians(data->deltaPsi);

	// Right ascension, apparent
	data->RAJupiter = MathUtils::toDegrees(MathUtils::norm2PiRad(atan2((sin(lambda) * MathUtils::cosd(data->eps) - tan(beta) * MathUtils::sind(data->eps)), cos(lambda))));

	// Declination of Jupiter, apparent
	data->DECJupiter = MathUtils::toDegrees(asin(sin(beta) * MathUtils::cosd(data->eps) + cos(beta) * MathUtils::sind(data->eps) * sin(lambda)));

	// GHA of Jupiter
	data->GHAJupiter = MathUtils::norm360Deg(data->GHAAtrue - data->RAJupiter);

	// Semi-diameter of Jupiter (equatorial)
	data->SDJupiter = 98.44 / d;

	// Horizontal parallax of Jupiter
	data->HPJupiter = 8.794 / d;

	// Illumination of the planet's disk
	double k = 100 * (1 + ((R - data->Re * MathUtils::cosd(B) * MathUtils::cosd(L - data->Le)) / d)) / 2;
	data->illumJupiter = round(10 * k) / 10;
}

// Calculations for Saturn
void calculateSaturn() {
	// Heliocentric coordinates
	double L = Saturn::lSaturn(data->Tau);
	double B = Saturn::bSaturn(data->Tau);
	double R = Saturn::rSaturn(data->Tau);

	// Rectangular coordinates
	double x = R * MathUtils::cosd(B) * MathUtils::cosd(L) - data->Re * MathUtils::cosd(data->Be) * MathUtils::cosd(data->Le);
	double y = R * MathUtils::cosd(B) * MathUtils::sind(L) - data->Re * MathUtils::cosd(data->Be) * MathUtils::sind(data->Le);
	double z = R * MathUtils::sind(B) - data->Re * MathUtils::sind(data->Be);

	// Geocentric coordinates
	double lambda = atan2(y, x);
	double beta = atan(z / sqrt(x * x + y * y));

	// Distance from earth / light time
	double d = sqrt(x * x + y * y + z * z);
	double lt = 0.0057755183 * d;

	// Time correction
	double Tau_corr = (data->JDE - lt - 2451545) / 365250;

	// Coordinates corrected for light time
	L = Saturn::lSaturn(Tau_corr);
	B = Saturn::bSaturn(Tau_corr);
	R = Saturn::rSaturn(Tau_corr);
	x = R * MathUtils::cosd(B) * MathUtils::cosd(L) - data->Re * MathUtils::cosd(data->Be) * MathUtils::cosd(data->Le);
	y = R * MathUtils::cosd(B) * MathUtils::sind(L) - data->Re * MathUtils::cosd(data->Be) * MathUtils::sind(data->Le);
	z = R * MathUtils::sind(B) - data->Re * MathUtils::sind(data->Be);

	lambda = atan2(y, x);
	beta = atan(z / sqrt(x * x + y * y));

	// aberration
	double dlambda = (data->e * data->kappa * cos(data->pi0 - lambda) - data->kappa * cos(MathUtils::toRadians(data->Lsun_true) - lambda)) / cos(beta);
	double dbeta = -data->kappa * sin(beta) * (sin(MathUtils::toRadians(data->Lsun_true) - lambda) - data->e * sin(data->pi0 - lambda));

	lambda += dlambda;
	beta += dbeta;

	// FK5
	double lambda_prime = lambda - MathUtils::toRadians(1.397) * data->TE - MathUtils::toRadians(0.00031) * data->TE2;
	dlambda = MathUtils::toRadians(-0.09033) / 3600 + MathUtils::toRadians(0.03916) / 3600 * (cos(lambda_prime) + sin(lambda_prime)) * tan(beta);
	dbeta = MathUtils::toRadians(0.03916) / 3600 * (cos(lambda_prime) - sin(lambda_prime));

	lambda += dlambda;
	beta += dbeta;

	// calculateNutation in longitude
	lambda += MathUtils::toRadians(data->deltaPsi);

	// Right ascension, apparent
	data->RASaturn = MathUtils::toDegrees(MathUtils::norm2PiRad(atan2((sin(lambda) * MathUtils::cosd(data->eps) - tan(beta) * MathUtils::sind(data->eps)), cos(lambda))));

	// Declination of Saturn, apparent
	data->DECSaturn = MathUtils::toDegrees(asin(sin(beta) * MathUtils::cosd(data->eps) + cos(beta) * MathUtils::sind(data->eps) * sin(lambda)));

	// GHA of Saturn
	data->GHASaturn = MathUtils::norm360Deg(data->GHAAtrue - data->RASaturn);

	// Semi-diameter of Saturn (equatorial)
	data->SDSaturn = 82.73 / d;

	// Horizontal parallax of Saturn
	data->HPSaturn = 8.794 / d;

	// Illumination of the planet's disk
	double k = 100 * (1 + ((R - data->Re * MathUtils::cosd(B) * MathUtils::cosd(L - data->Le)) / d)) / 2;
	data->illumSaturn = round(10 * k) / 10;
}

// Calculations for the moon
void calculateMoon() {
	// Mean longitude of the moon
	double Lmm = MathUtils::norm360Deg(218.3164591 + 481267.88134236 * data->TE - 0.0013268 * data->TE2 + data->TE3 / 538841 - data->TE4 / 65194000);

	// Mean elongation of the moon
	double D = MathUtils::norm360Deg(297.8502042 + 445267.1115168 * data->TE - 0.00163 * data->TE2 + data->TE3 / 545868 - data->TE4 / 113065000);

	// Mean anomaly of the sun
	double Msm = MathUtils::norm360Deg(357.5291092 + 35999.0502909 * data->TE - 0.0001536 * data->TE2 + data->TE3 / 24490000);

	// Mean anomaly of the moon
	double Mmm = MathUtils::norm360Deg(134.9634114 + 477198.8676313 * data->TE + 0.008997 * data->TE2 + data->TE3 / 69699 - data->TE4 / 14712000);

	// Mean distance of the moon from ascending node
	double F = MathUtils::norm360Deg(93.2720993 + 483202.0175273 * data->TE - 0.0034029 * data->TE2 - data->TE3 / 3526000 + data->TE4 / 863310000);

	// Corrections
	double A1 = MathUtils::norm360Deg(119.75 + 131.849 * data->TE);
	double A2 = MathUtils::norm360Deg(53.09 + 479264.29 * data->TE);
	double A3 = MathUtils::norm360Deg(313.45 + 481266.484 * data->TE);
	double fE = 1 - 0.002516 * data->TE - 0.0000074 * data->TE2;
	double fE2 = fE * fE;

	// Periodic terms for the moon:

	// Longitude and distance
	const int ldRows = 60,
            ldColumns = 6;
	const double ld[ldRows][ldColumns] = {
		{ 0,  0,  1,  0, 6288774, -20905355 },
		{ 2,  0, -1,  0, 1274027,  -3699111 },
		{ 2,  0,  0,  0,  658314,  -2955968 },
		{ 0,  0,  2,  0,  213618,   -569925 },
		{ 0,  1,  0,  0, -185116,     48888 },
		{ 0,  0,  0,  2, -114332,     -3149 },
		{ 2,  0, -2,  0,   58793,    246158 },
		{ 2, -1, -1,  0,   57066,   -152138 },
		{ 2,  0,  1,  0,   53322,   -170733 },
		{ 2, -1,  0,  0,   45758,   -204586 },
		{ 0,  1, -1,  0,  -40923,   -129620 },
		{ 1,  0,  0,  0,  -34720,    108743 },
		{ 0,  1,  1,  0,  -30383,    104755 },
		{ 2,  0,  0, -2,   15327,     10321 },
		{ 0,  0,  1,  2,  -12528,         0 },
		{ 0,  0,  1, -2,   10980,     79661 },
		{ 4,  0, -1,  0,   10675,    -34782 },
		{ 0,  0,  3,  0,   10034,    -23210 },
		{ 4,  0, -2,  0,    8548,    -21636 },
		{ 2,  1, -1,  0,   -7888,     24208 },
		{ 2,  1,  0,  0,   -6766,     30824 },
		{ 1,  0, -1,  0,   -5163,     -8379 },
		{ 1,  1,  0,  0,    4987,    -16675 },
		{ 2, -1,  1,  0,    4036,    -12831 },
		{ 2,  0,  2,  0,    3994,    -10445 },
		{ 4,  0,  0,  0,    3861,    -11650 },
		{ 2,  0, -3,  0,    3665,     14403 },
		{ 0,  1, -2,  0,   -2689,     -7003 },
		{ 2,  0, -1,  2,   -2602,         0 },
		{ 2, -1, -2,  0,    2390,     10056 },
		{ 1,  0,  1,  0,   -2348,      6322 },
		{ 2, -2,  0,  0,    2236,     -9884 },
		{ 0,  1,  2,  0,   -2120,      5751 },
		{ 0,  2,  0,  0,   -2069,         0 },
		{ 2, -2, -1,  0,    2048,     -4950 },
		{ 2,  0,  1, -2,   -1773,      4130 },
		{ 2,  0,  0,  2,   -1595,         0 },
		{ 4, -1, -1,  0,    1215,     -3958 },
		{ 0,  0,  2,  2,   -1110,         0 },
		{ 3,  0, -1,  0,    -892,      3258 },
		{ 2,  1,  1,  0,    -810,      2616 },
		{ 4, -1, -2,  0,     759,     -1897 },
		{ 0,  2, -1,  0,    -713,     -2117 },
		{ 2,  2, -1,  0,    -700,      2354 },
		{ 2,  1, -2,  0,     691,         0 },
		{ 2, -1,  0, -2,     596,         0 },
		{ 4,  0,  1,  0,     549,     -1423 },
		{ 0,  0,  4,  0,     537,     -1117 },
		{ 4, -1,  0,  0,     520,     -1571 },
		{ 1,  0, -2,  0,    -487,     -1739 },
		{ 2,  1,  0, -2,    -399,         0 },
		{ 0,  0,  2, -2,    -381,     -4421 },
		{ 1,  1,  1,  0,     351,         0 },
		{ 3,  0, -2,  0,    -340,         0 },
		{ 4,  0, -3,  0,     330,         0 },
		{ 2, -1,  2,  0,     327,         0 },
		{ 0,  2,  1,  0,    -323,      1165 },
		{ 1,  1, -1,  0,     299,         0 },
		{ 2,  0,  3,  0,     294,         0 },
		{ 2,  0, -1, -2,       0,      8752 }
	};

	const int latRows = 60,
            latColumns = 5;
	const double lat[latRows][latColumns] = {
		{ 0,  0,  0,  1, 5128122 },
		{ 0,  0,  1,  1,  280602 },
		{ 0,  0,  1, -1,  277693 },
		{ 2,  0,  0, -1,  173237 },
		{ 2,  0, -1,  1,   55413 },
		{ 2,  0, -1, -1,   46271 },
		{ 2,  0,  0,  1,   32573 },
		{ 0,  0,  2,  1,   17198 },
		{ 2,  0,  1, -1,    9266 },
		{ 0,  0,  2, -1,    8822 },
		{ 2, -1,  0, -1,    8216 },
		{ 2,  0, -2, -1,    4324 },
		{ 2,  0,  1,  1,    4200 },
		{ 2,  1,  0, -1,   -3359 },
		{ 2, -1, -1,  1,    2463 },
		{ 2, -1,  0,  1,    2211 },
		{ 2, -1, -1, -1,    2065 },
		{ 0,  1, -1, -1,   -1870 },
		{ 4,  0, -1, -1,    1828 },
		{ 0,  1,  0,  1,   -1794 },
		{ 0,  0,  0,  3,   -1749 },
		{ 0,  1, -1,  1,   -1565 },
		{ 1,  0,  0,  1,   -1491 },
		{ 0,  1,  1,  1,   -1475 },
		{ 0,  1,  1, -1,   -1410 },
		{ 0,  1,  0, -1,   -1344 },
		{ 1,  0,  0, -1,   -1335 },
		{ 0,  0,  3,  1,    1107 },
		{ 4,  0,  0, -1,    1021 },
		{ 4,  0, -1,  1,     833 },
		{ 0,  0,  1, -3,     777 },
		{ 4,  0, -2,  1,     671 },
		{ 2,  0,  0, -3,     607 },
		{ 2,  0,  2, -1,     596 },
		{ 2, -1,  1, -1,     491 },
		{ 2,  0, -2,  1,    -451 },
		{ 0,  0,  3, -1,     439 },
		{ 2,  0,  2,  1,     422 },
		{ 2,  0, -3, -1,     421 },
		{ 2,  1, -1,  1,    -366 },
		{ 2,  1,  0,  1,    -351 },
		{ 4,  0,  0,  1,     331 },
		{ 2, -1,  1,  1,     315 },
		{ 2, -2,  0, -1,     302 },
		{ 0,  0,  1,  3,    -283 },
		{ 2,  1,  1, -1,    -229 },
		{ 1,  1,  0, -1,     223 },
		{ 1,  1,  0,  1,     223 },
		{ 0,  1, -2, -1,    -220 },
		{ 2,  1, -1, -1,    -220 },
		{ 1,  0,  1,  1,    -185 },
		{ 2, -1, -2, -1,     181 },
		{ 0,  1,  2,  1,    -177 },
		{ 4,  0, -2, -1,     176 },
		{ 4, -1, -1, -1,     166 },
		{ 1,  0,  1, -1,    -164 },
		{ 4,  0,  1, -1,     132 },
		{ 1,  0, -1, -1,    -119 },
		{ 4, -1,  0, -1,     115 },
		{ 2, -2,  0,  1,     107 }
	};

	// Reading periodic terms
	double fD, fD2, fM, fM2, fMm, fMm2, fF, fF2, coeffs, coeffs2, coeffc, f, f2, sumL = 0, sumR = 0, sumB = 0;
	int x = 0;

	while (x < latRows) {
		fD = ld[x][0];
		fM = ld[x][1];
		fMm = ld[x][2];
		fF = ld[x][3];
		coeffs = ld[x][4];
		coeffc = ld[x][5];
		if (fM == 1 || fM == -1) {
			f = fE;
		} else if (fM == 2 || fM == -2) {
			f = fE2;
		} else {
			f = 1;
		}
		sumL += f * coeffs * MathUtils::sind(fD * D + fM * Msm + fMm * Mmm + fF * F);
		sumR += f * coeffc * MathUtils::cosd(fD * D + fM * Msm + fMm * Mmm + fF * F);
		fD2 = lat[x][0];
		fM2 = lat[x][1];
		fMm2 = lat[x][2];
		fF2 = lat[x][3];
		coeffs2 = lat[x][4];
		if (fM2 == 1 || fM2 == -1) {
			f2 = fE;
		} else if (fM2 == 2 || fM2 == -2) {
			f2 = fE2;
		} else {
			f2 = 1;
		}
		sumB += f2 * coeffs2 * MathUtils::sind(fD2 * D + fM2 * Msm + fMm2 * Mmm + fF2 * F);
		x++;
	}

	// Corrections
	sumL = sumL + 3958 * MathUtils::sind(A1) + 1962 * MathUtils::sind(Lmm - F) + 318 * MathUtils::sind(A2);
	sumB = sumB - 2235 * MathUtils::sind(Lmm) + 382 * MathUtils::sind(A3) + 175 * MathUtils::sind(A1 - F) + 175 * MathUtils::sind(A1 + F) + 127 * MathUtils::sind(Lmm - Mmm) - 115 * MathUtils::sind(Lmm + Mmm);

	// Longitude of the moon
	double lambdaMm = MathUtils::norm360Deg(Lmm + sumL / 1000000);

	// Latitude of the moon
	double betaM = sumB / 1000000;

	// Distance earth-moon
	double dEM = 385000.56 + sumR / 1000;

	// Apparent longitude of the moon
	data->lambdaMapp = lambdaMm + data->deltaPsi;

	// Right ascension of the moon, apparent
	data->RAMoon = MathUtils::toDegrees(MathUtils::norm2PiRad(atan2((MathUtils::sind(data->lambdaMapp) * MathUtils::cosd(data->eps) - MathUtils::tand(betaM) * MathUtils::sind(data->eps)), MathUtils::cosd(data->lambdaMapp))));

	// Declination of the moon
	data->DECMoon = MathUtils::toDegrees(asin(MathUtils::sind(betaM) * MathUtils::cosd(data->eps) + MathUtils::cosd(betaM) * MathUtils::sind(data->eps) * MathUtils::sind(data->lambdaMapp)));

	// GHA of the moon
	data->GHAMoon = MathUtils::norm360Deg(data->GHAAtrue - data->RAMoon);

	// Horizontal parallax of the moon
	data->HPMoon = MathUtils::toDegrees(3600 * asin(6378.14 / dEM));

	// Semi-diameter of the moon
	data->SDMoon = MathUtils::toDegrees(3600 * asin(1738 / dEM));

	// Geocentric angular distance between moon and sun
	data->LDist = MathUtils::toDegrees(acos(MathUtils::sind(data->DECMoon) * MathUtils::sind(data->DECSun) + MathUtils::cosd(data->DECMoon) * MathUtils::cosd(data->DECSun) * MathUtils::cosd(data->RAMoon - data->RASun)));

	//Phase angle
	double i = atan2(data->dES * MathUtils::sind(data->LDist), (dEM - data->dES * MathUtils::cosd(data->LDist)));

	//Illumination of the moon's disk in %
	double k = 100 * (1 + cos(i)) / 2;
	data->illumMoon = k;                // round(10 * k) / 10;
}

// Ephemerides of Polaris
void calculatePolaris() {
	// Equatorial coordinates of Polaris at 2000.0 (mean equinox and equator 2000.0)
	double RApol0 = 37.95293333;
	double DECpol0 = 89.26408889;

	// Proper motion per year
	double dRApol = 2.98155 / 3600;
	double dDECpol = -0.0152 / 3600;

	// Equatorial coordinates at Julian Date T (mean equinox and equator 2000.0)
	double RApol1 = RApol0 + 100 * data->TE * dRApol;
	double DECpol1 = DECpol0 + 100 * data->TE * dDECpol;

	// Mean obliquity of ecliptic at 2000.0 in degrees
	double eps0_2000 = 23.439291111;

	// Transformation to ecliptic coordinates in radians (mean equinox and equator 2000.0)
	double lambdapol1 = atan2((MathUtils::sind(RApol1) * MathUtils::cosd(eps0_2000) + MathUtils::tand(DECpol1) * MathUtils::sind(eps0_2000)), MathUtils::cosd(RApol1));
	double betapol1 = asin(MathUtils::sind(DECpol1) * MathUtils::cosd(eps0_2000) - MathUtils::cosd(DECpol1) * MathUtils::sind(eps0_2000) * MathUtils::sind(RApol1));

	// Precession
	double eta = MathUtils::toRadians(47.0029 * data->TE - 0.03302 * data->TE2 + 0.00006 * data->TE3) / 3600;
	double PI0 = MathUtils::toRadians(174.876384 - (869.8089 * data->TE + 0.03536 * data->TE2) / 3600);
	double p0 = MathUtils::toRadians(5029.0966 * data->TE + 1.11113 * data->TE2 - 0.0000006 * data->TE3) / 3600;

	double A1 = cos(eta) * cos(betapol1) * sin(PI0 - lambdapol1) - sin(eta) * sin(betapol1);
	double B1 = cos(betapol1) * cos(PI0 - lambdapol1);
	double C1 = cos(eta) * sin(betapol1) + sin(eta) * cos(betapol1) * sin(PI0 - lambdapol1);
	double lambdapol2 = p0 + PI0 - atan2(A1, B1);
	double betapol2 = asin(C1);

	// calculateNutation in longitude
	lambdapol2 += MathUtils::toRadians(data->deltaPsi);

	// aberration
	double dlambdapol = (data->e * data->kappa * cos(data->pi0 - lambdapol2) - data->kappa * cos(MathUtils::toRadians(data->Lsun_true) - lambdapol2)) / cos(betapol2);
	double dbetapol = -data->kappa * sin(betapol2) * (sin(MathUtils::toRadians(data->Lsun_true) - lambdapol2) - data->e * sin(data->pi0 - lambdapol2));

	lambdapol2 += dlambdapol;
	betapol2 += dbetapol;

	// Transformation back to equatorial coordinates in radians
	double RApol2 = atan2((sin(lambdapol2) * MathUtils::cosd(data->eps) - tan(betapol2) * MathUtils::sind(data->eps)), cos(lambdapol2));
	double DECpol2 = asin(sin(betapol2) * MathUtils::cosd(data->eps) + cos(betapol2) * MathUtils::sind(data->eps) * sin(lambdapol2));

	// Finals
	data->GHAPol = data->GHAAtrue - MathUtils::toDegrees(RApol2);
	data->GHAPol = MathUtils::norm360Deg(data->GHAPol);
	data->RAPol = MathUtils::toDegrees(RApol2);
	data->DECPol = MathUtils::toDegrees(DECpol2);
}

// Calculation of the phase of the Moon
void calculateMoonPhase() {
	double x = data->lambdaMapp - data->lambdaSun;
	x = MathUtils::norm360Deg(x);
	x = round(10 * x) / 10;
	data->moonPhaseAngle = x;
	if (x == 0) {
		strcpy(data->moonPhase, " New");
	}
	if (x > 0 && x < 90) {
		strcpy(data->moonPhase, " +cre");
	}
	if (x == 90) {
		strcpy(data->moonPhase, " FQ");
	}
	if (x > 90 && x < 180) {
		strcpy(data->moonPhase, " +gib");
	}
	if (x == 180) {
		strcpy(data->moonPhase, " Full");
	}
	if (x > 180 && x < 270) {
		strcpy(data->moonPhase, " -gib");
	}
	if (x == 270) {
		strcpy(data->moonPhase, " LQ");
	}
	if (x > 270 && x < 360) {
		strcpy(data->moonPhase, " -cre");
	}
}

void calculateWeekDay() {
  data->JD0h += 1.5;
  double res = data->JD0h - 7 * floor(data->JD0h / 7);
  // data->DoW = (char *) calloc(4, sizeof(char));
  switch ((int)res) {
    case 0:
      strcpy(data->DoW, "SUN");
      break;
    case 1:
      strcpy(data->DoW, "MON");
      break;
    case 2:
      strcpy(data->DoW, "TUE");
      break;
    case 3:
      strcpy(data->DoW, "WED");
      break;
    case 4:
      strcpy(data->DoW, "THU");
      break;
    case 5:
      strcpy(data->DoW, "FRI");
      break;
    case 6:
      strcpy(data->DoW, "SAT");
      break;
    default:
      break;
  }
}

/**
 * Y parameter (not year) for deltaT computing.
 *
 * @param year
 * @param month in [1..12]
 * @return
 */
double getY(int year, int month) {
	if (year < -1999 || year > 3000) {
		fprintf(stderr, "Year must be in [-1999, 3000]\n");
		return 0;
	} else {
		// fprintf(stdout, "Returning y=%f\n", (year + ((month - 0.5) / 12.0)));
		return (year + ((month - 0.5) / 12.0));
	}
}

/**
 * Since the usual (the ones I used to used) on-line resources are not always available, obsolete,
 * or expecting some serious revamping, here is a method to calculate deltaT out of thin air.
 *
 * See https://astronomy.stackexchange.com/questions/19172/obtaining-deltat-for-use-in-software
 * See values at https://eclipse.gsfc.nasa.gov/SEcat5/deltat.html#tab1 and
 *               https://eclipse.gsfc.nasa.gov/SEcat5/deltat.html#tab2
 *
 * @param year from -1999 to +3000
 * @param month in [1..12], NOT in [0..11] <b>&lt; Important!!</b>
 * @return
 */
double calculateDeltaT(int year, int month) {
	if (year < -1999 || year > 3000) {
		fprintf(stderr, "Year must be in [-1999, 3000]\n");
	}
	if (month < 1 || month > 12) {
		fprintf(stderr, "Month must be in [1, 12]\n");
	}

	double deltaT;
	double y = getY(year, month);

	if (year < -500) {
		double u = (y - 1820.0) / 100.0;
		deltaT = -20.0 + (32.0 * (u * u));
	} else if (year < 500) {
		double u = y / 100.0;
		deltaT = 10583.6
				+ (-1014.41 * u)
				+ (33.78311 * pow(u, 2))
				+ (-5.952053 * pow(u, 3))
				+ (-0.1798452 * pow(u, 4))
				+ (0.022174192 * pow(u, 5))
				+ (0.0090316521 * pow(u, 6));
	} else if (year < 1600) {
		double u = (y - 1000.0) / 100.0;
		deltaT = 1574.2
				+ (-556.01 * u)
				+ (71.23472 * pow(u, 2))
				+ (0.319781 * pow(u, 3))
				+ (-0.8503463 * pow(u, 4))
				+ (-0.005050998 * pow(u, 5))
				+ (0.0083572073 * pow(u, 6));
	} else if (year < 1700) {
		double t = y - 1600.0;
		deltaT = 120
				+ (-0.9808 * t)
				+ (-0.01532 * pow(t, 2))
				+ (pow(t, 3) / 7129);
	} else if (year < 1800) {
		double t = y - 1700.0;
		deltaT = 8.83
				+ 0.1603 * t
				+ (-0.0059285 * pow(t, 2))
				+ (0.00013336 * pow(t, 3))
				+ (pow(t, 4) / -1174000);
	} else if (year < 1860) {
		double t = y - 1800.0;
		deltaT = 13.72
				+ (-0.332447 * t)
				+ (0.0068612 * pow(t, 2))
				+ (0.0041116 * pow(t, 3))
				+ (-0.00037436 * pow(t, 4))
				+ (0.0000121272 * pow(t, 5))
				+ (-0.0000001699 * pow(t, 6))
				+ (0.000000000875 * pow(t, 7));
	} else if (year < 1900) {
		double t = y - 1860.0;
		deltaT = 7.62 +
				(0.5737 * t)
				+ (-0.251754 * pow(t, 2))
				+ (0.01680668 * pow(t, 3))
				+ (-0.0004473624 * pow(t, 4))
				+ (pow(t, 5) / 233174);
	} else if (year < 1920) {
		double t = y - 1900;
		deltaT = -2.79
				+ (1.494119 * t)
				+ (-0.0598939 * pow(t, 2))
				+ (0.0061966 * pow(t, 3))
				+ (-0.000197 * pow(t, 4));
	} else if (year < 1941) {
		double t = y - 1920;
		deltaT = 21.20
				+ (0.84493 * t)
				+ (-0.076100 * pow(t, 2))
				+ (0.0020936 * pow(t, 3));
	} else if (year < 1961) {
		double t = y - 1950;
		deltaT = 29.07
				+ (0.407 * t)
				+ (pow(t, 2) / -233)
				+ (pow(t, 3) / 2547);
	} else if (year < 1986) {
		double t = y - 1975;
		deltaT = 45.45
				+ (1.067 * t)
				+ (pow(t, 2) / -260)
				+ (pow(t, 3) / -718);
	} else if (year < 2005) {
		double t = y - 2000;
		deltaT = 63.86
				+ (0.3345 * t)
				+ (-0.060374 * pow(t, 2))
				+ (0.0017275 * pow(t, 3))
				+ (0.000651814 * pow(t, 4))
				+ (0.00002373599 * pow(t, 5));
	} else if (year < 2050) {
 		double t = y - 2000;
		deltaT = 62.92
				+ (0.32217 * t)
				+ (0.005589 * pow(t, 2));
	} else if (year < 2150) {
		deltaT = -20
				+ (32 * pow((y - 1820) / 100, 2))
				+ (-0.5628 * (2150 - y));
	} else {
		double u = (y - 1820) / 100;
		deltaT = -20
				+ (32 * pow(u, 2));
	}

	return deltaT;
}
