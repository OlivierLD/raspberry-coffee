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
::
:: Make sure docker is available
docker --version 1> nul 2>&1
if ERRORLEVEL 1 (
  echo Docker not available on this machine, exiting.
  echo To install Docker, see https://store.docker.com/search?type=edition^&offering=community
  REM goto eos
)
::
:menutop
  :: Menu
  echo +-------------- D O C K E R   I M A G E   B U I L D E R ---------------+
  echo +-------------------- Build and run a docker image. -------------------+
  echo ^|  1. Nav Server, Debian                                               ^|
  echo ^| 1p. Nav Server, Debian, with proxy (as an example)                   ^|
  echo ^|  2. Web Components, Debian                                           ^|
  echo ^|  3. To run on a Raspberry PI, Java, Raspberry Coffee, Web Components ^|
  echo ^|  4. Node PI, to run on a Raspberry PI                                ^|
  echo ^|  5. Node PI, to run on Debian                                        ^|
  echo ^|  6. GPS-mux, to run on a Raspberry PI (logger)                       ^|
  echo ^|  7. Golang, basics                                                   ^|
  echo ^|  8. Raspberry PI, MATE, with java, node, web comps, VNC              ^|
  echo ^|  9. Debian, Java, Scala, Spark                                       ^|
  echo ^| 10. Ubuntu MATE, TensorFlow, Keras, Python3, Jupyter, PyCharm, VNC   ^|
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
    set DOCKER_FILE=navserver.Dockerfile
    set IMAGE_NAME=oliv-nav
		set RUN_CMD="docker run -p 8080:9999 -d %IMAGE_NAME%:latest"
		::                            tcp port used in the image
		::                       tcp port as seen from outside (this machine)
		::
    echo ---------------------------------------------------
    echo Reach http://localhost:8080/web/index.html from your browser.
    echo You can also log in using: docker run -it %IMAGE_NAME%:latest /bin/bash
    echo ---------------------------------------------------
  ) else if /i [%option%] == [1p] (
    set OK=true
    set DOCKER_FILE=navserver.Dockerfile
    set IMAGE_NAME=oliv-nav
		set RUN_CMD="docker run -p 8080:9999 -d %IMAGE_NAME%:latest"
		::                              tcp port used in the image
		::                         tcp port as seen from outside (this machine)
		::
    echo ---------------------------------------------------
    echo Reach http://localhost:8080/web/index.html from your browser.
    echo You can also log in using: docker run -it %IMAGE_NAME%:latest /bin/bash
    echo ---------------------------------------------------
    ::
    set EXTRA_PRM="--build-arg http_proxy=http://www-proxy-hqdc.us.oracle.com:80"
    set EXTRA_PRM="%EXTRA_PRM% --build-arg https_proxy=http://www-proxy-hqdc.us.oracle.com:80"
    set EXTRA_PRM="%EXTRA_PRM% --build-arg no_proxy=localhost,127.0.0.1,orahub.oraclecorp.com,artifactory-slc.oraclecorp.com"
  ) else if /i [%option%] == [2] (
    set OK=true
    set DOCKER_FILE=webcomponents.Dockerfile
    set IMAGE_NAME=oliv-webcomp
		set RUN_CMD="docker run -p 9999:9999 -d %IMAGE_NAME%:latest"
		::                              tcp port used in the image
		::                         tcp port as seen from outside (this machine)
		::
    echo ---------------------------------------------------
    echo Reach http://localhost:9999/index.html from your browser.
    echo You can also log in using: docker run -it %IMAGE_NAME%:latest /bin/bash
    echo ---------------------------------------------------
  ) else if /i [%option%] == [3] (
    set OK=true
    set DOCKER_FILE=rpi.Dockerfile
    set IMAGE_NAME=oliv-rpi
		set RUN_CMD="docker run -p 8081:8080 -d %IMAGE_NAME%:latest"
		::                              tcp port used in the image
		::                         tcp port as seen from outside (this machine)
		::
    echo ---------------------------------------------------
    echo Reach http://localhost:8081/oliv-components/index.html from your browser.
    echo You can also log in using: docker run -it %IMAGE_NAME%:latest /bin/bash
    echo ---------------------------------------------------
  ) else if /i [%option%] == [4] (
    set OK=true
    set DOCKER_FILE=node-pi.Dockerfile
    set IMAGE_NAME=oliv-nodepi
		:: RUN_CMD="docker run -p 9876:9876 -t -i --device=/dev/ttyUSB0 %IMAGE_NAME%:latest /bin/bash"
		set RUN_CMD="docker run -p 9876:9876 -t -i --privileged -v /dev/ttyUSB0:/dev/ttyUSB0 -d %IMAGE_NAME%:latest"
		::                                                                       Device IN the docker image
		::                                                         Device name in the host (RPi) machine
		::                                           sudo access to the Serial Port
		::                              tcp port IN the docker image
		::                         tcp port as seen from outside (this machine)
		::
    :: echo See doc at https://github.com/OlivierLD/node.pi/blob/master/README.md
		ipconfig
    echo ---------------------------------------------------
    echo Reach http://[your-ip]:9876/data/demos/gps.demo.html in your browser.
    echo You can also log in using: docker run -it %IMAGE_NAME%:latest /bin/bash
    echo ---------------------------------------------------
  ) else if /i [%option%] == [5] (
    set OK=true
    set DOCKER_FILE=Dockerfile.node-debian
    set IMAGE_NAME=oliv-nodedebian
		:: RUN_CMD="docker run -p 9876:9876 --privileged -v /dev/tty.usbserial:/dev/ttyUSB0 -d %IMAGE_NAME%:latest"
		set RUN_CMD="docker run -p 9876:9876 -d %IMAGE_NAME%:latest"
		::                           tcp port used in the image
		::                      tcp port as seen from outside (this machine)
		::
		ipconfig
    echo ---------------------------------------------------
    echo Reach http://[your-ip]:9876/data/demos/gps.demo.html in your browser.
    echo You can also log in using: docker run -it %IMAGE_NAME%:latest /bin/bash
    echo ---------------------------------------------------
  ) else if /i [%option%] == [6] (
    set OK=true
    set DOCKER_FILE=rpi.mux.Dockerfile
    set IMAGE_NAME=oliv-nmea-mux
		:: RUN_CMD="docker run -p 9876:9876 -t -i --device=/dev/ttyUSB0 %IMAGE_NAME%:latest /bin/bash"
		set RUN_CMD="docker run -p 9999:9999 -t -i --privileged -v /dev/ttyUSB0:/dev/ttyUSB0 -d %IMAGE_NAME%:latest"
 		::                                                                    Device IN the docker image
		::                                                      Device name in the host (RPi) machine
		::                                        sudo access to the Serial Port
		::                           tcp port IN the docker image
		::                      tcp port as seen from outside (this machine)
		::
    :: echo See doc at https://github.com/OlivierLD/node.pi/blob/master/README.md
		ipconfig
    echo ---------------------------------------------------
    echo Reach http://[your-ip]:9999/web/index.html in your browser.
    echo REST operations available: http://localhost:9999/mux/oplist.
    echo You can also log in using: docker run -it %IMAGE_NAME%:latest /bin/bash
    echo ---------------------------------------------------
  ) else if /i [%option%] == [7] (
    set OK=true
    set DOCKER_FILE=golang.Dockerfile
    set IMAGE_NAME=oliv-go
    set RUN_CMD="docker run -d %IMAGE_NAME%:latest"
    ::
    echo ---------------------------------------------------
    echo Log in using: docker run -it %IMAGE_NAME%:latest /bin/bash
    echo ---------------------------------------------------
  ) else if /i [%option%] == [8] (
    set OK=true
    set DOCKER_FILE=rpidesktop.Dockerfile
    set IMAGE_NAME=oliv-pi-vnc
    set RUN_CMD="docker run -d %IMAGE_NAME%:latest"
    ::
    echo ---------------------------------------------------
    echo Log in using: docker run -it --rm -p 5901:5901 -p 8080:8080 -e USER=root %IMAGE_NAME%:latest /bin/bash
    echo - then run 'vncserver :1 -geometry 1280x800 (or 1440x900, 1680x1050, etc) -depth 24'
    echo - then use a vncviewer on localhost:1, password is 'mate'
    echo - then 'node server.js', and reach http://localhost:8080/oliv-components/index.html ...
    echo ---------------------------------------------------
  ) else if /i [%option%] == [9] (
    set OK=true
    set DOCKER_FILE=spark-debian.Dockerfile
    set IMAGE_NAME=oliv-spark
    set RUN_CMD="docker run -d %IMAGE_NAME%:latest"
    ::
    echo ---------------------------------------------------
    echo Log in using: docker run -it --rm -e USER=root %IMAGE_NAME%:latest /bin/bash
    echo ---------------------------------------------------
  ) else if /i [%option%] == [10] (
    set OK=true
    set DOCKER_FILE=tensorflow.Dockerfile
    set IMAGE_NAME=oliv-tf-vnc
    set RUN_CMD="docker run -d %IMAGE_NAME%:latest"
    ::
    echo ---------------------------------------------------
    echo You can log in using: docker run --interactive --tty --rm --publish 5901:5901 --publish 8888:8888 [--env USER=root] [--volume tensorflow:/root/workdir/shared] %IMAGE_NAME%:latest /bin/bash
    echo                    or docker run -it --rm -p 5901:5901 -p 8888:8888 -e USER=root -v tensorflow:/root/workdir/shared %IMAGE_NAME%:latest /bin/bash
    echo - then run 'vncserver :1 -geometry 1280x800 (or 1440x900, 1680x1050, etc) -depth 24'
    echo - then use a vncviewer on localhost:1, password is 'mate'
    echo - then (for example) python3 examples/mnist_cnn.py ...
    echo        or python3 examples/oliv/01.py ...
    echo   Several samples are available in the examples folder.
    echo ---------------------------------------------------
		echo To start Jupyter Notebook, type: jupyter notebook --allow-root --ip 0.0.0.0 --no-browser
		echo   - Default port 8888 is exposed, you can use from the host http://localhost:8888/?token=6c95d878c045212bxxxxxx
    echo ---------------------------------------------------
  ) else (
    echo What? Unknown command [%option%]
  )
  ::
if [%OK%] == [false] (
  goto menutop
)
::
::
if "%DOCKER_FILE%" != "" (
  ::
  :: Proxies, if needed
  :: export HTTP_PROXY=http://www-proxy.us.oracle.com:80
  :: export HTTPS_PROXY=http://www-proxy.us.oracle.com:80
  ::
  set EXTRA=
  if "%EXTRA_PRM%" != "" (
    set EXTRA="with %EXTRA_PRM%"
  )
  echo ---------------------------------------------------
  echo Generating %IMAGE_NAME% from %DOCKER_FILE% %EXTRA%
  echo ---------------------------------------------------
  :: Possibly use --quiet
  docker build -f %DOCKER_FILE% -t %IMAGE_NAME% %EXTRA_PRM% .
  ::
  :: Now run
  echo Now running %RUN_CMD%...
  %RUN_CMD%
)
::
:eos
@endlocal
