@setlocal
@echo off
set CP=.\classes
set CP=%CP%;.\lib\jansi-1.9.jar
java -cp %CP% adc.utils.EscapeSeq
@endlocal