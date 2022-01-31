# Several faxes, reworked _on the Client side_.
Features:
- For a given zone, download the faxes from the internet
- Start a Web Server (Python, NodeJS, or Java)
- Rework the faxes (color, orientation, offsets) _on the client side_, in ES6
- Display the generated page

Issues:
- This is done locally from a Shell Script because of some Cross Origin Resource Sharing (CORS) issues when running on a remote server...

## A Pure HTML5-CSS3-ES6 Tentative approach
We want to
- Read weather faxes from the internet
- Rescale, rotate them, change the black color to something else, make the white transparent
- Display them, side-by-side, on top of each other, or anything like that

### Color change issues
To change colors in an image, you can get to its pixel, by using `getImageData` on the canvas the image is displayed on.
- first issue: the method may raise a Cross Origin Resource Sharing (CORS) error if the image is reached on another server than the one the web page runs on,
  which is the case here (we reach the NOAA website to get the faxes).
    - To address that, we copy the image(s) locally, before loading the page, from the script `fax.processor.sh`.
- The same CORS error will be raised if the page is loaded from the file system (`file://`)    
    - To address that, we start a small python http server. Then we can use `http://`.
    - It could also be the `tiny-http-server`, from this project, NodeJS, or whatever can act as an HTTP Server.
  
### Rotate, rescale
This is done on the canvas(es) holding the reworked (colors changed) images,
with regular CSS (through ES6).

## Current status
Start the script `./fax.processor.sh`, and see for yourself.  
> Type `./fax.processor.sh --help` for help.
```
$ ./fax.processor.sh --help
For help, type ./fax.processor.sh --help
----------------------------------
Managing prm --help
Usage is:
 ./fax.processor.sh [--flavor:none|python|node|java] [--port:8080] [--kill-server:true] [--verbose] [--browser:true] [--help]
    --flavor: The flavor of the HTTP server to start. Default python. 'none' does not start a server. It may reuse an already started one.
    --port: HTTP port to use. Default 8080.
    --kill-server: Kill the server once the page is displayed. Default false.
    --browser: Default true.
    --spot:atl|pac - Default atl.
    --verbose, or -v. Default false.
    --help. Guess what! Exit after displaying help.
```

The script would:
- Download the expected faxes on the local file system
- Start a small HTTP Server
- Open the corresponding web page (`process.atl.faxes.html` or `process.pac.faxes.html`, depending on your `--spot` option) in your default browser, showing the faxes, recolored, rescaled, rotated.

The code to change the colors, orientation, and size of the faxes is in `faxes.js`. 

> _Note_: If you experience some rendering issues, try a force-reload (Shift+Ctrl+R or its equivalent) from your browser.  
> Seems to work fine on Firefox, OK on Chrome or Safari, I've seen some issues on Brave, could not explain them yet.

![4 Faxes](./4.faxes.png)

> _Note_: If you've not used the `--kill-server` option, you may kill the web server that was started by using
> - kill.py.sh
> - kill.node.sh
> - kill.java.sh
> 
> depending on the flavor you used.

---
