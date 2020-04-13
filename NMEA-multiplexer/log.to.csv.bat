@echo off
@setlocal
::
set argCount=0
for %%x in (%*) do (
   set /A argCount+=1
)
if %argCount% neq 2 (
  echo Usage is %0 [log.file.name] [csv.file.name]
  echo example: %0 sample.data/2010-11.03.Taiohae.nmea today.csv
  exit /b 1
)
::
set BREAK_AT=RMC
:: set DATA=RMC,HDG,VHW,MWV,MTW
set DATA=RMC,HDM,VHW,DBT,VWR,MTW

::
set CP=.\build\libs\NMEA-multiplexer-1.0-all.jar
set JAVA_OPTIONS=
#
:: set JAVA_OPTIONS=%JAVA_OPTIONS% -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80
:: set JAVA_OPTIONS=%JAVA_OPTIONS% -Dverbose=true
java %JAVA_OPTIONS% -cp %CP% util.NMEAtoCSV --in:%1 --out:%2 --data:%DATA% --break-at:%BREAK_AT%
::
@endlocal
