# HTTP/REST Server in Python
This folder contains the skeleton of a REST/HTTP Server written in Python.

The idea is to use it for sensor drivers written in Python.

For example, you have a breakout board [LIS3MDL](https://learn.adafruit.com/lis3mdl-triple-axis-magnetometer?view=all).
It is a triple axis magnetometer. And you have not translated the driver in Java yet.

Well, the idea here is to run it in the `http_server_skeleton.py`, feed a cache with the
data read from the board, and access those cached data from an HTTP client - possibly written in Java.

The integration between the board and the server is to be done in the 
`do_stuff` method, provided here as a dummy placeholder:
```python
def do_stuff():
    print("Let's go. Hit Ctrl+C to stop")
    while True:
        try:
            try:
                core.update_cache('position', 'data goes here')
                core.update_cache('timestamp', time.time())
            except AttributeError as ae:
                print("AttributeError : {}".format(ae))
        except KeyboardInterrupt:
            print("\n\t\tUser interrupted, exiting.")
            break
        except:
            # print("\t\tOoops! {}: {}".format(type(ex), ex))
            traceback.print_exc(file=sys.stdout)
        sleep(1.0)  # one sec between loops
    print("Bye.")
```
The cache feeding happens in the `core.update_cache` invocation. 

This `do_stuff` method is invoked in a thread, and it loops until a `Ctrl-C` is intercepted.
See the code for details.

In the case of our magnetometer, the `do_stuff` method could look like
```python
import time
import board
import busio
import adafruit_lis3mdl
 
i2c = busio.I2C(board.SCL, board.SDA)
sensor = adafruit_lis3mdl.LIS3MDL(i2c)
 
def do_stuff():
    print("Let's go. Hit Ctrl+C to stop")
    while True:
        try:
            mag_x, mag_y, mag_z = sensor.magnetic
            core.update_cache("X", mag_x)         
            core.update_cache("Y", mag_y)         
            core.update_cache("Z", mag_z)         
            # print("X:{0:10.2f}, Y:{1:10.2f}, Z:{2:10.2f} uT".format(mag_x, mag_y, mag_z))
        except KeyboardInterrupt:
            print("\n\t\tUser interrupted, exiting server.")
            break
        except:
            # print("\t\tOoops! {}: {}".format(type(ex), ex))
            traceback.print_exc(file=sys.stdout)
        time.sleep(1.0)
```

Then, from any HTTP client, you can get to the data emitted by the magnetometer,
by reaching `GET http://localhost:8080/sample/cache`:
```
$ curl -X GET http://localhost:8080/sample/cache | jq
{
  "X": -0.12,
  "Y": -0.04,
  "Z": -0.001
}
```

> The http port, the VERBs, the names of all the resources can obviously be customized according to your needs.

- See a real example [here](../../../../I2C-SPI/src/main/python/README.md).

---
