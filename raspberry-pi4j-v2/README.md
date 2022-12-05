# PI4J V2
This is another tentative to find a good and stable way to implement sensors and actuators drivers.

PI4j-v1 is now deprecated, because of the deprecation of WiringPi.  
Depending on third-party software is risky, as when it goes deprecated, you have to re-write whatever depends on it...

Possibilities - among many others - could be (the list is not closed!):
- [PI4J-v2](https://github.com/Pi4J/pi4j-v2/)
- [diozero](https://github.com/mattjlewis/diozero)
- [JOB](https://github.com/OlivierLD/JOB)
- [Using a bridge between Java and Python, using TCP ?](../java-python/README.md)
- ...

# _This is a Work in Progress_ 🚧

## Resources
- <https://pi4j.com/>
- <https://github.com/Pi4J/pi4j-v2/>
- <https://pi4j.com/examples/>
- <https://github.com/jveverka/rpi-projects>

## Now we're talking...
`PI4J-v2` requires `pigpio` to be installed, explicitly.
```
sudo apt-get update
sudo apt-get install pigpio python-pigpio python3-pigpio
```

