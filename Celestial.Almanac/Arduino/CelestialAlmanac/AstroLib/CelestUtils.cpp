// https://www.arduino.cc/reference/en/

// #include <Arduino.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h> 

#include "CelestStruct.h"
//#endif
#include "MathUtils.h"
#include "Earth.h"

MathUtils cuMu;
Earth earth; // No (), this would confuse the compiler.

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
char * outSideralTime(float x) {
	float GMSTdecimal = x / 15;
	int GMSTh = floor(GMSTdecimal);
	float GMSTmdecimal = 60 * (GMSTdecimal - GMSTh);
	int GMSTm = floor(GMSTmdecimal);
	float GMSTsdecimal = 60 * (GMSTmdecimal - GMSTm);
	int GMSTs = round(1000 * GMSTsdecimal) / 1000;
	// if (GMSTs - floor(GMSTs) == 0) {
	// 	GMSTs += ".000";
	// } else if (10 * GMSTs - floor(10 * GMSTs) == 0) {
	// 	GMSTs += "00";
	// } else if (100 * GMSTs - floor(100 * GMSTs) == 0) {
	// 	GMSTs += "0";
	// }
	sprintf(dataBuffer, "%dh %dm %ds", GMSTh, GMSTm, GMSTs);
	return &dataBuffer[0];
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
ComputedData * calculate(int year, int month, int day, int hour, int minute, int second, float delta_t) {
  calculateJulianDate(year, month, day, hour, minute, second, delta_t);
  calculateNutation();
  calculateAberration();
  calculateAries();
//  calculateSun();
//  calculateVenus();
//  calculateMars();
//  calculateJupiter();
//  calculateSaturn();
//  calculateMoon();
//  calculatePolaris();
//  calculateMoonPhase();
  calculateWeekDay();
  // return gatherOutput(); // TODO Return a struct like below
  return data;
}

/**
 * Input data conversion and reworking
 * All data are UTC data (except detlaT)
 **
 * @param year Number, UTC year
 * @param month Number, UTC month, [1..12]
 * @param day Number, UTC day of month
 * @param hour Number, UTC hour
 * @param minute Number, UTC minute
 * @param second Number, UTC second
 * @param delta_t Number, DeltaT
 */
void calculateJulianDate(int year, int month, int day, int hour, int minute, int second, float delta_t) {

  data->dayFraction = (hour + minute / 60 + second / 3600) / 24;
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
  float A = floor(year / 100);
  float B = 2 - A + floor(A / 4);
  data->JD0h = floor(365.25 * (year + 4716)) + floor(30.6001 * (month + 1)) + day + B - 1524.5;

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
}

// Nutation, obliquity of the ecliptic
void calculateNutation() {
  // IAU 1980 calculateNutation theory:

  // Mean anomaly of the Moon
  float Mm = 134.962981389 + 198.867398056 * data->TE + cuMu.norm360Deg(477000 * data->TE) + 0.008697222222 * data->TE2 + data->TE3 / 56250;

  // Mean anomaly of the Sun
  float M = 357.527723333 + 359.05034 * data->TE + cuMu.norm360Deg(35640 * data->TE) - 0.0001602777778 * data->TE2 - data->TE3 / 300000;

  // Mean distance of the Moon from ascending node
  float F = 93.271910277 + 82.017538055 * data->TE + cuMu.norm360Deg(483120 * data->TE) - 0.0036825 * data->TE2 + data->TE3 / 327272.7273;

  // Mean elongation of the Moon
  float D = 297.850363055 + 307.11148 * data->TE + cuMu.norm360Deg(444960 * data->TE) - 0.001914166667 * data->TE2 + data->TE3 / 189473.6842;

  // Longitude of the ascending node of the Moon
  float omega = 125.044522222 - 134.136260833 * data->TE - cuMu.norm360Deg(1800 * data->TE) + 0.002070833333 * data->TE2 + data->TE3 / 450000;

  // Periodic terms for nutation
  const int nutRows = 106,
            nutColumns = 9;
  const float nut[nutRows][nutColumns] = {
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
  float fMm, fM, fF, fD, f_omega, dp = 0, de = 0;
  int x = 0;
  while (x < nutRows) {
    fMm = nut[x][0];
    fM = nut[x][1];
    fF = nut[x][2];
    fD = nut[x][3];
    f_omega = nut[x][4];
    dp += (nut[x][5] + data->TE * nut[x][6]) * cuMu.sind(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega);
    de += (nut[x][7] + data->TE * nut[x][8]) * cuMu.cosd(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega);
    x++;
  }

  // Corrections (Herring, 1987)
  int corrRows = 4;
  int corrColumns = 9;
  const float corr[corrRows][corrColumns] = {
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
    dp += 0.1 * (corr[x][5] * cuMu.sind(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega) + corr[x][6] * cuMu.cosd(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega));
    de += 0.1 * (corr[x][7] * cuMu.cosd(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega) + corr[x][8] * cuMu.sind(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega));
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
  data->kappa = cuMu.toRadians(20.49552 / 3600);
  data->pi0 = cuMu.toRadians(102.93735 + 1.71953 * data->TE + 0.00046 * data->TE2);
  data->e = 0.016708617 - 0.000042037 * data->TE - 0.0000001236 * data->TE2;
}

// GHA Aries, GAST, GMST, equation of the equinoxes
void calculateAries() {
	// Mean GHA Aries
	data->GHAAmean = cuMu.norm360Deg(280.46061837 + 360.98564736629 * (data->JD - 2451545) + 0.000387933 * data->T2 - data->T3 / 38710000);

	// GMST
	data->SidTm = outSideralTime(data->GHAAmean);

	// True GHA Aries
	data->GHAAtrue = cuMu.norm360Deg(data->GHAAmean + data->deltaPsi * cuMu.cosd(data->eps));

	// GAST
	data->SidTa = outSideralTime(data->GHAAtrue);

	// Equation of the equinoxes
	data->EoE = 240 * data->deltaPsi * cuMu.cosd(data->eps);
	data->EoEout = round(1000 * data->EoE) / 1000;
	// EoEout = " " + EoEout + "s";
}

// Calculations for the Sun
void calculateSun() {
	// Mean longitude of the Sun
	float Lsun_mean = cuMu.norm360Deg(280.4664567 + 360007.6982779 * data->Tau + 0.03032028 * data->Tau2 + data->Tau3 / 49931 - data->Tau4 / 15299 - data->Tau5 / 1988000);

	// Heliocentric longitude of the Earth
  data->Le = earth.lEarth(data->Tau);

	// Geocentric longitude of the Sun
	data->Lsun_true = cuMu.norm360Deg(data->Le + 180 - 0.000025);

	// Heliocentric latitude of Earth
	data->Be = earth.bEarth(data->Tau);

	// Geocentric latitude of the Sun
	float beta = cuMu.norm360Deg(- data->Be);

	// Corrections
	data->Lsun_prime = cuMu.norm360Deg(data->Le + 180 - 1.397 * data->TE - 0.00031 * data->TE2);

	beta = beta + 0.000011 * (cuMu.cosd(data->Lsun_prime) - cuMu.sind(data->Lsun_prime));

	// Distance Earth-Sun
	data->Re = earth.rEarth(data->Tau);
	data->dES = 149597870.691 * data->Re;

	// Apparent longitude of the Sun
	data->lambdaSun = cuMu.norm360Deg(data->Lsun_true + data->deltaPsi - 0.005691611 / data->Re);

	// Right ascension of the Sun, apparent
	data->RASun = cuMu.toDegrees(cuMu.norm2PiRad(atan2((cuMu.sind(data->lambdaSun) * cuMu.cosd(data->eps) - cuMu.tand(beta) * cuMu.sind(data->eps)), cuMu.cosd(data->lambdaSun))));

	// Declination of the Sun, apparent
	data->DECSun = cuMu.toDegrees(asin(cuMu.sind(beta) * cuMu.cosd(data->eps) + cuMu.cosd(beta) * cuMu.sind(data->eps) * cuMu.sind(data->lambdaSun)));

	// GHA of the Sun
	data->GHASun = cuMu.norm360Deg(data->GHAAtrue - data->RASun);

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

// Calculations for the moon
void calculateMoon() {
	// Mean longitude of the moon
	float Lmm = cuMu.norm360Deg(218.3164591 + 481267.88134236 * data->TE - 0.0013268 * data->TE2 + data->TE3 / 538841 - data->TE4 / 65194000);

	// Mean elongation of the moon
	float D = cuMu.norm360Deg(297.8502042 + 445267.1115168 * data->TE - 0.00163 * data->TE2 + data->TE3 / 545868 - data->TE4 / 113065000);

	// Mean anomaly of the sun
	float Msm = cuMu.norm360Deg(357.5291092 + 35999.0502909 * data->TE - 0.0001536 * data->TE2 + data->TE3 / 24490000);

	// Mean anomaly of the moon
	float Mmm = cuMu.norm360Deg(134.9634114 + 477198.8676313 * data->TE + 0.008997 * data->TE2 + data->TE3 / 69699 - data->TE4 / 14712000);

	// Mean distance of the moon from ascending node
	float F = cuMu.norm360Deg(93.2720993 + 483202.0175273 * data->TE - 0.0034029 * data->TE2 - data->TE3 / 3526000 + data->TE4 / 863310000);

	// Corrections
	float A1 = cuMu.norm360Deg(119.75 + 131.849 * data->TE);
	float A2 = cuMu.norm360Deg(53.09 + 479264.29 * data->TE);
	float A3 = cuMu.norm360Deg(313.45 + 481266.484 * data->TE);
	float fE = 1 - 0.002516 * data->TE - 0.0000074 * data->TE2;
	float fE2 = fE * fE;

	// Periodic terms for the moon:

	// Longitude and distance
	int ldRows = 60;
	int ldColumns = 6;
	const float ld[ldRows][ldColumns] = {
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

	int latRows = 60;
	int latColumns = 5;
	const float lat[latRows][latColumns] = {
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
	float fD, fD2, fM, fM2, fMm, fMm2, fF, fF2, coeffs, coeffs2, coeffc, f, f2, sumL = 0, sumR = 0, sumB = 0;
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
		sumL += f * coeffs * cuMu.sind(fD * D + fM * Msm + fMm * Mmm + fF * F);
		sumR += f * coeffc * cuMu.cosd(fD * D + fM * Msm + fMm * Mmm + fF * F);
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
		sumB += f2 * coeffs2 * cuMu.sind(fD2 * D + fM2 * Msm + fMm2 * Mmm + fF2 * F);
		x++;
	}

	// Corrections
	sumL = sumL + 3958 * cuMu.sind(A1) + 1962 * cuMu.sind(Lmm - F) + 318 * cuMu.sind(A2);
	sumB = sumB - 2235 * cuMu.sind(Lmm) + 382 * cuMu.sind(A3) + 175 * cuMu.sind(A1 - F) + 175 * cuMu.sind(A1 + F) + 127 * cuMu.sind(Lmm - Mmm) - 115 * cuMu.sind(Lmm + Mmm);

	// Longitude of the moon
	float lambdaMm = cuMu.norm360Deg(Lmm + sumL / 1000000);

	// Latitude of the moon
	float betaM = sumB / 1000000;

	// Distance earth-moon
	float dEM = 385000.56 + sumR / 1000;

	// Apparent longitude of the moon
	data->lambdaMapp = lambdaMm + data->deltaPsi;

	// Right ascension of the moon, apparent
	data->RAMoon = cuMu.toDegrees(cuMu.norm2PiRad(atan2((cuMu.sind(data->lambdaMapp) * cuMu.cosd(data->eps) - cuMu.tand(betaM) * cuMu.sind(data->eps)), cuMu.cosd(data->lambdaMapp))));

	// Declination of the moon
	data->DECMoon = cuMu.toDegrees(asin(cuMu.sind(betaM) * cuMu.cosd(data->eps) + cuMu.cosd(betaM) * cuMu.sind(data->eps) * cuMu.sind(data->lambdaMapp)));

	// GHA of the moon
	data->GHAMoon = cuMu.norm360Deg(data->GHAAtrue - data->RAMoon);

	// Horizontal parallax of the moon
	data->HPMoon = cuMu.toDegrees(3600 * asin(6378.14 / dEM));

	// Semi-diameter of the moon
	data->SDMoon = cuMu.toDegrees(3600 * asin(1738 / dEM));

	// Geocentric angular distance between moon and sun
	data->LDist = cuMu.toDegrees(acos(cuMu.sind(data->DECMoon) * cuMu.sind(data->DECSun) + cuMu.cosd(data->DECMoon) * cuMu.cosd(data->DECSun) * cuMu.cosd(data->RAMoon - data->RASun)));

	//Phase angle
	float i = atan2(data->dES * cuMu.sind(data->LDist), (dEM - data->dES * cuMu.cosd(data->LDist)));

	//Illumination of the moon's disk
	float k = 100 * (1 + cos(i)) / 2;
	data->illumMoon = round(10 * k) / 10;
}

void calculateWeekDay() {
  data->JD0h += 1.5;
  float res = data->JD0h - 7 * floor(data->JD0h / 7);
  if (res == 0) {
    data->DoW = "SUN";
  }
  if (res == 1) {
    data->DoW = "MON";
  }
  if (res == 2) {
    data->DoW = "TUE";
  }
  if (res == 3) {
    data->DoW = "WED";
  }
  if (res == 4) {
    data->DoW = "THU";
  }
  if (res == 5) {
    data->DoW = "FRI";
  }
  if (res == 6) {
    data->DoW = "SAT";
  }
}