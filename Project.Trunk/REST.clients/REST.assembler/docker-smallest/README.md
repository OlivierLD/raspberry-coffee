- To be opened as a standalone project.
- Here for comparison with the others, specially in term of size.
- Can run on Docker (`Dockerfile` provided)

---

After setting your `JAVA_HOME` as expected, from the `raspberry-coffee` project, requires:

In `common-utils`:
```bash
http-tiny-server $ ../gradlew clean shadowJar install
```

In `http-tiny-server`:
```bash
http-tiny-server $ ../gradlew clean shadowJar install
```

In `ADC`:
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

### Size comparison
TODO
