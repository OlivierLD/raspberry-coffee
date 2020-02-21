- To be opened as a standalone project.
- Here for comparison with the others, specially in term of size.
- Can run on Docker (`Dockerfile` provided)

---

After setting your `JAVA_HOME` as expected, from the `raspberry-coffee` project, requires:

In `common-utils` (common utilities):
```bash
http-tiny-server $ ../gradlew clean shadowJar install
```

In `http-tiny-server` (for the REST part):
```bash
http-tiny-server $ ../gradlew clean shadowJar install
```

In `ADC` (for the Sensors part:
```bash
ADC $ ../gradlew clean shadowJar install
```

Then from this project directory:
```bash
$ ./gradlew clean shadowJar
$ java -Dhttp.port=9876 -jar build/libs/small-docker-sensors-1.0-all.jar
```
And from another shell:
```bash
$ curl -X GET http://localhost:9876/light/ambient
  {"percent":77.41936} 
```

### Docker
Make sure you've run
```bash
$ ./gradlew clean shadowJar
```
Then
```bash
$ docker build . -t micro-server
$ docker run -p 9876:9876 micro-server
```
The same `curl` request should work the same way as before!

---

### Sizes comparison
Compare the Docker images:
```bash
$ docker images
REPOSITORY                                                               TAG                         IMAGE ID            CREATED             SIZE
micro-server                                                             latest                      db8243f5c873        9 minutes ago       240MB
micronaut                                                                latest                      2073cf66fe3f        3 days ago          253MB
. . .
```
The Docker images have pretty much the same size.

Now, compare the `jar` sizes. Theses are "fat-jars", containing _all_ the app needs to run, thanks to `shadowJar`.
```bash
$ ls -lisah build/libs/
  total 6216
  8690058998    0 drwxr-xr-x  3 olediour  staff    96B Feb 21 08:41 .
  8690058975    0 drwxr-xr-x  6 olediour  staff   192B Feb 21 08:41 ..
  8690058999 6216 -rw-r--r--  1 olediour  staff   2.7M Feb 21 08:41 small-docker-sensors-1.0-all.jar
```
Compare to 
```bash
$ ls -lisah ../micronaut-sensors/sensors/build/libs/
  total 30728
  8690061628     0 drwxr-xr-x  3 olediour  staff    96B Feb 21 09:02 .
  8690061583     0 drwxr-xr-x  7 olediour  staff   224B Feb 21 09:02 ..
  8690061629 30728 -rw-r--r--  1 olediour  staff    15M Feb 21 09:02 sensors-0.1-all.jar
```

This is `15M`, vs `2.7M`, no comment!

---
