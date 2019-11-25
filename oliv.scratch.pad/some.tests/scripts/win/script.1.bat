@echo off
@setlocal
for %%v in (A, B, C, D, E, F, G, H, I, J) do (
  echo %%v >> out.txt
  set /p dummy="[Hit enter 1] > "
)
@endlocal
