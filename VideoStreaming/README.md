# Video Streaming

Good stuff here: <https://github.com/EbenKouao/pi-camera-stream-flask>

## Setup
```
sudo apt-get install libatlas-base-dev
sudo apt-get install libjasper-dev
sudo apt-get install libqtgui4
sudo apt-get install libqt4-test
sudo apt-get install libhdf5-dev
sudo pip3 install Flask
sudo pip3 install numpy
sudo pip3 install opencv-contrib-python
sudo pip3 install imutils
sudo pip3 install opencv-python

cd
git clone https://github.com/EbenKouao/pi-camera-stream-flask.git
```

## Run
```
sudo python3 ~/pi-camera-stream-flask/main.py
```

Then reach `http://raspi-address:5000` from a browser.
