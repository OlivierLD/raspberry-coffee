### Docker

Get Docker on your system : https://store.docker.com/search?type=edition&offering=community

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


Get the CONTAINER ID like a335f585deb0
```bash
$ docker stop a335f585deb0
```

To drop the image:
```bash
$ docker rmi oliv-image
```

### Examples

```bash
$ cp Dockerfile.web-components Dockerfile
$ docker build -t oliv-image .
$ docker run -p 8081:8080 -it oliv-image /bin/bash
root@7e754f8732a0:/workdir/raspberry-pi4j-samples/WebComponents# node server.js
```

Then reach `http://localhost:8081/oliv-components/index.html`

etc, etc...
