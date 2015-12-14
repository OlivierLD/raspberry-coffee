/*
 * Oliv proudly did it.
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <wiringPi.h>

/* Pin numbers: 
 *   pin 1 is BCM_GPIO 18.
 *   pin 4 is BCM_GPIO 23.
 *   pin 5 is BCM_GPIO 24.
 *   pin 6 is BCM_GPIO 25.
 */
#define	SPI_CLK	 1 // Clock
#define SPI_MISO 4 // Master In Slave Out
#define SPI_MOSI 5 // Master Out Slave In
#define SPI_CS   6 // Chip Select 

#define ADC_CHANNEL 0 // 0 to 7, 8 channels on the MCP3008 
#define DISPLAY_DIGIT 0

void initMPC3008(void)
{
  wiringPiSetup();
  pinMode(SPI_CLK,  OUTPUT);
  pinMode(SPI_MOSI, OUTPUT);
  pinMode(SPI_CS,   OUTPUT);

  digitalWrite(SPI_CLK,  LOW);
  digitalWrite(SPI_MOSI, LOW);
  digitalWrite(SPI_CS,   LOW);

  pinMode (SPI_MISO, INPUT);
}

int readMCP3008(int channel)
{
  int i;
  digitalWrite(SPI_CS, HIGH);

  digitalWrite(SPI_CLK, LOW);
  digitalWrite(SPI_CS,  LOW);

  int adcCommand = channel;
  adcCommand |= 0x18; // 0x18 = 00011000
  adcCommand <<= 3;
  // Send 5 bits: 8 - 3. 8 input channels on the MCP3008.
  for (i=0; i<5; i++)
  {
    if (DISPLAY_DIGIT)
      fprintf(stdout, "(i=%d) ADCCOMMAND: 0x%04x\n", i, adcCommand);
    if ((adcCommand & 0x80) != 0x0) // 0x80 = 0&10000000
      digitalWrite(SPI_MOSI, HIGH);
    else
      digitalWrite(SPI_MOSI, LOW);
    adcCommand <<= 1;   
    digitalWrite(SPI_CLK, HIGH);
    digitalWrite(SPI_CLK, LOW);
  }

  int adcOut = 0;
  for (i=0; i<12; i++) // Read in one empty bit, one null bit and 10 ADC bits
  {
    digitalWrite(SPI_CLK, HIGH);
    digitalWrite(SPI_CLK, LOW);
    adcOut <<= 1;

    if (digitalRead(SPI_MISO) == HIGH)
    {
      // Shift one bit on the adcOut
      adcOut |= 0x1;
    }
    if (DISPLAY_DIGIT)
      fprintf(stdout, "ADCOUT: 0x%04x\n", (adcOut));
  }
  digitalWrite(SPI_CS, HIGH);

  adcOut >>= 1; // Drop first bit
  return adcOut & 0x3FF; // 0x3FF = 2^10, 1024.  
}

