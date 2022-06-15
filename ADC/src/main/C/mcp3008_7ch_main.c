/*
 * Compile with 
 * gcc -l wiringPi -o readMCP3008_7 mcp3008reader.c mcp3008_7ch_main.c
 ***********************************************************************
 * Oliv proudly did it.
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <wiringPi.h>
#include "mcp3008reader.h"

#define ADC_CHANNEL_0 0
#define ADC_CHANNEL_1 1
#define ADC_CHANNEL_2 2
#define ADC_CHANNEL_3 3
#define ADC_CHANNEL_4 4
#define ADC_CHANNEL_5 5
#define ADC_CHANNEL_6 6

int main(void) {
  fprintf(stdout, "Raspberry Pi reads an ADC\n") ;

  initMPC3008();

  bool go = true;
  int lastRead_0 = -100, lastRead_1 = -100, lastRead_2 = -100, 
      lastRead_3 = -100, lastRead_4 = -100, lastRead_5 = -100, lastRead_6 = -100;
  int tolerance = 10;
  while (go) {
    int pot_0 = readMCP3008(ADC_CHANNEL_0);
    int pot_1 = readMCP3008(ADC_CHANNEL_1);
    int pot_2 = readMCP3008(ADC_CHANNEL_2);
    int pot_3 = readMCP3008(ADC_CHANNEL_3);
    int pot_4 = readMCP3008(ADC_CHANNEL_4);
    int pot_5 = readMCP3008(ADC_CHANNEL_5);
    int pot_6 = readMCP3008(ADC_CHANNEL_6);

    int potAdjust_0 = abs(pot_0 - lastRead_0);
    int potAdjust_1 = abs(pot_1 - lastRead_1);
    int potAdjust_2 = abs(pot_2 - lastRead_2);
    int potAdjust_3 = abs(pot_3 - lastRead_3);
    int potAdjust_4 = abs(pot_4 - lastRead_4);
    int potAdjust_5 = abs(pot_5 - lastRead_5);
    int potAdjust_6 = abs(pot_6 - lastRead_6);

    if (potAdjust_0 > tolerance || potAdjust_1 > tolerance || potAdjust_2 > tolerance ||
        potAdjust_3 > tolerance || potAdjust_4 > tolerance || potAdjust_5 > tolerance || potAdjust_6 > tolerance) {
      int volume_0 = (int)((float)pot_0 / 10.23);
      int volume_1 = (int)((float)pot_1 / 10.23);
      int volume_2 = (int)((float)pot_2 / 10.23);
      int volume_3 = (int)((float)pot_3 / 10.23);
      int volume_4 = (int)((float)pot_4 / 10.23);
      int volume_5 = (int)((float)pot_5 / 10.23);
      int volume_6 = (int)((float)pot_6 / 10.23);
      fprintf(stdout, 
              "0:%03d %% value:%04d, 1:%03d %% value:%04d, 2:%03d %% value:%04d, 3:%03d %% value:%04d, 4:%03d %% value:%04d, 5:%03d %% value:%04d, 6:%03d %% value:%04d\n", 
              volume_0, pot_0, volume_1, pot_1, volume_2, pot_2,
              volume_3, pot_3, volume_4, pot_4, volume_5, pot_5, volume_6, pot_6); 
      lastRead_0 = pot_0;
      lastRead_1 = pot_0;
      lastRead_2 = pot_0;
      lastRead_3 = pot_0;
      lastRead_4 = pot_0;
      lastRead_5 = pot_0;
      lastRead_6 = pot_0;
    }
    delay (250);
  }
  return 0;
}

