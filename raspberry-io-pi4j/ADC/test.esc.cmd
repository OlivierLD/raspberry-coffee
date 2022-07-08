@setlocal
@echo off
::
:: For Windows
::
set CP=.\build\libs\ADC-1.0-all.jar
java -cp %CP% adc.utils.EscapeSeq
@endlocal
