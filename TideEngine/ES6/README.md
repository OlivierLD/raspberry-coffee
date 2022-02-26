# ES6 Tide Engine, WiP (big time).
This is a tentative, the Java version remains the reference.  
It could also be seen as an exercise...  
The data files are (very) big, there are more than 4000 stations to deal with... But we want to see if it is possible to
do all the calculations on the client side.  
Then we would see if that is any useful.

Should also be used from NodeJS.

---
**_First thing to do, once_**:   
The data files we need are big.  
To offload the git repo, they are zipped together.  
Unzip the file `json/json.zip` in the directory it is in. You should end up with a structure like
```
 +-+- ES6
 | +-+- json
 | | +- constituents.js
 | | +- stations.js
 | +- tideEngine.js
 |
 . . .
```
---

This code heavily uses the `module` feature of `HTML5/ES6`. 
This `module` allows the usage of `imports`.  
This requires all this to run on top of a Web Server, HTML pages cannot be used as static resources.

For the HTTP server, NodeJS, Python, Java can be used...

### For Python
Use
```
python3 -m http.server [port-number] &
```
Default port number is `8000`.  
_Note_: Use `./kill.py.sh` to stop the Python server.  

Then reach <http://localhost:8000/Basic.02.html>, try to look for `Port-Navalo`  
(Compare to `test/.../SimplestMain`)

To see some basic steps:
- <http://localhost:8000/Basic.01.html>
- <http://localhost:8000/scratch.html>

---
### For NodeJS
Use
```
node server.js &
```
Default port number is `8888`.  
_Note_: Use `./kill.node.sh` to stop the NodeJS server.

### For Java
This repo contains a small Java HTTP server.  
To build it:
```
cd ../../http-tiny-server/
../gradlew shadowJar
cd -
```
If it has been built, then you can 
```
./java.server.sh &
```
Default port number is `8080`.  
_Note_: Use `./kill.java.sh` to stop the Java server.

---
There is also for now an `index.html`, listing some examples you can get inspiration from.

---

TODO: Data management in ES6, on any **TimeZone** (_not_ Time Offset).
