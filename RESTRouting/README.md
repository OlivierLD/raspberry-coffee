## GRIB Reader
And routing.

The server generates a json document, as in `GRIBBulk`.
The rendering is (to be) done on the client (HTML5/CSS) over a map. See in `RESTNavServer/web`.

And later, add the faxes.

Run `./runGRIBserver`:

![Test API](./screenshot.00.png)

### TODO
- GRIB Request generator ?
- Routing
- Remove JQuery dependencies, move to pure ES6
- Other grib providers than `saildocs`. (See <https://opengribs.org/en/gribs>, <https://www.zygrib.org/>, ...)
