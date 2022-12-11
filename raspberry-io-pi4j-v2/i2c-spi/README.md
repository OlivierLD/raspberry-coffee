# Porting the code from PI4J v1 to PI4 J v2

PI4j-v1 is now deprecated, because of the deprecation of WiringPi.  
Depending on third-party software is risky, as when it goes deprecated, you have to re-write whatever depends on it...

Possibilities - among many others - could be (the list is not closed!):
- [PI4J-v2](https://github.com/Pi4J/pi4j-v2/)
- [diozero](https://github.com/mattjlewis/diozero)
- [JOB](https://github.com/OlivierLD/JOB)
- [Using a bridge between Java and Python, using TCP ?](../../java-python/README.md)
- ...

# _This is a Work in Progress_ 🚧

## Resources
- <https://pi4j.com/>
- <https://github.com/Pi4J/pi4j-v2/>
- <https://pi4j.com/examples/>
- <https://github.com/jveverka/rpi-projects> <- very interesting

## Now we're talking...
`PI4J-v2` requires [pigpio](http://abyz.me.uk/rpi/pigpio/index.html) to be installed, explicitly.
> _**QUESTION**_: Really ?
> Isn't the depemdies section sufficient, with its `implementation "com.pi4j:pi4j-library-pigpio:$pi4j_version"` ?
```
sudo apt-get update
sudo apt-get install pigpio python-pigpio python3-pigpio
```
Still, problems can appear... <https://forums.raspberrypi.com/viewtopic.php?t=256475>

Problems with library `linuxfs` when using `shadowJar`... There is a way to fix this.
Look into the `build.gradle`...

