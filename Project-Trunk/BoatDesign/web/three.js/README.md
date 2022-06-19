# Three.js
- First, install `ThreeJS` as mentioned [here](https://threejs.org/docs/#manual/en/introduction/Installation), using `npm`.
- Use the templates (and the [three.js examples](https://threejs.org/docs/index.html#manual/en/introduction/Creating-a-scene)) to generate your own code
- Start the server: `node server.js`
- Reach the url in your browser, like `http://localhost:8080/index.03.html`

> Some problems occur when loading the `three.module.js`...
> ```
> Uncaught TypeError: Failed to resolve module specifier "three". Relative references must start with either "/", "./", or "../".
> ```
> imports from CDN seem to work.   
> _Note_: 
> - in `index.04.html`, the file `calculated.js` is derived from the `calculated.json` generated from the Swing UI launched from `design.sh`,
> launch the program with `-Dspit-out-points=true` and do a `Refresh Boat Shape` (button).
> - in `index.05.html`, the file `calculated.json` is read directly into a JS variable.

---
