# A Web Socket Robot on wheels

Drive a robot from a browser

---

Install the required WebSocket node module
```
$ cd node
$ npm install
$ cd ..
```

Compile the Java code:
```
$ ../../gradlew shadowJar
```

Then start tghe node server and run the java code
```
$ cd node
$ node robot.server.js &
$ cd ..
$ ./robot.pilot.sh
```

Finally, from your browser (laptop, tablet, smartphone), reach
`http://[server-name]:9876/data/robot.pilot.html`.


