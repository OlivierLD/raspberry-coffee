## Bits and Pieces for WebComponents

- [W3C Reference](http://w3c.github.io/webcomponents/spec/custom/)
- [Shadow DOM Spec](https://w3c.github.io/webcomponents/spec/shadow/)
- [HTML imports](http://w3c.github.io/webcomponents/spec/imports/)
- [HTML templates](https://html.spec.whatwg.org/multipage/webappapis.html)
- [Tutorial](https://auth0.com/blog/web-components-how-to-craft-your-own-custom-components/)

---

I use nodeJS to run the pages:
 ```bash
 $ node server.js
```

Then load the pages in a browser, using for example [http://localhost:8080/component.01/index.html](http://localhost:8080/component.01/index.html).

---

The `WebComponents` standard allows the definition of custom reusable visual components, used in a web page like any standard component.

Example:
```html
<pluvio-meter id="pluviometer-01"
              title="m/m per hour"
              min-value="0"
              max-value="10"
              major-ticks="1"
              minor-ticks="0.25"
              value="0"
              width="60"
              height="220"></pluvio-meter>
```
In addition, they may be accessed from JavaScript:
```javascript
function setData() {
  let elem = document.getElementById("pluviometer-01");
  let value = document.getElementById("rain-value").value;
  elem.value = value;
}
```
This means that their properties can be dynamically modified once the component is loaded.

A `WebComponent` extends `HTMLElement`; as such all the properties of an `HTMLElement` are available on a `WebComponent`
 (such as - see above - `class`, `title`, etc). _Not_ overriding those properties is probably a good idea.
 
---

## Lessons learned

No animation _**in**_ the component, `clearInterval` does not clear anything.
If animation is needed, make it happen outside the component.
Works OK (~so-so) if there is only one component, it's a mess otherwise.

---

## Questions to address
- Observed Attributes. Does a "final" (remaining as-is after being loaded) attribute have to be observed?
- Extending a WebComponent?

---

### Good to go

[Oliv's Components](https://github.com/OlivierLD/raspberry-coffee/tree/master/oliv.scratch.pad/some.tests/web-components-playground/oliv-components)

--- 

More to come...
