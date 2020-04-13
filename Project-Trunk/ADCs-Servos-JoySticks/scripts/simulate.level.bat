@echo off
@setlocal
set CP=..\build\libs\ADCs-Servos-JoySticks-1.0-all.jar
java -cp %CP% joystick.adc.levelreader.samples.LevelSimulator
@endlocal
