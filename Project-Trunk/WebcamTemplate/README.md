# Foundation for a WebCam Web Application

### Tags and Keywords
`camera`, `images`, `raspistill`, `REST Server`, `no flickering`.

### Several components
- A Java thread (`image.snap.SnapSnapSnap`) that uses a camera (WebCam, PiCam, whatever, see the code to get the right one) to take pictures at a given time interval
- A REST server that goes along with it, allowing an HTTP access to the picture taken above
- A small Web App that pings the REST server, and display the last picture in a web page, refreshable at will (automatically or manually).

### Compile and run
On the Pi:
```
 $ ../../gradlew clean shadowJar
 $ ./server.sh
``` 
From any browser, an any machine on the same network as the Pi: 
```
 http://[the-pi-address]:1234/web/index.html
``` 
 
### Next 
- OpenCV processing of the snapshots

---
