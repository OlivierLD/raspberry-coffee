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
$ docker run —name rest-nav-server -p 8080:9999 -d oliv-image
```

From the host (where the docker command was fired), reach for example http://localhost:8080/oplist
Yeah!

From another shell:
```bash
$ docker ps
```
Get the CONTAINER ID like 0d0ffd4a4fa5
```bash
$ docker stop 0d0ffd4a4fa5
```

To drop the image:
```bash
$ docker rmi oliv-image
```
