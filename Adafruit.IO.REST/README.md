## REST interface with Adafruit-IO
The project contains small classes showing how to communicate with [Adafruit-IO](https://io.adafruit.com) through
REST services.

You need to have your own key, and provide it as a system variable, using
`-Dkey=50c0707070c070302030a01040d020a0a0908050` from the command line.

In the examples, the code feeds and reads data to a feed named `onoff`, which is a switch. It takes the values `ON`
and `OFF`.

The class `adafruit.io.rest.HttpClient` contains a main that pushes data to the `onoff` feed, you can take a look at
 your Adafruit-IO dashboard and see the widget change its value, when running the code.

Similarly, the sample named `adafruit.io.sample.Poll` reads the feed, and will exit when its value changes.
 You can change the value from the Adafruit-IO dashboard, and the program will exit.

The classes depend on `json.org` to manage the json objects used as payload. The dependency is mentioned in the
gradle build file.

To run the classes, use a command line similar to this ones:
```
CP=./classes
CP=$CP:./lib/json.jar
#
java -cp $CP -Dkey=50c0707070c070302030a01040d020a0a0908050 adafruit.io.rest.HttpClient
#
java -cp $CP -Dkey=50c0707070c070302030a01040d020a0a0908050 adafruit.io.sample.Poll

```
The classpath and the key depend on your own configuration...
