#include <iostream>
#include <stdio.h>
#include <string.h>

#include "AstroComputer.h"

// #include "CelestStruct.h"

const double DELTA_T = 69.2201;

int main () {
  std::cout << "Sample main, calculating for 2020-Mar-28 16:50:20\n";
  // 2020-MAR-28 16:50:20 UTC
  
  double deltaT = DELTA_T;
  // Recalculate deltaT
  deltaT = calculateDeltaT(2020, 3);
  fprintf(stdout, "Setting DeltaT to %f\n", deltaT);

  ComputedData * data = calculate(2020, 3, 28, 16, 50, 20, deltaT);
  fprintf(stdout, "--- Calculated 2020-Mar-28 16:50:20 ---\n");
  fprintf(stdout, "Julian Dates %f %f %f\n", data->JD0h, data->JD, data->JDE);
  fprintf(stdout, "Sideral Time %s\n", data->SidTm);
  char eot[32];
  fprintf(stdout, "EoT: %f => %s\n", data->EoT, outEoT(data->EoT, eot));
  char gha[32], ra[32], dec[32], sd[32], hp[32];
  fprintf(stdout, "---------------- Bodies ---------------\n");
  fprintf(stdout, "Sun \tGHA: %s, RA: %s, Dec: %s, sd: %s, hp: %s\n",
      outHA(data->GHASun, gha),
      outRA(data->RASun, ra),
      outDec(data->DECSun, dec),
      outSdHp(data->SDSun, sd),
      outSdHp(data->HPSun, hp));
  fprintf(stdout, "Venus \tGHA: %s, RA: %s, Dec: %s, sd: %s, hp: %s\n",
      outHA(data->GHAVenus, gha),
      outRA(data->RAVenus, ra),
      outDec(data->DECVenus, dec),
      outSdHp(data->SDVenus, sd),
      outSdHp(data->HPVenus, hp));
  fprintf(stdout, "Mars \tGHA: %s, RA: %s, Dec: %s, sd: %s, hp: %s\n",
      outHA(data->GHAMars, gha),
      outRA(data->RAMars, ra),
      outDec(data->DECMars, dec),
      outSdHp(data->SDMars, sd),
      outSdHp(data->HPMars, hp));
  fprintf(stdout, "Jupiter\tGHA: %s, RA: %s, Dec: %s, sd: %s, hp: %s\n",
      outHA(data->GHAJupiter, gha),
      outRA(data->RAJupiter, ra),
      outDec(data->DECJupiter, dec),
      outSdHp(data->SDJupiter, sd),
      outSdHp(data->HPJupiter, hp));
  fprintf(stdout, "Saturn \tGHA: %s, RA: %s, Dec: %s, sd: %s, hp: %s\n",
      outHA(data->GHASaturn, gha),
      outRA(data->RASaturn, ra),
      outDec(data->DECSaturn, dec),
      outSdHp(data->SDSaturn, sd),
      outSdHp(data->HPSaturn, hp));

  fprintf(stdout, "Moon \tGHA: %s, RA: %s, Dec: %s, sd: %s, hp: %s\n",
      outHA(data->GHAMoon, gha),
      outRA(data->RAMoon, ra),
      outDec(data->DECMoon, dec),
      outSdHp(data->SDMoon, sd),
      outSdHp(data->HPMoon, hp));
  fprintf(stdout, "\tMoon phase: %f, %s\n", data->moonPhaseAngle, data->moonPhase);
  fprintf(stdout, "Polaris\tGHA: %s, RA: %s, Dec: %s\n",
      outHA(data->GHAPol, gha),
      outRA(data->RAPol, ra),
      outDec(data->DECPol, dec));
  fprintf(stdout, "Ecliptic obliquity %s, true %s\n", outECL(data->eps0, ra), outECL(data->eps, dec));
  fprintf(stdout, "Lunar Distance %s\n", outHA(data->LDist, gha));
  fprintf(stdout, "Day of Week %s\n", data->DoW);
  fprintf(stdout, "---------------------------------------\n");
  std::cout << "Done with C!\n";
}
