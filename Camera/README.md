# Image manipulation utilities
### Can be used on the server side, or in standalone
> Note: This is very lightweight. For real image manipulation, have a look at OpenCV.

## Examples

#### Change color
- Take 2 faxes from NOAA (black and white, opaque)
- On the first one: make the white transparent, turn black into blue.
- On the second one: make the white transparent, turn black into red.
- Stack them on a web page, and display it in a browser.

```bash
 $ ../gradlew [--no-daemon] shadowJar
 $ ./changecolor
```

## TODO
- Download a fax, make it transparent and change its color.
- Draw chart under the fax, Mercator projection
- Display fax(es) on top of the chart
- Move fax on top of the chart, to find its right position (and size, by scaling it)
- Store and browse composites in a catalog
