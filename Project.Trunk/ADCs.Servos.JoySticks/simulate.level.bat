@echo off
@setlocal
set CP=.\classes
set CP=%CP%;.\lib\java_websocket.jar
set CP=%CP%;.\lib\json.jar
java -cp %CP% raspisamples.adc.levelreader.samples.LevelSimulator
@endlocal
