# Read raw Data from a GPS
## Through the Serial interface, using PI4J.
> Note: Not only for GPS

See script named `run`
```
pi@raspberrypi3:~/raspberry-pi4j-samples/GPS.read $ ./run
Read serial port, raw data
Serial Communication.
 ... connect using settings: 4800, N, 8, 1.
 ... data received on serial port should be displayed below.
Opening port [/dev/ttyUSB0]
Port is opened.
37 34 34 2E 39 33 33 35 2C 4E 2C 31 32 32 33 30     744.9335,N,12230
2E 34 31 37 38 2C 57 2C 30 30 30 2E 30 2C 30 30     .4178,W,000.0,00
30 2E 30 2C 31 39 31 30 31 36 2C 2C 2C 41 2A 37     0.0,191016,,,A*7
35 0D 0A 24 47 50 47 47 41 2C 31 31 34 39 32 38     5..$GPGGA,114928
2E 39 30 36 2C 33 37 34 34 2E 39 33 33 35 2C 4E     .906,3744.9335,N
2C 31 32 32 33 30 2E 34 31 37 38 2C 57 2C 31 2C     ,12230.4178,W,1,
30 37 2C 31 2E 34 2C 2D 32 38 2E 31 2C 4D 2C 2C     07,1.4,-28.1,M,,
2C 2C 30 30 30 30 2A 30 31 0D 0A 24 47 50 47 53     ,,0000*01..$GPGS
41 2C 41 2C 33 2C 33 30 2C 30 36 2C 32 38 2C 31     A,A,3,30,06,28,1
37 2C 31 39 2C 30 31 2C 31 33 2C 2C 2C 2C 2C 2C     7,19,01,13,,,,,,
32 2E 31 2C 31 2E 34 2C 31 2E 37 2A 33 30 0D 0A     2.1,1.4,1.7*30..
24 47 50 52 4D 43 2C 31 31 34 39 32 38 2E 39 30     $GPRMC,114928.90
36 2C 41 2C 33 37 34 34 2E 39 33 33 35 2C 4E 2C     6,A,3744.9335,N,
31 32 32 33 30 2E 34 31 37 38 2C 57 2C 30 30 30     12230.4178,W,000
2E 30 2C 30 30 30 2E 30 2C 31 39 31 30 31 36 2C     .0,000.0,191016,
2C 2C 41 2A 37 41                                   ,,A*7A
0D 0A                                               ..
24 47 50 47 47 41 2C 31 31 34 39 32 39 2E 39 30     $GPGGA,114929.90
36                                                  6
2C 33 37 34 34 2E 39 33 33 35 2C 4E 2C 31 32 32     ,3744.9335,N,122
33 30 2E                                            30.
34 31 37 38 2C 57 2C 31 2C 30 37 2C 31 2E 34 2C     4178,W,1,07,1.4,
2D                                                  -
32 38 2E 31 2C 4D 2C 2C 2C 2C 30 30 30 30 2A 30     28.1,M,,,,0000*0
30                                                  0
0D 0A 24 47 50 47 53 41 2C 41 2C 33 2C 33 30 2C     ..$GPGSA,A,3,30,

30 36 2C 32 38 2C 31 37 2C 31 39 2C 30 31 2C 31     06,28,17,19,01,1
33                                                  3
2C 2C 2C 2C 2C 2C 32 2E 31 2C 31 2E 34 2C 31 2E     ,,,,,,2.1,1.4,1.
37                                                  7
... etc
```
