@echo off
@setlocal
::
:: WIP
:: Create a Windows distributable environment
::
:: Need 7z to be installed
set PATH=%PATH%;"C:\Program Files\7-Zip"
::
echo Make sure you've run a build, ..\gradlew clean shadowJar [-x :NMEA-multiplexer:compileScala]
::
:: Check is a distrib folder in %HOMEPATH%
if not exist %HOMEPATH%\distrib (
  mkdir %HOMEPATH%\distrib
)
:: Drop distrib content
rmdir /s /q %HOMEPATH%\distrib\rxtx
rmdir /s /q %HOMEPATH%\distrib\mux
::
:: pause
mkdir %HOMEPATH%\distrib\rxtx
mkdir %HOMEPATH%\distrib\mux
mkdir %HOMEPATH%\distrib\mux\build
mkdir %HOMEPATH%\distrib\mux\build\libs
xcopy /e /a %HOMEPATH%\rxtx-2.2pre2-bins %HOMEPATH%\distrib\rxtx
xcopy /e /a .\build\libs %HOMEPATH%\distrib\mux\build\libs
copy *.bat %HOMEPATH%\distrib\mux\
copy *.yaml %HOMEPATH%\distrib\mux\
echo You will need to change the rxtx location in windows.mux.bat
:: Now archive the stuff
set ARCHIVE_NAME=.\distrib.zip
7z a -tzip %ARCHIVE_NAME% %HOMEPATH%\distrib\*
echo %ARCHIVE_NAME% is ready.
@endlocal
