#include <iostream>
#include <stdio.h>
#include <string.h>

#include "AstroComputer.h"

// #include "CelestStruct.h"

const double DELTA_T = 69.2201;

int main () {
  std::cout << "Sample main, calculating for 2020-Mar-28 16:50:20\n";
  // 2020-MAR-28 16:50:20 UTC
  ComputedData * data = calculate(2020, 3, 28, 16, 50, 20, DELTA_T);
  fprintf(stdout, "Julian Dates %f %f %f\n", data->JD0h, data->JD, data->JDE);
  fprintf(stdout, "Sideral Time %s\n", data->SidTm);
  fprintf(stdout, "Sun GHA: %f, %s, RA: %s\n", data->GHASun, outHA(data->GHASun), outRA(data->RASun));
  fprintf(stdout, "EoT: %f => %s\n", data->EoT, outEoT(data->EoT));
  std::cout << "Done!\n";
}
