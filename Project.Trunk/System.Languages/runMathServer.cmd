@setlocal
@echo on
set CP=.\build\libs\System.Languages-1.0-all.jar
::
echo ---------------------------------
echo Usage is %0 [-px^|--proxy] [-p:^|--port:1234]
echo       -px or --proxy means with a proxy
echo ---------------------------------
::
set USE_PROXY=false
set HTTP_PORT=1234
::
:looptop
set PRM=%1
echo PRM="%PRM%"
if "%PRM%" == "" (
  echo No more parameter
  goto next
) else (
  if "%PRM%" == "-px" (
    set USE_PROXY=true
  ) else if "%PRM%" == "--proxy" (
    set USE_PROXY=true
  ) else if "%PRM:~0,3%" == "-p:" (
    set HTTP_PORT=%PRM:~3%
  ) else if "%PRM:~0,7%" == "--port:" (
    set HTTP_PORT=%PRM:~7%
  )
  shift /1
  goto looptop
)
:next
echo Let us go
::
set HTTP_VERBOSE=false
set MATH_REST_VERBOSE=true
set SYSTEM_VERBOSE=true
::
set JAVA_OPTS=
set JAVA_OPTS=%JAVA_OPTS% -Dhttp.verbose=%HTTP_VERBOSE%
set JAVA_OPTS=%JAVA_OPTS% -Dmath.rest.verbose=%MATH_REST_VERBOSE%
set JAVA_OPTS=%JAVA_OPTS% -Dsystem.verbose=%SYSTEM_VERBOSE%
::
if "%USE_PROXY%" == "true" (
  set JAVA_OPTS=%JAVA_OPTS% -Dhttp.proxyHost=www-proxy-hqdc.us.oracle.com -Dhttp.proxyPort=80
)
java -cp %CP% %JAVA_OPTS% matrix.server.MathServer
::
@endlocal
