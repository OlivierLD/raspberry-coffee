## REST Tide Engine

This is a tentative Tide Application, based on Server-Side Java implementing REST APIs for a Tide Computer.
The rendering will be done through HTML5 and JavaScript querying the REST APIs.  

If that one works, then we can really move away from Swing.

```
 /GET /tide-stations
 /GET /tide-stations/{station}
 /POST /tide-stations/{station}/wh?from=XXX&to=YYY
 /POST /tide-stations/{station}/wh/details?from=XXX&to=YYY
 
 ... etc
 
```

### Features (to be)
- Web UI for tidal curves
- REST APIs for server-side computation 
- Publishing: Server side `pdf` generation

---

The engine is based on XML data, stored in `xml.zip`. Those data are generated after the 
harmonic data files found in the `harmonics` directory.
 Re-generating those is not necessary, but in case you're interested, run
```bash
 $ ../gradlew --no-daemon harmonicsXML
```
The sources of the generator are obviously available.

Implements **two** REST Request Managers.
- One for tide data
- One for celestial data

### Why are we using XML instead of the raw `txt` file?
The format of the harmonic files is a proprietary format. To be used efficiently, the file has to 
be parsed and loaded in memory for the data it contains to be available in a timely manner.

This could be quite demanding for a small machine like the Raspberry PI (the`Zero` has "only" 518 Mb of RAM), even the generation of the XML files
can be challenginfg for the Raspberry PI.

Using a SAX Parser allows the amount of memory to use to substancially shrink.
As opposed to a DOM Parser that loads the DOM representation of the document in memory,
the SAX parser scans it un til the expected data are found.

> Note about the encoding: The XML Data are 'ISO-8859-1' encoded. For an easier access, those
> elements (mainly the String elements, like station names) are returned as 'UTF-8' encoded by the REST API.

> See in the HTML Examples how to render the strings correctly.

### To run the examples
- Build the soft
```bash
 $ ../gradlew clean shadowJar
```
- Then start then server
```bash
 $ ./runTideServ
```
- From a browser, access `http://host:9999/web/index.html`, where`host` is the name or IP of the machine where the server runs.

---  