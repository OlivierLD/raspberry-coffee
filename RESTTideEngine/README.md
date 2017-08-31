## REST Tide Engine

This is a tentative Tide Application, based on Server-Side Java implementing REST APIs for a Tide Computer.
The rendering will be done through HTML5 and JavaScript querying the REST APIs.  

If that one works, then we can really move away from Swing.

```
 /GET /tide-stations
 /GET /tide-stations/{station}
 /GET /tide-stations/{station}/data?from=XXX&to=YYY
 /GET /tide-stations/{station}/data/details?from=XXX&to=YYY
 
 ... etc
 
```

### TODO
Dig out the `XML` generation from the `txt` harmonic files.
