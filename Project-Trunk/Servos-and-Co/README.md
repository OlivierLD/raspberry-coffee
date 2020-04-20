# Servos, Feedback potentiometers
We want to use a Continuous Servo to orient some panel (like a solar panel...),
and a feedback pot to know its actual position.

> _Note_: For this kind of orientation, we could maybe use Standard Servos.
> The thing is that they are quite limited in term of power. 
>
> We use Continuous Servos to be able to use gear ratios and other worm gear, 
> to de-multiply the load assigned to the servo.
>
> The same could apply to DC and Stepper Motors.   

We can also use some 3D-printed hardware, described in some other projects and repos.
- [Linear potentiometers](https://github.com/OlivierLD/raspberry-coffee/blob/master/ADC-benchmark/LINEAR_POTS.md)
- [Feedback POC](https://github.com/OlivierLD/3DPrinting/tree/master/OpenSCAD/FeedbackPOC)

## Wiring

![Wiring](./RPi-MCP3008-2-Pots_bb.png)

#### ADC, MCP3008
We have 2 `B10K` linear potentiometers, one for the knob (channel 0) and one for the feedback (channel 1).

```
Wiring of the MCP3008-SPI (without power supply):
 +---------++-----------------------------------------------+
 | MCP3008 || Raspberry Pi                                  |
 +---------++------+------------+------+---------+----------+
 |         || Pin# | Name       | Role | GPIO    | wiringPI |
 |         ||      |            |      | /BCM    | /PI4J    |
 +---------++------+------------+------+---------+----------+
 | CLK (13)|| #23  | SPI0_CLK   | CLK  | GPIO_11 | 14       |
 | Din (11)|| #19  | SPI0_MOSI  | MOSI | GPIO_10 | 12       |
 | Dout(12)|| #21  | SPI0_MISO  | MISO | GPIO_09 | 13       |
 | CS  (10)|| #24  | SPI0_CS0_N | CS   | GPIO_08 | 10       |
 +---------++------+------------+-----+----------+----------+

       +--------+ 
* CH0 -+  1  16 +- Vdd 
* CH1 -+  2  15 +- Vref 
  CH2 -+  3  14 +- aGnd 
  CH3 -+  4  13 +- CLK 
  CH4 -+  5  12 +- Dout 
  CH5 -+  6  11 +- Din 
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
       |     |     | 3v3          | #17 || #18 |       GPIO_5 | 05  | 24  |       
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
```

#### Servo driver, Adafruit's `PCA9685`
- Board 3.3V to sensor VCC
- Board GND to sensor GND
- Board SCL to sensor SCL
- Board SDA to sensor SDA

A 5v power supply is required to feed the servos.

the Female DC power adapter works fine
I was also able to power it from the Raspberry Header (pins #2 and #39) for 2 servos without problem.


--- 
