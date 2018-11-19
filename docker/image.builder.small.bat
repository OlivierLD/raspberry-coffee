@setlocal
@echo off
title Docker Image Builder
::
:: Build and run a docker image
::
set OK=false
set DOCKER_FILE=
set IMAGE_NAME=
set RUN_CMD=
set EXTRA_PRM=
set MESSAGE=Bye!
set MESSAGE_LABEL=
::
:: Make sure docker is available
docker --version 1> nul 2>&1
if ERRORLEVEL 1 (
  echo Docker not available on this machine, exiting.
  echo To install Docker, see https://store.docker.com/search?type=edition^&offering=community
  goto eos
)
::
:menutop
  :: Menu
  echo +-------------- D O C K E R   I M A G E   B U I L D E R ---------------+
  echo +-------------------- Build and run a docker image. -------------------+
  echo ^| 1. Ubuntu MATE, TensorFlow, Keras, Python3, Jupyter, PyCharm, VNC    ^|
  echo +----------------------------------------------------------------------+
  echo ^| Q. Oops, nothing, thanks, let me out.                                ^|
  echo +----------------------------------------------------------------------+
  set /p option=You choose =^>
  ::
  if /i [%option%] == [Q] (
    set OK=true
    echo You are done.
    echo Please come back soon!
  ) else if /i [%option%] == [1] (
    set OK=true
    set DOCKER_FILE=tensorflow.Dockerfile
    set IMAGE_NAME=oliv-tf-vnc
    set RUN_CMD=docker run -d %IMAGE_NAME%:latest
    set MESSAGE_LABEL=messLabelOne
  ) else (
    echo What? Unknown command [%option%]
  )
if [%OK%] == [false] (
  goto menutop
)
::
if [%DOCKER_FILE%] NEQ [] (
  ::
  :: Proxies, if needed
  :: export HTTP_PROXY=http://www-proxy.us.oracle.com:80
  :: export HTTPS_PROXY=http://www-proxy.us.oracle.com:80
  ::
  set EXTRA=
  if [%EXTRA_PRM%] NEQ [] (
    set EXTRA=with %EXTRA_PRM%
  )
  cls
  echo ---------------------------------------------------
  echo Generating %IMAGE_NAME% from %DOCKER_FILE% %EXTRA%
  echo Running docker build -f %DOCKER_FILE% -t %IMAGE_NAME% %EXTRA_PRM% .
  echo ---------------------------------------------------
  :: Possibly use --quiet
  docker build -f %DOCKER_FILE% -t %IMAGE_NAME% %EXTRA_PRM% .
  ::
  :: Now run
  echo Now running %RUN_CMD%...
  %RUN_CMD%
  call :%MESSAGE_LABEL%
)
goto eos
::
:messLabelOne
echo ---------------------------------------------------
echo You can log in using: docker run --interactive --tty --rm --publish 5901:5901 --publish 8888:8888 [--env USER=root] [--volume tensorflow:/root/workdir/shared] %IMAGE_NAME%:latest /bin/bash
echo                    or docker run -it --rm -p 5901:5901 -p 8888:8888 -e USER=root -v tensorflow:/root/workdir/shared %IMAGE_NAME%:latest /bin/bash
echo - then run 'vncserver :1 -geometry 1280x800 ^(or 1440x900, 1680x1050, etc^) -depth 24'
echo - then use a vncviewer on localhost:1, password is 'mate'
echo - then ^(for example^) python3 examples/mnist_cnn.py ...
echo        or python3 examples/oliv/01.py ...
echo   Several samples are available in the examples folder.
echo ---------------------------------------------------
echo To start Jupyter Notebook, type: jupyter notebook --allow-root --ip 0.0.0.0 --no-browser
echo   - Default port 8888 is exposed, you can use from the host http://localhost:8888/?token=6c95d878c045212bxxxxxx
echo ---------------------------------------------------
goto eos
::
:eos
@endlocal
