# Useful commands

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
