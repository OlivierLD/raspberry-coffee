/*
 * Oliv proudly did it.
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <wiringPi.h>

/*
 * Pin numbers to use (wPi column):
 * +-----+-----+---------+------+---+---Pi 3B+-+---+------+---------+-----+-----+
 * | BCM | wPi |   Name  | Mode | V | Physical | V | Mode | Name    | wPi | BCM |
 * +-----+-----+---------+------+---+----++----+---+------+---------+-----+-----+
 * |     |     |    3.3v |      |   |  1 || 2  |   |      | 5v      |     |     |
 * |   2 |   8 |   SDA.1 | ALT0 | 1 |  3 || 4  |   |      | 5v      |     |     |
 * |   3 |   9 |   SCL.1 | ALT0 | 1 |  5 || 6  |   |      | 0v      |     |     |
 * |   4 |   7 | GPIO. 7 |   IN | 1 |  7 || 8  | 0 | OUT  | TxD     | 15  | 14  |
 * |     |     |      0v |      |   |  9 || 10 | 0 | OUT  | RxD     | 16  | 15  |
 * |  17 |   0 | GPIO. 0 |   IN | 0 | 11 || 12 | 0 | OUT  | GPIO. 1 | 1   | 18  |
 * |  27 |   2 | GPIO. 2 |   IN | 0 | 13 || 14 |   |      | 0v      |     |     |
 * |  22 |   3 | GPIO. 3 |   IN | 0 | 15 || 16 | 0 | IN   | GPIO. 4 | 4   | 23  |
 * |     |     |    3.3v |      |   | 17 || 18 | 0 | OUT  | GPIO. 5 | 5   | 24  |
 * |  10 |  12 |    MOSI | ALT0 | 0 | 19 || 20 |   |      | 0v      |     |     |
 * |   9 |  13 |    MISO | ALT0 | 0 | 21 || 22 | 1 | OUT  | GPIO. 6 | 6   | 25  |
 * |  11 |  14 |    SCLK | ALT0 | 0 | 23 || 24 | 1 | OUT  | CE0     | 10  | 8   |
 * |     |     |      0v |      |   | 25 || 26 | 1 | OUT  | CE1     | 11  | 7   |
 * |   0 |  30 |   SDA.0 |   IN | 1 | 27 || 28 | 1 | IN   | SCL.0   | 31  | 1   |
 * |   5 |  21 | GPIO.21 |  OUT | 1 | 29 || 30 |   |      | 0v      |     |     |
 * |   6 |  22 | GPIO.22 |   IN | 1 | 31 || 32 | 0 | IN   | GPIO.26 | 26  | 12  |
 * |  13 |  23 | GPIO.23 |   IN | 0 | 33 || 34 |   |      | 0v      |     |     |
 * |  19 |  24 | GPIO.24 |   IN | 0 | 35 || 36 | 0 | IN   | GPIO.27 | 27  | 16  |
 * |  26 |  25 | GPIO.25 |   IN | 0 | 37 || 38 | 0 | IN   | GPIO.28 | 28  | 20  |
 * |     |     |      0v |      |   | 39 || 40 | 0 | IN   | GPIO.29 | 29  | 21  |
 * +-----+-----+---------+------+---+----++----+---+------+---------+-----+-----+
 * | BCM | wPi |   Name  | Mode | V | Physical | V | Mode | Name    | wPi | BCM |
 * +-----+-----+---------+------+---+---Pi 3B+-+---+------+---------+-----+-----+
 *
 */

/* Pin numbers: 
 *   pin 1 is BCM_GPIO 18. (CLK)
 *   pin 4 is BCM_GPIO 23. (MISO)
 *   pin 5 is BCM_GPIO 24. (MOSI)
 *   pin 6 is BCM_GPIO 25. (CS)
 */
//#define	SPI_CLK	 1 // Clock
//#define SPI_MISO 4 // Master In Slave Out
//#define SPI_MOSI 5 // Master Out Slave In
//#define SPI_CS   6 // Chip Select

/*
 * "Default" pins (for JOB, diozero, etc) would be
 * CLK:  BCM 11, wPi 14
 * MISO: BCM 09, wPi 13
 * MOSI: BCM 10, wPi 12
 * CS:   BCM 07 or 08, wPi 11 or 10 (CE1, CE0)
 */

#define	SPI_CLK	 14 // Clock
#define SPI_MISO 13 // Master In Slave Out
#define SPI_MOSI 12 // Master Out Slave In
#define SPI_CS   10 // Chip Select

#define ADC_CHANNEL 0 // 0 to 7, 8 channels on the MCP3008 
#define DISPLAY_DIGIT 0

void initMPC3008(void) {
  wiringPiSetup();
  pinMode(SPI_CLK,  OUTPUT);
  pinMode(SPI_MOSI, OUTPUT);
  pinMode(SPI_CS,   OUTPUT);

  digitalWrite(SPI_CLK,  LOW);
  digitalWrite(SPI_MOSI, LOW);
  digitalWrite(SPI_CS,   LOW);

  pinMode (SPI_MISO, INPUT);
}

int readMCP3008(int channel) {
  int i;
  digitalWrite(SPI_CS, HIGH);

  digitalWrite(SPI_CLK, LOW);
  digitalWrite(SPI_CS,  LOW);

  int adcCommand = channel;
  adcCommand |= 0x18; // 0x18 = 00011000
  adcCommand <<= 3;
  // Send 5 bits: 8 - 3. 8 input channels on the MCP3008.
  for (i=0; i<5; i++) {
    if (DISPLAY_DIGIT) {
      fprintf(stdout, "(i=%d) ADCCOMMAND: 0x%04x\n", i, adcCommand);
    }
    if ((adcCommand & 0x80) != 0x0) { // 0x80 = 0&10000000
      digitalWrite(SPI_MOSI, HIGH);
    } else {
      digitalWrite(SPI_MOSI, LOW);
    }
    adcCommand <<= 1;   
    digitalWrite(SPI_CLK, HIGH);
    digitalWrite(SPI_CLK, LOW);
  }

  int adcOut = 0;
  for (i=0; i<12; i++) { // Read in one empty bit, one null bit and 10 ADC bits
    digitalWrite(SPI_CLK, HIGH);
    digitalWrite(SPI_CLK, LOW);
    adcOut <<= 1;

    if (digitalRead(SPI_MISO) == HIGH) {
      // Shift one bit on the adcOut
      adcOut |= 0x1;
    }
    if (DISPLAY_DIGIT) {
      fprintf(stdout, "ADCOUT: 0x%04x\n", (adcOut));
    }
  }
  digitalWrite(SPI_CS, HIGH);

  adcOut >>= 1; // Drop first bit
  return adcOut & 0x3FF; // 0x3FF = 2^10, 1024.  
}

