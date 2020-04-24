@echo off
@setlocal
::
set argCount=0
for %%x in (%*) do (
   set /A argCount+=1
)
if %argCount% neq 3 (
  echo Usage is %0 [log.file.name] [title] [sub-title]
  echo example: %0 sample.data\estero.drake.2018-09-29.nmea "Kayak Drake Estero" "28-Sep-2018"
  exit /b 1
)
set CP=.\build\libs\NMEA-multiplexer-1.0-all.jar
set JAVA_OPTIONS=
::
:: set JAVA_OPTIONS=%JAVA_OPTIONS% -Drmc.date.offset=7168
java %JAVA_OPTIONS% -cp %CP% util.NMEAtoKML %1 --title:%2 --sub-title:%3
::
@endlocal
