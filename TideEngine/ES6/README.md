# ES6 Tide Engine, WiP (big time).
This is a tentative, the Java version remains the reference.

---
**First thing to do, once**:   
Unzip the file `json/json.zip` in the directory it is in. You should end up with a structure like
```
 +- ES6
 | +- json
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

For Python, use
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
For NodeJS, use
```
node server.js &
```
Default port number is `8888`.  
_Note_: Use `./kill.node.sh` to stop the NodeJS server.

---
There is also now an `index.html`, listing the examples you can get inspiration from.

---
More to come about Java...

---

TODO: Data management in ES6, on any **TimeZone** (not Time Offset).
