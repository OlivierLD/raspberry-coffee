# ADC Benchmark

We want to compare several Analog to Digital Converters

- MCP3008 (10 bits) $ 3.75
- ADS1015 (12 bits) $ 9.95
- ADS1115 (16 bits) $14.95

The final goal will be to evaluate the orientation of a panel (like a solar panel),
using a potentiometer like [this one](https://www.adafruit.com/product/562).

This potentiometer rotates on a out 300&deg;.

We will call the center (150&deg;) the zero. Thus, reading will got from -150&deg; to +150&deg;.

Servos are _not_ an option here, as the panel we want to orient is too heavy.
Stepper motors are required.

We will need two potentiometers, one for the tilt (rotation on an horizontal axis),
and another one for the azimuth (rotation on a vertical axis).

A precision of one degree will happen to be sufficient in this case.

### MCP3008

SPI, 8 channels of 10-bit analog input

As such, returns values between 0 and 2<sup><small>10</small></sup>, \[0..1023\]

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
Done.
 $
```

### ADS1015
I<sup><small>2</small></sup>C, 4 channels of 12-bit analog input

As such, returns values between 0 and 2<sup><small>12</small></sup>, \[0..4095\]

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
ADC Value: 44, Voltage: 0.04400
ADC Value: 46, Voltage: 0.04600
ADC Value: 48, Voltage: 0.04800
ADC Value: 54, Voltage: 0.05400
ADC Value: 56, Voltage: 0.05600
ADC Value: 60, Voltage: 0.06000
ADC Value: 62, Voltage: 0.06200
ADC Value: 66, Voltage: 0.06600
ADC Value: 68, Voltage: 0.06800
ADC Value: 70, Voltage: 0.07000
ADC Value: 72, Voltage: 0.07200
ADC Value: 78, Voltage: 0.07800
ADC Value: 80, Voltage: 0.08000
ADC Value: 82, Voltage: 0.08200
ADC Value: 86, Voltage: 0.08600
ADC Value: 90, Voltage: 0.09000
ADC Value: 94, Voltage: 0.09400
ADC Value: 98, Voltage: 0.09800
ADC Value: 100, Voltage: 0.10000
ADC Value: 104, Voltage: 0.10400
ADC Value: 106, Voltage: 0.10600
ADC Value: 112, Voltage: 0.11200
ADC Value: 116, Voltage: 0.11600
ADC Value: 122, Voltage: 0.12200
ADC Value: 126, Voltage: 0.12600
ADC Value: 132, Voltage: 0.13200
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
ADC Value: 194, Voltage: 0.19400
ADC Value: 196, Voltage: 0.19600
ADC Value: 202, Voltage: 0.20200
ADC Value: 206, Voltage: 0.20600
ADC Value: 210, Voltage: 0.21000
ADC Value: 214, Voltage: 0.21400
ADC Value: 220, Voltage: 0.22000
ADC Value: 224, Voltage: 0.22400
ADC Value: 228, Voltage: 0.22800
ADC Value: 230, Voltage: 0.23000
ADC Value: 234, Voltage: 0.23400
ADC Value: 236, Voltage: 0.23600
ADC Value: 242, Voltage: 0.24200
ADC Value: 248, Voltage: 0.24800
ADC Value: 250, Voltage: 0.25000
ADC Value: 254, Voltage: 0.25400
ADC Value: 256, Voltage: 0.25600
ADC Value: 258, Voltage: 0.25800
ADC Value: 262, Voltage: 0.26200
ADC Value: 266, Voltage: 0.26600
ADC Value: 270, Voltage: 0.27000
ADC Value: 272, Voltage: 0.27200
ADC Value: 276, Voltage: 0.27600
ADC Value: 280, Voltage: 0.28000
ADC Value: 284, Voltage: 0.28400
ADC Value: 288, Voltage: 0.28800
ADC Value: 292, Voltage: 0.29200
ADC Value: 294, Voltage: 0.29400
ADC Value: 298, Voltage: 0.29800
ADC Value: 300, Voltage: 0.30000
ADC Value: 306, Voltage: 0.30600
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
ADC Value: 348, Voltage: 0.34800
ADC Value: 350, Voltage: 0.35000
ADC Value: 354, Voltage: 0.35400
ADC Value: 356, Voltage: 0.35600
ADC Value: 360, Voltage: 0.36000
ADC Value: 362, Voltage: 0.36200
ADC Value: 364, Voltage: 0.36400
ADC Value: 368, Voltage: 0.36800
ADC Value: 372, Voltage: 0.37200
ADC Value: 374, Voltage: 0.37400
ADC Value: 378, Voltage: 0.37800
ADC Value: 380, Voltage: 0.38000
ADC Value: 382, Voltage: 0.38200
ADC Value: 384, Voltage: 0.38400
ADC Value: 386, Voltage: 0.38600
ADC Value: 388, Voltage: 0.38800
ADC Value: 392, Voltage: 0.39200
ADC Value: 396, Voltage: 0.39600
ADC Value: 400, Voltage: 0.40000
ADC Value: 402, Voltage: 0.40200
ADC Value: 404, Voltage: 0.40400
ADC Value: 406, Voltage: 0.40600
ADC Value: 410, Voltage: 0.41000
ADC Value: 412, Voltage: 0.41200
ADC Value: 414, Voltage: 0.41400
ADC Value: 416, Voltage: 0.41600
ADC Value: 418, Voltage: 0.41800
ADC Value: 420, Voltage: 0.42000
ADC Value: 424, Voltage: 0.42400
ADC Value: 426, Voltage: 0.42600
ADC Value: 428, Voltage: 0.42800
ADC Value: 430, Voltage: 0.43000
ADC Value: 432, Voltage: 0.43200
ADC Value: 436, Voltage: 0.43600
ADC Value: 438, Voltage: 0.43800
ADC Value: 440, Voltage: 0.44000
ADC Value: 442, Voltage: 0.44200
ADC Value: 444, Voltage: 0.44400
ADC Value: 446, Voltage: 0.44600
ADC Value: 450, Voltage: 0.45000
ADC Value: 452, Voltage: 0.45200
ADC Value: 454, Voltage: 0.45400
ADC Value: 456, Voltage: 0.45600
ADC Value: 460, Voltage: 0.46000
ADC Value: 464, Voltage: 0.46400
ADC Value: 466, Voltage: 0.46600
ADC Value: 468, Voltage: 0.46800
ADC Value: 470, Voltage: 0.47000
ADC Value: 472, Voltage: 0.47200
ADC Value: 474, Voltage: 0.47400
ADC Value: 476, Voltage: 0.47600
ADC Value: 478, Voltage: 0.47800
ADC Value: 480, Voltage: 0.48000
ADC Value: 482, Voltage: 0.48200
ADC Value: 484, Voltage: 0.48400
ADC Value: 488, Voltage: 0.48800
ADC Value: 490, Voltage: 0.49000
ADC Value: 492, Voltage: 0.49200
ADC Value: 494, Voltage: 0.49400
ADC Value: 496, Voltage: 0.49600
ADC Value: 498, Voltage: 0.49800
ADC Value: 500, Voltage: 0.50000
ADC Value: 502, Voltage: 0.50200
ADC Value: 504, Voltage: 0.50400
ADC Value: 506, Voltage: 0.50600
ADC Value: 508, Voltage: 0.50800
ADC Value: 510, Voltage: 0.51000
ADC Value: 512, Voltage: 0.51200
ADC Value: 514, Voltage: 0.51400
ADC Value: 516, Voltage: 0.51600
ADC Value: 518, Voltage: 0.51800
ADC Value: 520, Voltage: 0.52000
ADC Value: 522, Voltage: 0.52200
ADC Value: 524, Voltage: 0.52400
ADC Value: 526, Voltage: 0.52600
ADC Value: 528, Voltage: 0.52800
ADC Value: 530, Voltage: 0.53000
ADC Value: 532, Voltage: 0.53200
ADC Value: 534, Voltage: 0.53400
ADC Value: 536, Voltage: 0.53600
ADC Value: 538, Voltage: 0.53800
ADC Value: 540, Voltage: 0.54000
ADC Value: 542, Voltage: 0.54200
ADC Value: 544, Voltage: 0.54400
ADC Value: 546, Voltage: 0.54600
ADC Value: 548, Voltage: 0.54800
ADC Value: 550, Voltage: 0.55000
ADC Value: 552, Voltage: 0.55200
ADC Value: 554, Voltage: 0.55400
ADC Value: 556, Voltage: 0.55600
ADC Value: 558, Voltage: 0.55800
ADC Value: 560, Voltage: 0.56000
ADC Value: 562, Voltage: 0.56200
ADC Value: 566, Voltage: 0.56600
ADC Value: 568, Voltage: 0.56800
ADC Value: 570, Voltage: 0.57000
ADC Value: 572, Voltage: 0.57200
ADC Value: 574, Voltage: 0.57400
ADC Value: 576, Voltage: 0.57600
ADC Value: 578, Voltage: 0.57800
ADC Value: 580, Voltage: 0.58000
ADC Value: 582, Voltage: 0.58200
ADC Value: 584, Voltage: 0.58400
ADC Value: 586, Voltage: 0.58600
ADC Value: 588, Voltage: 0.58800
ADC Value: 592, Voltage: 0.59200
ADC Value: 594, Voltage: 0.59400
ADC Value: 596, Voltage: 0.59600
ADC Value: 598, Voltage: 0.59800
ADC Value: 600, Voltage: 0.60000
ADC Value: 602, Voltage: 0.60200
ADC Value: 606, Voltage: 0.60600
ADC Value: 608, Voltage: 0.60800
ADC Value: 610, Voltage: 0.61000
ADC Value: 612, Voltage: 0.61200
ADC Value: 614, Voltage: 0.61400
ADC Value: 616, Voltage: 0.61600
ADC Value: 618, Voltage: 0.61800
ADC Value: 622, Voltage: 0.62200
ADC Value: 624, Voltage: 0.62400
ADC Value: 626, Voltage: 0.62600
ADC Value: 628, Voltage: 0.62800
ADC Value: 630, Voltage: 0.63000
ADC Value: 632, Voltage: 0.63200
ADC Value: 638, Voltage: 0.63800
ADC Value: 642, Voltage: 0.64200
ADC Value: 644, Voltage: 0.64400
ADC Value: 646, Voltage: 0.64600
ADC Value: 648, Voltage: 0.64800
ADC Value: 652, Voltage: 0.65200
ADC Value: 654, Voltage: 0.65400
ADC Value: 658, Voltage: 0.65800
ADC Value: 660, Voltage: 0.66000
ADC Value: 662, Voltage: 0.66200
ADC Value: 664, Voltage: 0.66400
ADC Value: 666, Voltage: 0.66600
ADC Value: 668, Voltage: 0.66800
ADC Value: 672, Voltage: 0.67200
ADC Value: 674, Voltage: 0.67400
ADC Value: 676, Voltage: 0.67600
ADC Value: 680, Voltage: 0.68000
ADC Value: 682, Voltage: 0.68200
ADC Value: 684, Voltage: 0.68400
ADC Value: 690, Voltage: 0.69000
ADC Value: 692, Voltage: 0.69200
ADC Value: 696, Voltage: 0.69600
ADC Value: 700, Voltage: 0.70000
ADC Value: 702, Voltage: 0.70200
ADC Value: 704, Voltage: 0.70400
ADC Value: 706, Voltage: 0.70600
ADC Value: 708, Voltage: 0.70800
ADC Value: 712, Voltage: 0.71200
ADC Value: 716, Voltage: 0.71600
ADC Value: 720, Voltage: 0.72000
ADC Value: 724, Voltage: 0.72400
ADC Value: 726, Voltage: 0.72600
ADC Value: 728, Voltage: 0.72800
ADC Value: 732, Voltage: 0.73200
ADC Value: 736, Voltage: 0.73600
ADC Value: 738, Voltage: 0.73800
ADC Value: 740, Voltage: 0.74000
ADC Value: 742, Voltage: 0.74200
ADC Value: 744, Voltage: 0.74400
ADC Value: 748, Voltage: 0.74800
ADC Value: 750, Voltage: 0.75000
ADC Value: 754, Voltage: 0.75400
ADC Value: 758, Voltage: 0.75800
ADC Value: 760, Voltage: 0.76000
ADC Value: 762, Voltage: 0.76200
ADC Value: 766, Voltage: 0.76600
ADC Value: 768, Voltage: 0.76800
ADC Value: 770, Voltage: 0.77000
ADC Value: 772, Voltage: 0.77200
ADC Value: 774, Voltage: 0.77400
ADC Value: 778, Voltage: 0.77800
ADC Value: 780, Voltage: 0.78000
ADC Value: 782, Voltage: 0.78200
ADC Value: 786, Voltage: 0.78600
ADC Value: 790, Voltage: 0.79000
ADC Value: 792, Voltage: 0.79200
ADC Value: 794, Voltage: 0.79400
ADC Value: 796, Voltage: 0.79600
ADC Value: 798, Voltage: 0.79800
ADC Value: 800, Voltage: 0.80000
ADC Value: 802, Voltage: 0.80200
ADC Value: 804, Voltage: 0.80400
ADC Value: 806, Voltage: 0.80600
ADC Value: 808, Voltage: 0.80800
ADC Value: 806, Voltage: 0.80600
ADC Value: 804, Voltage: 0.80400
ADC Value: 802, Voltage: 0.80200
ADC Value: 800, Voltage: 0.80000
ADC Value: 798, Voltage: 0.79800
ADC Value: 796, Voltage: 0.79600
ADC Value: 794, Voltage: 0.79400
ADC Value: 796, Voltage: 0.79600
ADC Value: 798, Voltage: 0.79800
ADC Value: 802, Voltage: 0.80200
ADC Value: 806, Voltage: 0.80600
ADC Value: 808, Voltage: 0.80800
ADC Value: 810, Voltage: 0.81000
ADC Value: 812, Voltage: 0.81200
ADC Value: 814, Voltage: 0.81400
ADC Value: 818, Voltage: 0.81800
ADC Value: 822, Voltage: 0.82200
ADC Value: 830, Voltage: 0.83000
ADC Value: 840, Voltage: 0.84000
ADC Value: 844, Voltage: 0.84400
ADC Value: 850, Voltage: 0.85000
ADC Value: 856, Voltage: 0.85600
ADC Value: 862, Voltage: 0.86200
ADC Value: 866, Voltage: 0.86600
ADC Value: 874, Voltage: 0.87400
ADC Value: 878, Voltage: 0.87800
ADC Value: 884, Voltage: 0.88400
ADC Value: 892, Voltage: 0.89200
ADC Value: 898, Voltage: 0.89800
ADC Value: 906, Voltage: 0.90600
ADC Value: 912, Voltage: 0.91200
ADC Value: 920, Voltage: 0.92000
ADC Value: 926, Voltage: 0.92600
ADC Value: 934, Voltage: 0.93400
ADC Value: 936, Voltage: 0.93600
ADC Value: 940, Voltage: 0.94000
ADC Value: 946, Voltage: 0.94600
ADC Value: 952, Voltage: 0.95200
ADC Value: 956, Voltage: 0.95600
ADC Value: 966, Voltage: 0.96600
ADC Value: 970, Voltage: 0.97000
ADC Value: 976, Voltage: 0.97600
ADC Value: 980, Voltage: 0.98000
ADC Value: 988, Voltage: 0.98800
ADC Value: 994, Voltage: 0.99400
ADC Value: 998, Voltage: 0.99800
ADC Value: 1004, Voltage: 1.00400
ADC Value: 1010, Voltage: 1.01000
ADC Value: 1014, Voltage: 1.01400
ADC Value: 1020, Voltage: 1.02000
ADC Value: 1028, Voltage: 1.02800
ADC Value: 1032, Voltage: 1.03200
ADC Value: 1038, Voltage: 1.03800
ADC Value: 1046, Voltage: 1.04600
ADC Value: 1050, Voltage: 1.05000
ADC Value: 1058, Voltage: 1.05800
ADC Value: 1064, Voltage: 1.06400
ADC Value: 1070, Voltage: 1.07000
ADC Value: 1072, Voltage: 1.07200
ADC Value: 1076, Voltage: 1.07600
ADC Value: 1086, Voltage: 1.08600
ADC Value: 1090, Voltage: 1.09000
ADC Value: 1096, Voltage: 1.09600
ADC Value: 1098, Voltage: 1.09800
ADC Value: 1104, Voltage: 1.10400
ADC Value: 1112, Voltage: 1.11200
ADC Value: 1114, Voltage: 1.11400
ADC Value: 1122, Voltage: 1.12200
ADC Value: 1128, Voltage: 1.12800
ADC Value: 1130, Voltage: 1.13000
ADC Value: 1138, Voltage: 1.13800
ADC Value: 1142, Voltage: 1.14200
ADC Value: 1146, Voltage: 1.14600
ADC Value: 1150, Voltage: 1.15000
ADC Value: 1156, Voltage: 1.15600
ADC Value: 1162, Voltage: 1.16200
ADC Value: 1166, Voltage: 1.16600
ADC Value: 1170, Voltage: 1.17000
ADC Value: 1174, Voltage: 1.17400
ADC Value: 1178, Voltage: 1.17800
ADC Value: 1182, Voltage: 1.18200
ADC Value: 1188, Voltage: 1.18800
ADC Value: 1194, Voltage: 1.19400
ADC Value: 1196, Voltage: 1.19600
ADC Value: 1200, Voltage: 1.20000
ADC Value: 1204, Voltage: 1.20400
ADC Value: 1210, Voltage: 1.21000
ADC Value: 1216, Voltage: 1.21600
ADC Value: 1220, Voltage: 1.22000
ADC Value: 1224, Voltage: 1.22400
ADC Value: 1234, Voltage: 1.23400
ADC Value: 1240, Voltage: 1.24000
ADC Value: 1244, Voltage: 1.24400
ADC Value: 1248, Voltage: 1.24800
ADC Value: 1252, Voltage: 1.25200
ADC Value: 1260, Voltage: 1.26000
ADC Value: 1268, Voltage: 1.26800
ADC Value: 1272, Voltage: 1.27200
ADC Value: 1276, Voltage: 1.27600
ADC Value: 1282, Voltage: 1.28200
ADC Value: 1286, Voltage: 1.28600
ADC Value: 1290, Voltage: 1.29000
ADC Value: 1298, Voltage: 1.29800
ADC Value: 1300, Voltage: 1.30000
ADC Value: 1304, Voltage: 1.30400
ADC Value: 1310, Voltage: 1.31000
ADC Value: 1312, Voltage: 1.31200
ADC Value: 1314, Voltage: 1.31400
ADC Value: 1322, Voltage: 1.32200
ADC Value: 1324, Voltage: 1.32400
ADC Value: 1326, Voltage: 1.32600
ADC Value: 1336, Voltage: 1.33600
ADC Value: 1342, Voltage: 1.34200
ADC Value: 1344, Voltage: 1.34400
ADC Value: 1350, Voltage: 1.35000
ADC Value: 1356, Voltage: 1.35600
ADC Value: 1362, Voltage: 1.36200
ADC Value: 1366, Voltage: 1.36600
ADC Value: 1370, Voltage: 1.37000
ADC Value: 1378, Voltage: 1.37800
ADC Value: 1386, Voltage: 1.38600
ADC Value: 1390, Voltage: 1.39000
ADC Value: 1398, Voltage: 1.39800
ADC Value: 1404, Voltage: 1.40400
ADC Value: 1410, Voltage: 1.41000
ADC Value: 1414, Voltage: 1.41400
ADC Value: 1422, Voltage: 1.42200
ADC Value: 1428, Voltage: 1.42800
ADC Value: 1436, Voltage: 1.43600
ADC Value: 1440, Voltage: 1.44000
ADC Value: 1446, Voltage: 1.44600
ADC Value: 1450, Voltage: 1.45000
ADC Value: 1458, Voltage: 1.45800
ADC Value: 1462, Voltage: 1.46200
ADC Value: 1468, Voltage: 1.46800
ADC Value: 1474, Voltage: 1.47400
ADC Value: 1476, Voltage: 1.47600
ADC Value: 1486, Voltage: 1.48600
ADC Value: 1494, Voltage: 1.49400
ADC Value: 1502, Voltage: 1.50200
ADC Value: 1510, Voltage: 1.51000
ADC Value: 1512, Voltage: 1.51200
ADC Value: 1518, Voltage: 1.51800
ADC Value: 1530, Voltage: 1.53000
ADC Value: 1536, Voltage: 1.53600
ADC Value: 1542, Voltage: 1.54200
ADC Value: 1550, Voltage: 1.55000
ADC Value: 1558, Voltage: 1.55800
ADC Value: 1568, Voltage: 1.56800
ADC Value: 1576, Voltage: 1.57600
ADC Value: 1580, Voltage: 1.58000
ADC Value: 1584, Voltage: 1.58400
ADC Value: 1588, Voltage: 1.58800
ADC Value: 1598, Voltage: 1.59800
ADC Value: 1606, Voltage: 1.60600
ADC Value: 1610, Voltage: 1.61000
ADC Value: 1622, Voltage: 1.62200
ADC Value: 1628, Voltage: 1.62800
ADC Value: 1630, Voltage: 1.63000
ADC Value: 1638, Voltage: 1.63800
ADC Value: 1644, Voltage: 1.64400
ADC Value: 1654, Voltage: 1.65400
ADC Value: 1662, Voltage: 1.66200
ADC Value: 1668, Voltage: 1.66800
ADC Value: 1678, Voltage: 1.67800
ADC Value: 1686, Voltage: 1.68600
ADC Value: 1692, Voltage: 1.69200
ADC Value: 1702, Voltage: 1.70200
ADC Value: 1708, Voltage: 1.70800
ADC Value: 1714, Voltage: 1.71400
ADC Value: 1728, Voltage: 1.72800
ADC Value: 1744, Voltage: 1.74400
ADC Value: 1748, Voltage: 1.74800
ADC Value: 1758, Voltage: 1.75800
ADC Value: 1770, Voltage: 1.77000
ADC Value: 1776, Voltage: 1.77600
ADC Value: 1782, Voltage: 1.78200
ADC Value: 1786, Voltage: 1.78600
ADC Value: 1798, Voltage: 1.79800
ADC Value: 1810, Voltage: 1.81000
ADC Value: 1818, Voltage: 1.81800
ADC Value: 1824, Voltage: 1.82400
ADC Value: 1832, Voltage: 1.83200
ADC Value: 1844, Voltage: 1.84400
ADC Value: 1850, Voltage: 1.85000
ADC Value: 1860, Voltage: 1.86000
ADC Value: 1866, Voltage: 1.86600
ADC Value: 1878, Voltage: 1.87800
ADC Value: 1888, Voltage: 1.88800
ADC Value: 1900, Voltage: 1.90000
ADC Value: 1904, Voltage: 1.90400
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
ADC Value: 2034, Voltage: 2.03400
ADC Value: 2042, Voltage: 2.04200
ADC Value: 2050, Voltage: 2.05000
ADC Value: 2062, Voltage: 2.06200
ADC Value: 2076, Voltage: 2.07600
ADC Value: 2082, Voltage: 2.08200
ADC Value: 2096, Voltage: 2.09600
ADC Value: 2108, Voltage: 2.10800
ADC Value: 2116, Voltage: 2.11600
ADC Value: 2124, Voltage: 2.12400
ADC Value: 2136, Voltage: 2.13600
ADC Value: 2144, Voltage: 2.14400
ADC Value: 2158, Voltage: 2.15800
ADC Value: 2170, Voltage: 2.17000
ADC Value: 2172, Voltage: 2.17200
ADC Value: 2188, Voltage: 2.18800
ADC Value: 2198, Voltage: 2.19800
ADC Value: 2212, Voltage: 2.21200
ADC Value: 2222, Voltage: 2.22200
ADC Value: 2236, Voltage: 2.23600
ADC Value: 2246, Voltage: 2.24600
ADC Value: 2258, Voltage: 2.25800
ADC Value: 2274, Voltage: 2.27400
ADC Value: 2284, Voltage: 2.28400
ADC Value: 2296, Voltage: 2.29600
ADC Value: 2310, Voltage: 2.31000
ADC Value: 2326, Voltage: 2.32600
ADC Value: 2334, Voltage: 2.33400
ADC Value: 2346, Voltage: 2.34600
ADC Value: 2360, Voltage: 2.36000
ADC Value: 2366, Voltage: 2.36600
ADC Value: 2386, Voltage: 2.38600
ADC Value: 2394, Voltage: 2.39400
ADC Value: 2408, Voltage: 2.40800
ADC Value: 2418, Voltage: 2.41800
ADC Value: 2432, Voltage: 2.43200
ADC Value: 2448, Voltage: 2.44800
ADC Value: 2456, Voltage: 2.45600
ADC Value: 2464, Voltage: 2.46400
ADC Value: 2476, Voltage: 2.47600
ADC Value: 2488, Voltage: 2.48800
ADC Value: 2504, Voltage: 2.50400
ADC Value: 2512, Voltage: 2.51200
ADC Value: 2526, Voltage: 2.52600
ADC Value: 2540, Voltage: 2.54000
ADC Value: 2552, Voltage: 2.55200
ADC Value: 2564, Voltage: 2.56400
ADC Value: 2570, Voltage: 2.57000
ADC Value: 2578, Voltage: 2.57800
ADC Value: 2588, Voltage: 2.58800
ADC Value: 2606, Voltage: 2.60600
ADC Value: 2614, Voltage: 2.61400
ADC Value: 2618, Voltage: 2.61800
ADC Value: 2624, Voltage: 2.62400
ADC Value: 2630, Voltage: 2.63000
ADC Value: 2638, Voltage: 2.63800
ADC Value: 2650, Voltage: 2.65000
ADC Value: 2656, Voltage: 2.65600
ADC Value: 2658, Voltage: 2.65800
ADC Value: 2666, Voltage: 2.66600
ADC Value: 2668, Voltage: 2.66800
ADC Value: 2678, Voltage: 2.67800
ADC Value: 2688, Voltage: 2.68800
ADC Value: 2692, Voltage: 2.69200
ADC Value: 2686, Voltage: 2.68600
ADC Value: 2698, Voltage: 2.69800
ADC Value: 2696, Voltage: 2.69600
ADC Value: 2712, Voltage: 2.71200
ADC Value: 2722, Voltage: 2.72200
ADC Value: 2734, Voltage: 2.73400
ADC Value: 2740, Voltage: 2.74000
ADC Value: 2744, Voltage: 2.74400
ADC Value: 2760, Voltage: 2.76000
ADC Value: 2764, Voltage: 2.76400
ADC Value: 2770, Voltage: 2.77000
ADC Value: 2768, Voltage: 2.76800
ADC Value: 2770, Voltage: 2.77000
ADC Value: 2768, Voltage: 2.76800
ADC Value: 2770, Voltage: 2.77000
ADC Value: 2768, Voltage: 2.76800
ADC Value: 2770, Voltage: 2.77000
ADC Value: 2772, Voltage: 2.77200
ADC Value: 2770, Voltage: 2.77000
ADC Value: 2772, Voltage: 2.77200
ADC Value: 2776, Voltage: 2.77600
ADC Value: 2784, Voltage: 2.78400
ADC Value: 2792, Voltage: 2.79200
ADC Value: 2798, Voltage: 2.79800
ADC Value: 2802, Voltage: 2.80200
ADC Value: 2808, Voltage: 2.80800
ADC Value: 2824, Voltage: 2.82400
ADC Value: 2834, Voltage: 2.83400
ADC Value: 2840, Voltage: 2.84000
ADC Value: 2858, Voltage: 2.85800
ADC Value: 2870, Voltage: 2.87000
ADC Value: 2874, Voltage: 2.87400
ADC Value: 2890, Voltage: 2.89000
ADC Value: 2910, Voltage: 2.91000
ADC Value: 2922, Voltage: 2.92200
ADC Value: 2934, Voltage: 2.93400
ADC Value: 2950, Voltage: 2.95000
ADC Value: 2964, Voltage: 2.96400
ADC Value: 2976, Voltage: 2.97600
ADC Value: 2982, Voltage: 2.98200
ADC Value: 2996, Voltage: 2.99600
ADC Value: 3010, Voltage: 3.01000
ADC Value: 3020, Voltage: 3.02000
ADC Value: 3028, Voltage: 3.02800
ADC Value: 3038, Voltage: 3.03800
ADC Value: 3044, Voltage: 3.04400
ADC Value: 3054, Voltage: 3.05400
ADC Value: 3060, Voltage: 3.06000
ADC Value: 3070, Voltage: 3.07000
ADC Value: 3074, Voltage: 3.07400
ADC Value: 3086, Voltage: 3.08600
ADC Value: 3094, Voltage: 3.09400
ADC Value: 3102, Voltage: 3.10200
ADC Value: 3104, Voltage: 3.10400
ADC Value: 3118, Voltage: 3.11800
ADC Value: 3124, Voltage: 3.12400
ADC Value: 3130, Voltage: 3.13000
ADC Value: 3138, Voltage: 3.13800
ADC Value: 3146, Voltage: 3.14600
ADC Value: 3154, Voltage: 3.15400
ADC Value: 3166, Voltage: 3.16600
ADC Value: 3176, Voltage: 3.17600
ADC Value: 3186, Voltage: 3.18600
ADC Value: 3194, Voltage: 3.19400
ADC Value: 3198, Voltage: 3.19800
ADC Value: 3208, Voltage: 3.20800
ADC Value: 3218, Voltage: 3.21800
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
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3246, Voltage: 3.24600
ADC Value: 3244, Voltage: 3.24400
ADC Value: 3242, Voltage: 3.24200
ADC Value: 3240, Voltage: 3.24000
ADC Value: 3238, Voltage: 3.23800
ADC Value: 3236, Voltage: 3.23600
ADC Value: 3234, Voltage: 3.23400
ADC Value: 3238, Voltage: 3.23800
ADC Value: 3236, Voltage: 3.23600
ADC Value: 3238, Voltage: 3.23800
...
^CDone.
 $
```

### ADS1115
I<sup><small>2</small></sup>C, 4 channels of 16-bit analog input

As such, returns values between 0 and 2<sup><small>16</small></sup>, \[0..65535\]

![ADS1115](./rpi-ads1115-pot_bb.png)

---
