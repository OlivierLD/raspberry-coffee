Inspired by the doc at <https://learn.adafruit.com/lora-and-lorawan-radio-for-raspberry-pi?view=all>

> _Note_: The resistor in the doc is a 1k&Omega; &plusmn;5%  
> _Note_: Do it on a recent version of RasPi OS. That comes with at least Python 3.6 (do a `python3 -V`) to know.
 
Required installations:
- Circuit Python at <https://learn.adafruit.com/circuitpython-on-raspberrypi-linux/installing-circuitpython-on-raspberry-pi>
    - which in turn requires Python 6 (or more recent)
    ```
    wget https://www.python.org/ftp/python/3.6.0/Python-3.6.0.tgz
    tar xzvf Python-3.6.0.tgz
    cd Python-3.6.0/
    ./configure
    make -j4
    sudo make install
    ```
    
```
$ sudo pip3 install adafruit-blinka
$ sudo pip3 install adafruit-circuitpython-ssd1306
$ sudo pip3 install adafruit-circuitpython-framebuf
$ sudo pip3 install adafruit-circuitpython-rfm9x
$ wget https://github.com/adafruit/Adafruit_CircuitPython_framebuf/raw/master/examples/font5x8.bin
```

# TODO
- A REST Python server, accessible from Java (and everything) to send/receive from any language.

