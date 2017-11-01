# Image manipulation utilities
### Can be used on the server side, or in standalone

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
