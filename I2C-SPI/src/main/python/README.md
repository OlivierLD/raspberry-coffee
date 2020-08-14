# Some Python code
Mostly for tests...

- `servo.driver.py` contains a simple servo driver.
- The `lis3mdl` folder contains a way to read a `LIS3MDL` breakout board from Adafruit.
See the [documentation](https://learn.adafruit.com/lis3mdl-triple-axis-magnetometer?view=all)
  
## LIS3MDL
- Connect the `LIS3MDL` on the Raspberry Pi as explained in then documentation above.
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
    - Edit `lis3mdl_server.py`, and change the machine name to the right IP
    in `machine_name = "192.168.42.9"`.
    - Then you can run `$ python3 lis3mdl_server.py`
    - From any machine on the same network, you can now run
    ```
  curl -X GET http://192.168.42.9:8080/lis3mdl/cache | jq
    % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                   Dload  Upload   Total   Spent    Left  Speed
  100    77    0    77    0     0   2655      0 --:--:-- --:--:-- --:--:--  2655
  {
    "x": -50.014615609470916,
    "y": 23.531131248173047,
    "z": -21.674948845366853
  }
    ```
  