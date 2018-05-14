# ADC Benchmark

We want to compare several Analog to Digital Converters

- MCP3008 (10 bits) (May 2018: $ 3.75)
- ADS1015 (12 bits) (May 2018: $ 9.95)
- ADS1115 (16 bits) (May 2018: $14.95)

The final goal for this case study will be to evaluate the orientation of a panel (like a solar panel),
using a _linear_ potentiometer like [this one](https://www.adafruit.com/product/562).

This potentiometer rotates on about 300&deg;.

We will call the center (150&deg;) the zero. Thus, reading will go from -150&deg; to +150&deg;.

Servos are _not_ an option here, as the panel we want to orient is too heavy.
Stepper motors are required.

We will need two potentiometers, one for the tilt (rotation on an horizontal axis),
and another one for the azimuth (rotation on a vertical axis).

A precision of one or two degrees will happen to be sufficient in this use-case.

### MCP3008
##### SPI, 8 channels of 10-bit analog input

As such, returns 2<sup><small>10</small></sup> values, in \[0..1023\].

![MCP3008](./RPi-MCP3008-Pot_bb.png)


```
 $ ./run --channel:5
Read an ADC for 3.3 Volt estimation
+------------+
| 1: MCP3008 |
| 2: ADS1015 |
| 3: ADS1115 |
+------------+
| Q: Bye     |
+------------+
 You choose > 1

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
Volume: 050% (0513) => 1.650 V, +00 degree(s)
Volume: 049% (0505) => 1.617 V, -03 degree(s)
Volume: 049% (0511) => 1.617 V, -03 degree(s)
Volume: 051% (0522) => 1.683 V, +03 degree(s)
Volume: 052% (0539) => 1.716 V, +06 degree(s)
Volume: 054% (0553) => 1.782 V, +12 degree(s)
Volume: 054% (0561) => 1.782 V, +12 degree(s)
Volume: 056% (0573) => 1.848 V, +18 degree(s)
Volume: 056% (0583) => 1.848 V, +18 degree(s)
Volume: 057% (0591) => 1.881 V, +21 degree(s)
Volume: 058% (0603) => 1.914 V, +24 degree(s)
Volume: 060% (0617) => 1.980 V, +30 degree(s)
Volume: 062% (0639) => 2.046 V, +36 degree(s)
Volume: 064% (0660) => 2.112 V, +42 degree(s)
Volume: 066% (0679) => 2.178 V, +48 degree(s)
Volume: 067% (0695) => 2.211 V, +51 degree(s)
Volume: 068% (0705) => 2.244 V, +54 degree(s)
Volume: 070% (0718) => 2.310 V, +60 degree(s)
Volume: 071% (0732) => 2.343 V, +63 degree(s)
Volume: 075% (0769) => 2.475 V, +75 degree(s)
Volume: 076% (0783) => 2.508 V, +78 degree(s)
Volume: 080% (0821) => 2.640 V, +90 degree(s)
Volume: 082% (0840) => 2.706 V, +96 degree(s)
Volume: 084% (0864) => 2.772 V, +102 degree(s)
Volume: 086% (0886) => 2.838 V, +108 degree(s)
Volume: 087% (0893) => 2.871 V, +111 degree(s)
Volume: 086% (0881) => 2.838 V, +108 degree(s)
Volume: 085% (0870) => 2.805 V, +105 degree(s)
Volume: 083% (0850) => 2.739 V, +99 degree(s)
Volume: 080% (0825) => 2.640 V, +90 degree(s)
Volume: 077% (0796) => 2.541 V, +81 degree(s)
Volume: 074% (0767) => 2.442 V, +72 degree(s)
Volume: 071% (0733) => 2.343 V, +63 degree(s)
Volume: 069% (0706) => 2.277 V, +57 degree(s)
Volume: 067% (0687) => 2.211 V, +51 degree(s)
Volume: 065% (0671) => 2.145 V, +45 degree(s)
Volume: 065% (0665) => 2.145 V, +45 degree(s)
Volume: 062% (0640) => 2.046 V, +36 degree(s)
Volume: 058% (0603) => 1.914 V, +24 degree(s)
Volume: 055% (0569) => 1.815 V, +15 degree(s)
Volume: 052% (0534) => 1.716 V, +06 degree(s)
Volume: 050% (0521) => 1.650 V, +00 degree(s)
Volume: 050% (0512) => 1.650 V, +00 degree(s)
Volume: 048% (0492) => 1.584 V, -06 degree(s)
Volume: 046% (0475) => 1.518 V, -12 degree(s)
Volume: 045% (0465) => 1.485 V, -15 degree(s)
Volume: 044% (0457) => 1.452 V, -18 degree(s)
Volume: 043% (0440) => 1.419 V, -21 degree(s)
Volume: 039% (0404) => 1.287 V, -33 degree(s)
Volume: 036% (0375) => 1.188 V, -42 degree(s)
Volume: 035% (0362) => 1.155 V, -45 degree(s)
Volume: 034% (0351) => 1.122 V, -48 degree(s)
Volume: 033% (0345) => 1.089 V, -51 degree(s)
Volume: 032% (0337) => 1.056 V, -54 degree(s)
Volume: 033% (0343) => 1.089 V, -51 degree(s)
^C
Shutting down.
Done.
 $
```

The quality of the potentiometer seems to be _the_ important parameter here.

![Chart](./chart.png)


### ADS1015
##### I<sup><small>2</small></sup>C, 4 channels of 12-bit analog input

As such, returns 2<sup><small>12</small></sup> values, in \[0..4095\].

```
 $ ./run
Read an ADC for 3.3 Volt estimation
+------------+
| 1: MCP3008 |
| 2: ADS1015 |
| 3: ADS1115 |
+------------+
| Q: Bye     |
+------------+
 You choose > 2
ADC Value: 0, Voltage: 0.00000
ADC Value: 2, Voltage: 0.00200
ADC Value: 6, Voltage: 0.00600
ADC Value: 12, Voltage: 0.01200
ADC Value: 14, Voltage: 0.01400
ADC Value: 20, Voltage: 0.02000
ADC Value: 24, Voltage: 0.02400
ADC Value: 28, Voltage: 0.02800
ADC Value: 32, Voltage: 0.03200
ADC Value: 36, Voltage: 0.03600
ADC Value: 40, Voltage: 0.04000
...
ADC Value: 134, Voltage: 0.13400
ADC Value: 140, Voltage: 0.14000
ADC Value: 146, Voltage: 0.14600
ADC Value: 154, Voltage: 0.15400
ADC Value: 160, Voltage: 0.16000
ADC Value: 162, Voltage: 0.16200
ADC Value: 172, Voltage: 0.17200
ADC Value: 174, Voltage: 0.17400
ADC Value: 176, Voltage: 0.17600
ADC Value: 182, Voltage: 0.18200
ADC Value: 190, Voltage: 0.19000
...
ADC Value: 308, Voltage: 0.30800
ADC Value: 310, Voltage: 0.31000
ADC Value: 314, Voltage: 0.31400
ADC Value: 318, Voltage: 0.31800
ADC Value: 320, Voltage: 0.32000
ADC Value: 324, Voltage: 0.32400
ADC Value: 328, Voltage: 0.32800
ADC Value: 330, Voltage: 0.33000
ADC Value: 334, Voltage: 0.33400
ADC Value: 336, Voltage: 0.33600
ADC Value: 338, Voltage: 0.33800
ADC Value: 342, Voltage: 0.34200
ADC Value: 346, Voltage: 0.34600
...
ADC Value: 1914, Voltage: 1.91400
ADC Value: 1920, Voltage: 1.92000
ADC Value: 1926, Voltage: 1.92600
ADC Value: 1936, Voltage: 1.93600
ADC Value: 1944, Voltage: 1.94400
ADC Value: 1950, Voltage: 1.95000
ADC Value: 1964, Voltage: 1.96400
ADC Value: 1970, Voltage: 1.97000
ADC Value: 1980, Voltage: 1.98000
ADC Value: 1994, Voltage: 1.99400
ADC Value: 1998, Voltage: 1.99800
ADC Value: 2008, Voltage: 2.00800
ADC Value: 2020, Voltage: 2.02000
ADC Value: 2028, Voltage: 2.02800
...
ADC Value: 3230, Voltage: 3.23000
ADC Value: 3234, Voltage: 3.23400
ADC Value: 3238, Voltage: 3.23800
ADC Value: 3236, Voltage: 3.23600
ADC Value: 3232, Voltage: 3.23200
ADC Value: 3234, Voltage: 3.23400
ADC Value: 3232, Voltage: 3.23200
ADC Value: 3234, Voltage: 3.23400
ADC Value: 3238, Voltage: 3.23800
ADC Value: 3240, Voltage: 3.24000
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
...
^CDone.
 $
```

### ADS1115
##### I<sup><small>2</small></sup>C, 4 channels of 16-bit analog input

As such, returns 2<sup><small>16</small></sup> values, inb \[0..65535\].

![ADS1115](./rpi-ads1115-pot_bb.png)

## Discussion
If the goal here is still to read the orientation of a (solar) panel, it looks like the
three ADCs work in a similar fashion. There is apparently no real advantage in using one instead of the others.

The key seems to be more on the potentiometer quality.
Making sure that we obtain the same readings when the potentiometer is in the same position
is what will make the difference. The spreadsheet graph above shows thiose values.
It is _close_ to being linear.


---
