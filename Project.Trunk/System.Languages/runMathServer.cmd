@setLocal
:: EnableDelayedExpansion
@echo off
set CP=.\build\libs\System.Languages-1.0-all.jar
::
:: No column (:) in the arguments, they make a mess, confused with substrings
echo ---------------------------------
echo Usage is %0 [-px^|--proxy] [-p^|--port 1234]
echo       -px or --proxy means with a proxy
echo       -p 1243 or --port 1234 overrides the HTTP port (default is 1234)
echo ---------------------------------
::
set USE_PROXY=false
set HTTP_PORT=1234
::
:loopTop
set PRM=%1
:: echo PRM="%PRM%"
if "%PRM%" == "" (
::echo No more parameter
  goto next
) else (
  if [%PRM%] == [-px] (
    set USE_PROXY=true
  ) else if [%PRM%] == [--proxy] (
    set USE_PROXY=true
  ) else if [%PRM%] == [-p] (
    set HTTP_PORT=%2
  ) else if [%PRM%] == [--port] (
    set HTTP_PORT=%2
  )
  shift /1
  goto loopTop
)
:next
echo Let us go
echo USE_PROXY=%USE_PROXY%, HTTP_PORT=%HTTP_PORT%
::
set HTTP_VERBOSE=false
set MATH_REST_VERBOSE=true
set SYSTEM_VERBOSE=true
::
set JAVA_OPTS=-Dhttp.port=%HTTP_PORT%
set JAVA_OPTS=%JAVA_OPTS% -Dhttp.verbose=%HTTP_VERBOSE%
set JAVA_OPTS=%JAVA_OPTS% -Dmath.rest.verbose=%MATH_REST_VERBOSE%
set JAVA_OPTS=%JAVA_OPTS% -Dsystem.verbose=%SYSTEM_VERBOSE%
::
if [%USE_PROXY%] == [true] (
  set JAVA_OPTS=%JAVA_OPTS% -Dhttp.proxyHost=www-proxy-hqdc.us.oracle.com -Dhttp.proxyPort=80
)
java -cp %CP% %JAVA_OPTS% matrix.server.MathServer
::
@endLocal
