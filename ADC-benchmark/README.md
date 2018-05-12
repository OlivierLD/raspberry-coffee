# ADC Benchmark

We want to compare several Analog to Digital Converters

- MCP3008 (10 bits)
- ADS1015 (12 bits)
- ADS1115 (16 bits)

### MCP3008

SPI, 8 channels of 10-bit analog input

As such, returns values between 0 and 2<sup><small>10</small></sup>, \[0..1023\]

![MCP3008](./RPi-MCP3008-Pot_bb.png)


```
 $ ./run --channel:5
Read an ADC for 3.3 Volt estimation
Usage is ./run --miso:9 --mosi:10 --clk:11 --cs:8 --channel:0
 For miso, mosi, clk & cs, use BCM pin numbers
Usage is java adcbenchmark.mcp3008.MainMCP3008Sample33 --miso:9 --mosi:10 --clk:11 --cs:8 --channel:0
Values above are default values (GPIO/BCM numbers).

Reading MCP3008 on channel 5
 Wiring of the MCP3008-SPI (without power supply):
 +---------++-----------------------------------------------+
 | MCP3008 || Raspberry PI                                  |
 +---------++------+------------+------+---------+----------+
 |         || Pin# | Name       | Role | GPIO    | wiringPI |
 |         ||      |            |      | /BCM    | /PI4J    |
 +---------++------+------------+------+---------+----------+
 | CLK (13)|| #23  | SPI0_CLK   | CLK  | GPIO_11 | 14       |
 | Din (11)|| #19  | SPI0_MOSI  | MOSI | GPIO_10 | 12       |
 | Dout(12)|| #21  | SPI0_MISO  | MISO | GPIO_09 | 13       |
 | CS  (10)|| #24  | SPI0_CS0_N | CS   | GPIO_08 | 10       |
 +---------++------+------------+-----+----------+----------+
Raspberry PI is the Master, MCP3008 is the Slave:
- Dout on the MCP3008 goes to MISO on the RPi
- Din on the MCP3008 goes to MOSI on the RPi
Pins on the MCP3008 are numbered from 1 to 16, beginning top left, counter-clockwise.
       +--------+
  CH0 -+  1  16 +- Vdd
  CH1 -+  2  15 +- Vref
  CH2 -+  3  14 +- aGnd
  CH3 -+  4  13 +- CLK
  CH4 -+  5  12 +- Dout
* CH5 -+  6  11 +- Din
  CH6 -+  7  10 +- CS
  CH7 -+  8   9 +- dGnd
       +--------+
       +-----+-----+--------------+-----++-----+--------------+-----+-----+
       | BCM | wPi | Name         |  Physical  |         Name | wPi | BCM |
       +-----+-----+--------------+-----++-----+--------------+-----+-----+
       |     |     | 3v3          | #01 || #02 |          5v0 |     |     |
       |  02 |  08 | SDA1         | #03 || #04 |          5v0 |     |     |
       |  03 |  09 | SCL1         | #05 || #06 |          GND |     |     |
       |  04 |  07 | GPCLK0       | #07 || #08 |    UART0_TXD | 15  | 14  |
       |     |     | GND          | #09 || #10 |    UART0_RXD | 16  | 15  |
       |  17 |  00 | GPIO_0       | #11 || #12 | PCM_CLK/PWM0 | 01  | 18  |
       |  27 |  02 | GPIO_2       | #13 || #14 |          GND |     |     |
       |  22 |  03 | GPIO_3       | #15 || #16 |       GPIO_4 | 04  | 23  |
       |     |     | 3v3          | #01 || #18 |       GPIO_5 | 05  | 24  |
   Din |  10 |  12 | SPI0_MOSI    | #19 || #20 |          GND |     |     |
  Dout |  09 |  13 | SPI0_MISO    | #21 || #22 |       GPIO_6 | 06  | 25  |
   CLK |  11 |  14 | SPI0_CLK     | #23 || #24 |   SPI0_CS0_N | 10  | 08  | CS
       |     |     | GND          | #25 || #26 |   SPI0_CS1_N | 11  | 07  |
       |     |  30 | SDA0         | #27 || #28 |         SCL0 | 31  |     |
       |  05 |  21 | GPCLK1       | #29 || #30 |          GND |     |     |
       |  06 |  22 | GPCLK2       | #31 || #32 |         PWM0 | 26  | 12  |
       |  13 |  23 | PWM1         | #33 || #34 |          GND |     |     |
       |  19 |  24 | PCM_FS/PWM1  | #35 || #36 |      GPIO_27 | 27  | 16  |
       |  26 |  25 | GPIO_25      | #37 || #38 |      PCM_DIN | 28  | 20  |
       |     |     | GND          | #39 || #40 |     PCM_DOUT | 29  | 21  |
       +-----+-----+--------------+-----++-----+--------------+-----+-----+
       | BCM | wPi | Name         |  Physical  |         Name | wPi | BCM |
       +-----+-----+--------------+-----++-----+--------------+-----+-----+
Volume: 100% (1023) => 3.30 V
Volume: 099% (1013) => 3.27 V
Volume: 094% (0969) => 3.10 V
Volume: 088% (0906) => 2.90 V
Volume: 082% (0849) => 2.71 V
Volume: 079% (0813) => 2.61 V
Volume: 076% (0783) => 2.51 V
Volume: 074% (0759) => 2.44 V
Volume: 071% (0732) => 2.34 V
Volume: 068% (0705) => 2.24 V
Volume: 068% (0698) => 2.24 V
Volume: 065% (0673) => 2.15 V
Volume: 062% (0638) => 2.05 V
Volume: 059% (0611) => 1.95 V
Volume: 055% (0572) => 1.82 V
Volume: 051% (0527) => 1.68 V
Volume: 047% (0488) => 1.55 V
Volume: 045% (0464) => 1.48 V
Volume: 041% (0426) => 1.35 V
Volume: 039% (0402) => 1.29 V
Volume: 037% (0386) => 1.22 V
Volume: 035% (0367) => 1.15 V
Volume: 035% (0360) => 1.15 V
Volume: 037% (0382) => 1.22 V
Volume: 040% (0416) => 1.32 V
Volume: 043% (0441) => 1.42 V
Volume: 044% (0452) => 1.45 V
Volume: 044% (0458) => 1.45 V
^CShutting down.
 $
```

### ADS1015
I<sup><small>2</small></sup>C, 4 channels of 12-bit analog input

As such, returns values between 0 and 2<sup><small>12</small></sup>, \[0..4095\]

### ADS1115
I<sup><small>2</small></sup>C, 4 channels of 16-bit analog input

As such, returns values between 0 and 2<sup><small>16</small></sup>, \[0..65535\]

![ADS1115](./rpi-ads1115-pot_bb.png)

---
