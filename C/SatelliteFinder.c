/*
 * Compile with
 * gcc -o SatelliteFinder SatelliteFinder.c
 ***********************************************************************
 * Oliv proudly did it.
 */

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <float.h>
#include <string.h>

#define PI 3.14159265
#define R1 (1 + 35786 / 6378.16)

typedef struct {
  char name[48];
  double longitude;
} Satellite;

typedef struct {
  double zDegrees;
  double elevDegrees;
  double tilt;
} Result;

double toDegrees(double val) {
  return (180 / PI) * val;
}

double toRadians(double val) {
  return (PI / 180) * val;
}

double azimuth(double satLng, double earthStationLat, double earthStationLng) {
  double deltaG = toRadians(earthStationLng - satLng);
  double earthStationAzimuth = 180 + toDegrees(atan(tan(deltaG) / sin((toRadians(earthStationLat)))));
  if (earthStationLat < 0)
    earthStationAzimuth -= 180;
  if (earthStationAzimuth < 0)
    earthStationAzimuth += 360;
  return earthStationAzimuth;
}

double elevation(double satLong, double earthStationLat, double earthStationLong) {
  double deltaG = toRadians(earthStationLong - satLong);

  double latRad = toRadians(earthStationLat);
  double v1 = R1 * cos(latRad) * cos(deltaG) - 1;
  double v2 = R1 * sqrt(1 - cos(latRad) * cos(latRad) * cos(deltaG) * cos(deltaG));
  double earthStationElevation = toDegrees(atan(v1/v2));
  return earthStationElevation;
}

double tilt(double satLong, double earthStationLat, double earthStationLong) {
  double deltaG = toRadians(earthStationLong - satLong);
  double latRad = toRadians(earthStationLat);
  return (toDegrees(atan(sin(deltaG) / tan(latRad))));
}

Result aim(Satellite target, double fromLDegrees, double fromGDegrees) {
  double zDeg  = azimuth(target.longitude, fromLDegrees, fromGDegrees);
  double elDeg = elevation(target.longitude, fromLDegrees, fromGDegrees);
  double tlt  = tilt(target.longitude, fromLDegrees, fromGDegrees);
  Result result = { zDeg, elDeg, tlt };
  return result;
}

#define LAT_PRM "-lat:"
#define LNG_PRM "-lng:"

char * toHexStr(double val, char type) {
  char sgn = (val < 0) ? (type == 'L' ? 'S' : 'W') : (type == 'L' ? 'N' : 'E');
  int deg = (int) fabs(val);
  double min = fabs((val - (val > 0 ? deg : -deg)) * 60);
  char *str = (char *) malloc(sizeof(char) * 48);
  memset(str, 0, 48);
  sprintf(str, "%d°%.02f %c", deg, min, sgn);
//fprintf(stdout, ">>>  %s\n", str);
  return str;
}

int main(int argc, char **argv) {
// 2010 48th Ave, SF
  double lat = 37.7489;
  double lng = -122.5070;

//fprintf(stdout, "Received %d arguments\n", argc);
  int arg;
  for (arg=1; arg<argc; arg++) {
    char * argument = argv[arg];
    fprintf(stdout, "Checking %s... ", argument);
    if (strncmp(LAT_PRM, argument, strlen(LAT_PRM)) == 0) {
      char latPrm[30];
      strcpy(latPrm, &argument[strlen(LAT_PRM)]);
      fprintf(stdout, "Using Lat [%s]\n", latPrm);
      lat = atof(latPrm);
    } else if (strncmp(LNG_PRM, argument, strlen(LNG_PRM)) == 0) {
      char lngPrm[30];
      strcpy(lngPrm, &argument[strlen(LNG_PRM)]);
      fprintf(stdout, "Using Lng [%s]\n", lngPrm);
      lng = atof(lngPrm);
    } else {
      fprintf(stdout, "Ignored [%s]\n", argument);
    }
  }

  const Satellite one   = { "I-4 F1 Asia-Pacific", 143.5 };
  const Satellite two   = { "I-4 F2 EMEA (Europe, Middle East and Africa)", 63.0 };
  const Satellite three = { "I-4 F3 Americas", -97.6 };
  const Satellite four  = { "Alphasat", 24.9 };

  const Satellite satellites[] = { one, two, three, four };

  char * fmtLat = toHexStr(lat, 'L');
  char * fmtLng = toHexStr(lng, 'G');
  fprintf(stdout, "Finding the right satellite from %s / %s...\n", fmtLat, fmtLng);
  free(fmtLat);
  free(fmtLng);
  int i;
//  for (i=0; i<4; i++) {
//    fprintf(stdout, "%s\n", satellites[i].name);
//  }

  Satellite toUse;
  Result finalResult;
  double maxAlt = -DBL_MAX;
  for (i=0; i<4; i++) {
    Result result = aim(satellites[i], lat, lng);
//  fprintf(stdout, "-> %s: Z: %f° (true), el: %f°, tilt: %f°\n", satellites[i].name, result.zDegrees, result.elevDegrees, result.tilt);
    if (result.elevDegrees > maxAlt) {
      maxAlt = result.elevDegrees;
      toUse = satellites[i];
      finalResult = result;
    }
  }
  fprintf(stdout, "\nuse %s: El %.02f°, Z %.02f° (true), Tilt %.02f°\n", toUse.name, finalResult.elevDegrees, finalResult.zDegrees, finalResult.tilt);
  return 0; // All good!
}

