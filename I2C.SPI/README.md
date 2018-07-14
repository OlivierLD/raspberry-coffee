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

## Servos & Motors

#### PCA9685
I<sup>2</sup>C 16-channel 12-bit PWM/Servo driver.
[from Adafruit](https://www.adafruit.com/product/815)

About servo calibration, read [this](./PWM.md).

#### Adafruit Motor HAT
I<sup>2</sup>C, DC and Stepper Motors
[from Adafruit](https://www.adafruit.com/product/2348)

## Screens

#### SSD1306 (SPI and I<sup>2</sup>C)
128x32 OLED screen, https://www.adafruit.com/product/3527, https://www.adafruit.com/product/931, and https://www.adafruit.com/product/661.

#### Nokia5110
SPI 84x48 Monochrome LCD. https://www.adafruit.com/product/338

## Miscellaneous
I<sup>2</sup>C communication between Raspberry PI and Arduino.
Package `i2c.comm`.

## Examples
### MeArm
Requires a `PCA9685`, and 4 servos.

See `i2c.samples.MeArmScriptDemo` and the script `mearm.script`, along with the file `script.01.mearm`.

See [here](https://github.com/OlivierLD/raspberry-pi4j-samples/tree/master/Processing#mearm-gui) for wiring and more details.

... More details to come.


