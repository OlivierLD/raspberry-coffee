# Some Python code
Mostly for tests...

- `servo.driver.py` contains a simple servo driver (pure PWM).
- `catapult.py` (Pure PWM) is a cool example, seen at <https://www.instructables.com/Program-a-Servo-Build-a-Catapult-and-Solve-for-%ce%a0-w>,
  wired like this:
  ![Catapult](./catapult_bb.png).
- The `lis3mdl` folder contains a way to read a `LIS3MDL` breakout board from Adafruit.
See the [documentation](https://learn.adafruit.com/lis3mdl-triple-axis-magnetometer?view=all)
  
## LIS3MDL
- Connect the `LIS3MDL` on the Raspberry Pi as explained in the [documentation](https://learn.adafruit.com/lis3mdl-triple-axis-magnetometer?view=all#python-computer-wiring-3052085-3).
- Try a `sudo i2cdetect -y 1`, you should see a `1c` device:
```
pi@rpi-buster:~/adafruit/lis3mdl $ sudo i2cdetect -y 1
     0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f
00:          -- -- -- -- -- -- -- -- -- -- -- -- -- 
10: -- -- -- -- -- -- -- -- -- -- -- -- 1c -- -- -- 
20: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
30: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
40: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
50: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
60: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
70: -- -- -- -- -- -- -- --                         
```
- Do a `sudo pip3 install adafruit-circuitpython-lis3mdl`
- Then you should be able to run:
```
$ python3 lis3mdl_simpletest.py 
X:    -49.71, Y:     23.56, Z:    -23.09 uT
X:    -49.97, Y:     24.16, Z:    -22.79 uT
X:    -49.66, Y:     23.69, Z:    -23.08 uT
X:    -50.69, Y:     24.01, Z:    -23.31 uT
. . .
```
- To run it as an HTTP Server
    - Edit `lis3mdl_server.py`, and change the machine name to the right IP address (127.0.0.1 works, but you would not be able to reach it from another machine)
    in `machine_name = "192.168.42.9"`.
    - Then you can run `$ python3 lis3mdl_server.py --machine-name:$(hostname -I) [ --port:8888 --verbose:true ]`
    - From any machine on the same network, you can now run
    ```
  $ curl -X GET http://192.168.42.9:8080/lis3mdl/cache | jq
    % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                   Dload  Upload   Total   Spent    Left  Speed
  100    77    0    77    0     0   2655      0 --:--:-- --:--:-- --:--:--  2655
  {
      "x": -50.014615609470916,
      "y": 23.531131248173047,
      "z": -21.674948845366853
  }
    ```
- From an HTTP Client
    - See `frompython.http.MagnetometerReader` (there is no calibration, it is just an example)
```
{"x": -48.64074831920491, "y": 36.45133002046185, "z": -23.107278573516517}
Heading: 143.152146 Pitch: 122.371485, Roll: -115.410527
```  

> Note: For magnetometers' calibration, see [here](../../../lsm303.calibration/README.md).
