# Demos

---

[<< Back](./README.md)

---

All the demos can be started from [http://localhost:9999/web/index.html](http://localhost:9999/web/index.html).

![GPS Demo](./docimages/gps.demo.png "GPS Demo")
Plug a GPS on the Serial Port, start the multiplexer, and launch [http://localhost:9999/web/gps.demo.html](http://localhost:9999/web/gps.demo.html) in a browser.
This is pinging the `/cache` REST service every second.

Satellites are displayed on the chart, and on the right pane.
When available, Speed Over Ground and Course Over Ground are displayed too.

With Google Maps (requires an Internet connection):
![GPS Demo, google maps](./docimages/googlemaps.png "GPS Demo with Google Maps")

With [LeafletJS](http://leafletjs.com) (requires an Internet connection):
![GPS Demo, LeafletJS](./docimages/leaflet.png "GPS Demo with LeafletJS")

---
