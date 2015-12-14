## Some "real" samples involving the components of the other projects ##
---

### Home Weather Station ###
This one uses the <code>SDLWeather80422</code> class, from the <code>WeatherStation</code> project.
You can
- Read the data from the station 
- Simulate the data read from the station

Those data can then be rendered in different ways, with a Web Interface.
They are - for now - using a nodejs server and its WebSocket module, running on the Raspberry Pi.

After installing NodeJS (try [this](http://www.lmgtfy.com/?q=install+node+js+raspberry+pi)) on the Raspberry PI, in the <code>node</code> directory, install the WebSocket module:
<pre>
Prompt> npm install websocket
</pre>

Then you can start the node server:
<pre>
Prompt> node weather.server.js
</pre>

From another console, then start the process that will read the <code>SDLWeather80422</code>, and feed the WebSocket server:

<pre>
Prompt> ./weather.station.reader
</pre>

You can also start a simulator, in case you  are not on the Raspberry PI, and want to make some tests:
<pre>
Prompt> ./weather.simulator
</pre>

Then you can visualize the data in a browser, using a URL like 
<code>http://raspberrypi:9876/data/weather.station/index.html</code>

The analog console can be reached from <code>http://localhost:9876/data/weather.station/analog.html</code>, 
and it supports query string parameters <code>border</code> and <code>theme</code>.

<code>border</code> can be <code>Y</code> or <code>N</code>, and <code>theme</code> can be <code>black</code> or <code>white</code>.

_For example_:<code>http://localhost:9876/data/weather.station/analog.html?border=N&theme=white</code>.

---

See [here](http://www.lediouris.net/RaspberryPI/WeatherStation/readme.html).
