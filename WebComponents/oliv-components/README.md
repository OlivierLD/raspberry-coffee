## Oliv Web Components

_Those components requires absolutely **NO** external library._

They do require a WebComponents-savvy browser though. Most of them are (or will shortly be...).

### Live Demos
Requires `nodejs` to be available on your system.

To see the components at work, after cloning the repo, start the `node` server _**from the `WebComponents` directory**_, and load `index.html` in a browser:

```bash
 $> node server.js
```
Then load [http://localhost:8080/oliv-components/index.html](http://localhost:8080/oliv-components/index.html) in a WebComponents-enabled web browser.

### Components list
- Pluviometer. &#9989; Done.
- Thermometer. &#9989; Done
- Jumbo Display. &#9989; Done
- Direction Display. &#9989; Done
- Analog Display. &#9989; Done
- Apparent Wind. TODO
- Globe and Map (with `import` and `module`). &#9989; Done <!-- &#10140; WIP -->
- Compass Rose. &#9989; Done
- Digital Display. TODO
- Date and Time display. TODO
- Watch/Clock (analog). &#9989; Done
- Marquee. &#10140; WIP
- Evolution Displays. TODO
- Graphs. TODO
- Sky Map and Star Finder (along with some `REST` service(s)). &#10140; WIP.
- Satellite plotter?
- Tide Graph?
- Current Display?
- Boat Overview?

---

##### Artifacts
- Javascript Modules (careful with Firefox 58 and below...)
    - defining the component's parameters (properties) and behavior
- Examples, in `index.html` and on [`CodePen`](http://codepen.io/OlivierLD/).
    - including animations examples
- CSS rules and classes (in their own stylesheet, `web-components.css`)

---

#### TODO
- JSDoc
- Local `npm` registry

#### CodePen (might not be 100% in sync... But close)
The point of truth remains this repo.

Components above, live at [CodePen](http://codepen.io/OlivierLD/), when released.

- [Jumbo Display](https://codepen.io/OlivierLD/pen/VQyVjy).
- [Pluviometer](https://codepen.io/OlivierLD/pen/oEPKgg).
- [Thermometer](https://codepen.io/OlivierLD/pen/KQQEEp).
- [Direction](https://codepen.io/OlivierLD/pen/bLjwdj).
- [Analog Display](https://codepen.io/OlivierLD/pen/QQBYEw).
- [Compass Rose](https://codepen.io/OlivierLD/pen/aqaLQq).
- [World Map](https://codepen.io/OlivierLD/pen/xYQbmb).

#### Firefox 58
If you are having trouble running the WebComponents in Firefox 58, see
[this document](https://www.designedbyaturtle.co.uk/2015/how-to-enable-web-components-in-firefox-shadow-dom/).

#### Transpilation - Warning: could not get it to work correctly.
Use `babel`, as explained [here](https://babeljs.io/docs/usage/cli/).

```bash
 $ npm install --save-dev babel-cli babel-preset-env
```
Create a `.babelrc`
```bash
 $ echo '{ "presets": ["env"] }' > .babelrc
```
And run the transpilation:
```bash
 $ npx babel oliv-components/widgets --out-dir oliv-components/lib
```
The `lib` directory now contains the transpiled files.

---
&copy; 2018, by Oliv Soft.

