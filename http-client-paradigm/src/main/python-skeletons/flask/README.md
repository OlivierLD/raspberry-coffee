# Flask
### HTTP Server in Python
See
- [Python Web Server with Flask](https://projects.raspberrypi.org/en/projects/python-web-server-with-flask/)
- [Flask-RESTful](https://flask-restful.readthedocs.io/en/latest/#:~:text=Flask%2DRESTful%C2%B6,be%20easy%20to%20pick%20up)

#### Installation
```
$ pip3 install flask
```
See what's installed:
```
$ pip3 list
```

#### Run the HTTP Server
Drill down to the `flaskapp` folder, and run
```
$ python3 app.py
```
And from another terminal:
```
$ curl -X GET http://localhost:5000/
Hello world
```
