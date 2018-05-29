### Docker

Get Docker on your system : https://store.docker.com/search?type=edition&offering=community

#### Pre-defined Docker images
This project also provides a script that will build pre-defined Docker images (different OS's, with various features).
Look into the script, the images are defined by the files `*.Dockerfile`.

Just run:
```
 $ ./image.builder.sh
 +-------------- D O C K E R   I M A G E   B U I L D E R --------------+
 | 1. Nav Server, Debian                                               |
 | 2. Web Components, Debian                                           |
 | 3. To run on a Raspberry PI, Java, Raspberry Coffee, Web Components |
 | 4. Node PI, to run on a Raspberry PI                                |
 | 5. Node PI, to run on Debian                                        |
 | 6. GPS-mux, to run on a Raspberry PI (logger)                       |
 | 7. Golang, basics                                                   |
 | 8. Raspberry PI, MATE, with java, node, web comps, VNC              |
 +---------------------------------------------------------------------+
 | Q. Oops, nothing, thanks, let me out.                               |
 +---------------------------------------------------------------------+
 == You choose =>
```


#### Let's go

From the directory the `Dockerfile` lives in:
```bash
$ docker build -t oliv-image .
```
and to log in the image:
```bash
$ docker run -it oliv-image /bin/bash
```
to start the server:
```bash
$ docker run --name rest-nav-server -p 8080:9999 -d oliv-image
```

From the host (where the `docker` command was fired), reach for example http://localhost:8080/oplist

Yeah!

From another shell:
```bash
$ docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS                      PORTS               NAMES
a335f585deb0        oliv-image          "/bin/bash"              36 minutes ago      Exited (0) 35 minutes ago                       confident_proskuriakova
f36550370ea8        445821efd8a0        "/bin/sh -c 'echo “g…"   44 minutes ago      Exited (2) 44 minutes ago                       affectionate_davinci
```


Get the CONTAINER ID like `a335f585deb0`
```bash
$ docker stop a335f585deb0
```

To drop the image:
```bash
$ docker rmi oliv-image
```

### Examples

```bash
$ docker build -f webcomponents.Dockerfile -t oliv-image .
$ docker run -p 8081:8080 -it oliv-image /bin/bash
root@7e754f8732a0:/workdir/raspberry-pi4j-samples/WebComponents# node server.js
```

Then reach `http://localhost:8081/oliv-components/index.html`

etc, etc...

Several images can be built from the script `image.builder.sh`.

### Case Study
You have a `nodejs` project you want to share with others.

The application reads GPS data through a Serial port, and feeds a `WebSocket` server.
The data can then be visualized through a Web interface.

To enable everything, you need to:
1. Have a Raspberry PI
1. Flash its SD card and connect it to a network
1. Install build tools
1. Install `git`
1. Install `NodeJS` and `npm`
1. Clone the right `git` repository
1. Install *_all_* the required `node` modules
1. Drill down into the right directory
1. Start the `node` server with the right script
1. Access the Raspberry PI from another machine on the same network, and reach the right HTML page.

This is certainly not difficult, but there are many ways to do several mistakes at each step
of the process.

`Docker` can take care of the steps `3` to `9`.
It will build the image, and then run it.
The image can also be pushed to a repository, so users would not have to build it.
Just to run it after downloading it.

The only pre-requisite would be to have installed `Docker` on the machine (the Raspberry PI here),
as explained at the top of this document.

In this case, the full `Docker` image creation (named `oliv-nodepi` below) comes down to:
```bash
 $ docker build -f Dockerfile.node-pi -t oliv-nodepi .
Sending build context to Docker daemon  752.6kB
Step 1/20 : FROM resin/raspberrypi3-debian:latest
 ---> c542b8f7a388
Step 2/20 : MAINTAINER Olivier LeDiouris <olivier@lediouris.net>
 ---> Using cache
 ---> b2ff0d7c489f
Step 3/20 : ADD nodepi.banner.sh /
 ---> 535733298dd1
Step 4/20 : RUN echo "alias ll='ls -lisah'" >> $HOME/.bashrc
 ---> Running in 09baf7261a55
Removing intermediate container 09baf7261a55
 ---> 71e1e4c95663
Step 5/20 : RUN apt-get update
 ---> Running in 5d817a941a14
Get:1 http://security.debian.org jessie/updates InRelease [94.4 kB]
Get:2 http://archive.raspbian.org jessie InRelease [14.9 kB]
Get:3 http://archive.raspberrypi.org jessie InRelease [22.9 kB]

...

npm notice created a lockfile as package-lock.json. You should commit this file.
added 166 packages in 81.166s
Removing intermediate container 13986530db28
 ---> 051eb94b8a3c
Step 19/20 : EXPOSE 9876
 ---> Running in 67b587845fe0
Removing intermediate container 67b587845fe0
 ---> 46973b7ba9ac
Step 20/20 : CMD ["npm", "start"]
 ---> Running in 153bf2ea02ad
Removing intermediate container 153bf2ea02ad
 ---> 6bf3d76d38ae
Successfully built 6bf3d76d38ae
Successfully tagged oliv-nodepi:latest
ed9a7d9042dddd3939b1788cf0e89d16f5273192a6456266507f072f90ce91bc
 $
```
Once the step above is completed, plug in your GPS, and run
```bash
 $ docker run -p 9876:9876 -t -i --privileged -v /dev/ttyUSB0:/dev/ttyUSB0 -d oliv-nodepi:latest
```
Then from a machine seeing the Raspberry PI on its network (it can be the Raspberry PI itself),
reach http://raspi:9876/data/demos/gps.demo.wc.html in a browser.

![Running](DockerAtWork.png)

This shows you the position the GPS has computed, and the satellites in sight.

You can also login to the image:
```bash
 $ docker run -it oliv-nodepi:latest /bin/bash

#     #                                 ######    ###
##    #   ####   #####   ######         #     #    #
# #   #  #    #  #    #  #              #     #    #
#  #  #  #    #  #    #  #####   #####  ######     #
#   # #  #    #  #    #  #              #          #
#    ##  #    #  #    #  #              #          #
#     #   ####   #####   ######         #         ###

git version 2.1.4
node:v9.11.1
npm:5.6.0
root@b9679d0d65a7:/workdir/node.pi#

```
... and do whatever you like.

The build operation needs to be done once. There is no need to do it again as long as no
change in the image is required.

### Stuff...

```bash
 $ CID=`docker run -d oliv-go:latest`
 $ docker logs $CID
 Hello go world!
 $
```

--------------------------------------------------------------------------
