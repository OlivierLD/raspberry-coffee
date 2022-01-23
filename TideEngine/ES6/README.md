# ES6 Tide Engine, WiP, big time.
This is a tentative, the Java version remains the reference.

**First thing to do, once**:   
Unzip the file `json/json.zip` in the directory it is in. You should end up with a structure like
```
 +- ES6
 | +- json
 | | - constituents.js
 | | - stations.js
 | +- tideEngine.js
 . . .
```
This heavily uses the `module` feature of HTML/ES6. This requires
all this to run on top of a Web Server, HTML pages cannot be used as static resources.

NodeJS, Python, Java can be used...

For Python, use
```
python3 -m http.server [port-number] &
```
Default port number is `8000`.

Then reach <http://localhost:8000/Basic.02.html>

More to come about NodeJS and Java...
