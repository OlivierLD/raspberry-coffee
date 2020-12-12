# I<sup>2</sup>C and SPI code and examples

## Sensors

#### BME280, BMP180
I<sup>2</sup>C Pressure, Temperature, Humidity

#### BMP183
SPI Pressure, Temperature

#### HMC5883L
I<sup>2</sup>C 3 axis Compass

#### HTU21DF
I<sup>2</sup>C Humidity & Temperature

#### L3GD20
I<sup>2</sup>C Triple axis Gyroscope

#### LSM303
I<sup>2</sup>C Accelerometer + Magnetometer

There is this [good paper](https://github.com/praneshkmr/node-lsm303/wiki/Understanding-the-calibration-of-the-LSM303-magnetometer-(compass)) about the LSM303 calibration.
See also [that one](./lsm303.calibration/README.md), in this module.
Run the `i2c.sensor.LSM303` with `-Dlsm303.log.for.calibration=true` (see in the script `lsm303.sh`), this will generate a CSV log file you can use in a spreadsheet (like in the document above),
to get the calibration offsets and coefficients.

Also see in the code (`i2c.sensor.LSM303.java`) how to apply those calibration parameters:
```java
  sensor.setCalibrationValue(LSM303.MAG_X_OFFSET, 9);
  sensor.setCalibrationValue(LSM303.MAG_Y_OFFSET, -16);
```
![Wiring](./img/lsm303.png)

#### MPC9808
I<sup>2</sup>C Temperature

#### MPL115A2
I<sup>2</sup>C Pressure, Temperature

#### TCS34725
I<sup>2</sup>C Light sensor, color sensor

#### TSL2561
I<sup>2</sup>C Light Sensor

#### VCNL4000
I<sup>2</sup>C Proximity sensor

#### VL53L0X
I<sup>2</sup>C Time of Flight Distance

## ADCs

#### ADS1x15
ADS1015 and ADS1115. I<sup>2</sup>C ADCs.

Document: [ADC Benchmark](https://github.com/OlivierLD/raspberry-coffee/blob/master/ADC-benchmark/README.md).

## Servos & Motors

#### PCA9685
I<sup>2</sup>C 16-channel 12-bit PWM/_**Servo driver**_.

From [Adafruit](https://www.adafruit.com/product/815)

About servo calibration, read [this](./PWM.md).

##### Wiring
- Board 3.3V to sensor VCC
- Board GND to sensor GND
- Board SCL to sensor SCL
- Board SDA to sensor SDA

A `5v` power supply is required to feed the servos. 
- the [Female DC power adapter](http://adafru.it/368) works fine
- I was also able to power it from the Raspberry Header (pins #2 and #39) for 2 servos without problem.


#### Adafruit Motor HAT
I<sup>2</sup>C, DC and Stepper Motors.

From [Adafruit](https://www.adafruit.com/product/2348)

Requires an external power supply (5-12v).

[NEMA-17 diagrams & Docs](https://www.circuitspecialists.com/nema_17_stepper_motor_42bygh4807.html). 

## Screens

#### SSD1306 (SPI and I<sup>2</sup>C)
128x32 and 128x64 OLED monochrome screens, https://www.adafruit.com/product/3527, https://www.adafruit.com/product/931, and https://www.adafruit.com/product/661,
https://www.adafruit.com/product/326, https://www.adafruit.com/product/938.

#### Nokia5110
SPI 84x48 Monochrome LCD. https://www.adafruit.com/product/338

#### Waveshare 240x240, 1.3inch IPS LCD display HAT for Raspberry Pi 🌊
Very cool 240x240 _**Color**_ screen, with 3 buttons and a 5-option joystick, https://www.waveshare.com/product/modules/oleds-lcds/raspberry-pi-lcd/1.3inch-lcd-hat.htm,
same size as the Raspberry Pi Zero.

|    |    |
|:--:|:--:|
| ![Grahical display](./img/01.ws.jpg) <br/> Graphical Display | ![BMP Image](./img/02.ws.jpg) <br/> BMP Image |
| ![Gif image](./img/03.ws.jpg) <br/> Gif image | ![Keys and Joystick](./img/04.ws.jpg) <br/> Keys and Joystick listeners |
| ![Character data](./img/05.ws.jpg) <br/> Character data | ![Analog watch](./img/06.ws.jpg) <br/> Analog watch |

Through the Java `ImageIO` package, all image formats are supported.

See the code [here](https://github.com/OlivierLD/raspberry-coffee/tree/master/I2C.SPI/src/spi/lcd/waveshare).

## Miscellaneous
I<sup>2</sup>C communication between Raspberry Pi and Arduino.
Package `i2c.comm`.

## Examples
### MeArm
Requires a `PCA9685`, and 4 servos.

- Basic test, hard-coded in `mearm.sh`
- Scripted demo, see `i2c.samples.MeArmScriptDemo` and the script `mearm.script.sh`, along with the file [`script.01.mearm`](./script.01.mearm).
- Interactive demo (drive the MeArm from your keyboard)
```
$ ./mearm.pilot.inter.sh -left:0 -right:4 -bottom:2 -claw:1
```

See [here](https://github.com/OlivierLD/raspberry-coffee/tree/master/Processing#mearm-gui) for wiring and more details.

... More details to come.


