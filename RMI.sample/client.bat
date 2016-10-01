@echo off
@setlocal
::
set CP=.
set CP=%CP%;.\build\libs\compute.jar
set CP=%CP%;.\build\classes
::
java -cp %CP% client.ComputePi %*
::
@endlocal
