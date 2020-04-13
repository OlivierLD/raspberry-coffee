@echo off
@setlocal
::
set CP=.
set CP=%CP%;.\build\libs\compute.jar
set CP=%CP%;.\build\classes
::
set JAVA_OPTS=
::
java -cp %CP% %JAVA_OPTS% client.ComputePi %*
::
@endlocal
