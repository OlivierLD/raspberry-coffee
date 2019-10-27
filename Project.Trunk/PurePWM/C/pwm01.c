/*
 * Compile with
 * gcc -l wiringPi -o pwm01C pwm01.c
 * run with sudo.
 ***********************************************************************
 * Oliv proudly did it.
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <wiringPi.h>

int main (void) {
   fprintf(stdout, "Raspberry Pi PWM wiringPi test program\n");
   wiringPiSetupGpio();
   pinMode (18, PWM_OUTPUT); // Pin #12
   pwmSetMode (PWM_MODE_MS);
   pwmSetRange (2000);
   pwmSetClock (192);
   fprintf(stdout, "\tWriting 150\n");
   pwmWrite(18, 150);
   delay(1000);
   fprintf(stdout, "\tWriting 200\n");
   pwmWrite(18, 200);
   delay(1000);
   fprintf(stdout, "\tWriting 2000\n");
   pwmWrite(18, 2000);
   delay(1000);
   fprintf(stdout, "\tWriting 200\n");
   pwmWrite(18, 200);
   fprintf(stdout, "Done\n");
   return 0;
}
