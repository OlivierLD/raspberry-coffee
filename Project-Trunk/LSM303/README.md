### Pitch and roll with an LSM303
Read an LSM303 I2C board to get the pitch and roll. Feeds a WebSocket server with the data.
An HTML page displays a boat, graphically, with the appropriate picth and roll.

Run
```bash
 $> ../gradlew clean shadowJar
```
Then, from one shell
```bash
 $> cd node
 $> node server.js
```
And from another one
```bash
 $> ./lsm303.sh  # was ./pitchroll

```
Then from a browser, reach the machine where `node` is running:
```
 http://<machine-name>:9876/data/pitchroll.html
```

![WebGL UI](./pitchroll.png)

