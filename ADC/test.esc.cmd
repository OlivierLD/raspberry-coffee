@setlocal
@echo off
set CP=.\build\libs\ADC-1.0-all.jar
java -cp %CP% adc.utils.EscapeSeq
@endlocal
