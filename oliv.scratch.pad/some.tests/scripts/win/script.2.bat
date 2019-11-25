@echo off
@setlocal
for %%v in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10) do (
  echo %%v >> out.txt
  set /p dummy="[Hit enter 2] > "
)
@endlocal
