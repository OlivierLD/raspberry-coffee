# Useful commands

Read this [Good paper](https://medium.com/the-code-review/top-10-docker-commands-you-cant-live-without-54fb6377f481)


```
$ docker run dockerinaction/hello_world
```

```
$ docker run -it dockerinaction/hello_world /bin/bash
$ docker run --interactive dockerinaction/hello_world /bin/bash
```

```
$ docker inspect dockerinaction/hello_world
```

```
$ docker history dockerinaction/hello_world
```

```
$ docker run --detach --name web nginx:latest
```

```
$ docker run --detach --name mailer dockerinaction/ch2_mailer
```

```
$ docker run --interactive --tty --link web:web --name web_test busybox:latest /bin/sh
/ # wget -O - http://web:80/
```

```
$ docker run -it --name agent --link web:insideweb --link mailer:insidemailer dockerinaction/ch2_agent
System up.
System up.

[Ctrl] P Q
```
```
$ docker ps [-a]
```

Start an exited instance and (re-)connect to it:
```
$ docker ps -a
CONTAINER ID        IMAGE                COMMAND             CREATED             STATUS              PORTS               NAMES
891075762de6        oliv-devenv:latest   "/bin/bash"         35 minutes ago      Exited (128) 13 seconds ago             dev-env
$ docker start dev-env
891075762de6
$ docker ps -a
CONTAINER ID        IMAGE                COMMAND             CREATED             STATUS              PORTS               NAMES
891075762de6        oliv-devenv:latest   "/bin/bash"         36 minutes ago      Up 1 second         5901/tcp            dev-env
$ docker exec -it dev-env /bin/bash
git version 2.11.0
node:v9.11.2
...

```

```
$ docker top dev-env
```
