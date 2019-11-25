@echo off
@setlocal
::
:: Launch 2 scripts in parallel, with one output file.
::
del out.txt
start cmd /c "script.1.bat"
start cmd /c "script.2.bat"
@endlocal
