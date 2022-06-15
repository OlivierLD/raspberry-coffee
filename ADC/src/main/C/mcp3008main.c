/*
 * Compile with 
 * gcc -l wiringPi -o readMCP3008 mcp3008reader.c mcp3008main.c
 ***********************************************************************
 * Oliv proudly did it.
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <wiringPi.h>
#include "mcp3008reader.h"

#define ADC_CHANNEL 0 // 0 to 7, 8 channels on the MCP3008 

int main(void) {
  fprintf(stdout, "Raspberry Pi reads an ADC\n") ;

  initMPC3008();

  bool go = true;
  int lastRead = -100;
  int tolerance = 10;
  while (go) {
    bool trimPotChanged = false;
    int pot = readMCP3008(ADC_CHANNEL);
    int potAdjust = abs(pot - lastRead);
    if (potAdjust > tolerance) {
      trimPotChanged = true;
      int volume = (int)((float)pot / 10.23);
      fprintf(stdout, "Pot volume:%d %% value:%d\n", volume, pot); 
      lastRead = pot;
    }
    delay (250);
  }
  return 0;
}

