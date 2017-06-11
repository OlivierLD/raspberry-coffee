/*
 * Compile with
 * gcc -l wiringPi -o pwm01 pwm01.c
 ***********************************************************************
 * Oliv proudly did it.
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <wiringPi.h>

int main (void) {
   printf ("Raspberry Pi PWM wiringPi test program\n");
   wiringPiSetupGpio();
   pinMode (18, PWM_OUTPUT);
   pwmSetMode (PWM_MODE_MS);
   pwmSetRange (2000);
   pwmSetClock (192);
   pwmWrite(18,150);
   delay(1000);
   pwmWrite(18,200);
   return 0;
}