# Several faxes, reworked.
## A Pure HTML5-CSS3-ES6 Tentative approach
We want to
- Read weather faxes from the internet
- Rescale, rotate, them, change the black color to something else, make the white transparent
- Display them, side-by-side, on top of each other, or anything like that

### Color change issues
To change colors in an image, you can get to its pixel, by using `getImageData` on the canvas the image is displayed on.
- first issue: the method may raise a Cross Origin Resource Sharing (CORS) error if the image is reached on another server than the one the web page runs on,
  which is the case here (we reach the NOAA website to get the faxes).
    - To address that, we copy the image(s) locally, before loading the page, from the script `fax.processor.sh`.
- The same CORS error will be raised if the page is loaded from the file system (`file://`)    
    - To address that, we start a small python http server. Then we can use `http://`.
    - It could also be the `tiny-http-server`, from this project.
  
### Rotate, rescale
This is done on the canvas(es) holding the reworked (colors changed) images,
with regular CSS (through ES6).

## Current status
Start the script `./fax.processor.sh`, and see for yourself.

---
