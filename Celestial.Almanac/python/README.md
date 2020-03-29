# Celestial Computer in Python.

See sample usage in `calculation_sample.py`.
```
$ python calculation_sample.py 
----------------------------------------------
Calculations done for 2020-Mar-28 16:50:20 UTC
----------------------------------------------
Sun: GHA 071° 21' 49", RA 00h 31m 07.2s, DEC N  03° 21' 30", sd 961.2", hp 8.8"
Venus: GHA 027° 59' 22", RA 03h 24m 37.0s, DEC N  22° 06' 49", sd 12.4", hp 12.9"
Mars: GHA 138° 18' 09", RA 20h 03m 21.8s, DEC S  21° 20' 27", sd 3.1", hp 5.9"
Jupiter: GHA 143° 16' 28", RA 19h 43m 28.6s, DEC S  21° 22' 02", sd 18.3", hp 1.6"
Saturn: GHA 136° 28' 07", RA 20h 10m 42.0s, DEC S  20° 06' 17", sd 8.0.0", hp 0.8"
Moon: GHA 025° 09' 40", RA 03h 35m 55.8s, DEC N  16° 11' 24", sd 894.4", hp 3282.3"
	Moon phase: 47.100000,  +cre
Polaris: GHA 035° 17' 02", RA 02h 55m 26.3s, DEC N  89° 21' 02"
Ecliptic: obliquity 23° 26' 11.973", true 23° 26' 12.013"
Equation of time - 4m 52.8s 
Lunar Distance: 047° 10' 03"
Day of Week: SAT
$
```

How many lines of python?
```
$ (find . -name '*.py' -print0 | xargs -0 cat) |  wc -l
```
