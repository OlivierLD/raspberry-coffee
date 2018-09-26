const verbose = false;

/* global HTMLElement */

class BasicCanvas extends HTMLElement {

	static get observedAttributes() { return ["data", "width", "height"]; }

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		this.canvas = document.createElement("canvas"); // create and append a <canvas>
		this.shadowRoot.appendChild(this.canvas);

		this._data = "Default"; // Init
		this._width = 50;
		this._height = 50;
		if (verbose) {
			console.log("Data in Constructor:", this._data);
		}

		this.addEventListener("click", e => {
			console.log('Click on ', this.data);
		});
	}

	connectedCallback() { // Called whenever the custom element is inserted into the DOM.
		if (verbose) {
			console.log("connectedCallback invoked, 'data' value is [", this.data, "]");
		}
	}

	disconnectedCallback() { // Called whenever the custom element is removed from the DOM.
		if (verbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	attributeChangedCallback(attrName, oldVal, newVal) { // Called whenever an attribute is added, removed or updated. Only attributes listed in the observedAttributes property are affected.
		if (verbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "data":
				this._data = newVal;
				break;
			case "width":
				this._width = newVal;
				break;
			case "height":
				this._height = newVal;
				break;
			default:
				break;
		}
		this.paint();
	}

	adoptedCallback() { // Called whenever the custom element has been moved into a new document.
		if (verbose) {
			console.log("adoptedCallback invoked");
		}
	}

	// Set the "data" property
	set data(option) {
		this.setAttribute("data", option);
		if (verbose) {
			console.log(">> Data option:", option);
		}
	}

	// Get the "data" property
	get data() {
		return this._data;
//	return this.hasAttribute("data"); // This one returns a boolean!
	}

	set width(val) {
		this.setAttribute("width", val);
	}

	get width() {
		return this._width;
	}

	set height(val) {
		this.setAttribute("height", val);
	}

	get height() {
		return this._height;
	}

	set shadowRoot(val) {
		this._shadowRoot = val;
	}

	get shadowRoot() {
		return this._shadowRoot;
	}

	// Component methods
	paint() {
		let context = this.canvas.getContext('2d');

		if (this.width === 0 || this.height === 0) { // Not visible
			return;
		}
		// Set the canvas size from its container.
		this.canvas.width = this.width;
		this.canvas.height = this.height;

		// Cleanup
		context.fillStyle = "transparent";
		context.fillRect(0, 0, this.width, this.height);

		// Circle
		context.strokeStyle = "red";
		context.arc(this.width / 2, this.height / 2, 18, 0, 2 * Math.PI, false);
		context.stroke();

		// Text
		context.font = "bold 10px Arial";
		context.fillStyle = "blue";
		let str = this.data;
		let len = context.measureText(str).width;
		context.fillText(str, (this.width / 2) - (len / 2), (this.height / 2) + (10 / 2));
	}
}

/* Note:
To enable custom elements and shadow DOM in Firefox, set the
dom.webcomponents.enabled ,
dom.webcomponents.shadowdom.enabled,
and dom.webcomponents.customelements.enabled preferences to true.
Support will be introduced in Firefox 59/60.

To do it, enter in the firefox url field: about:config

Even like that, Firefox 58 sometimes does not work well (as expected)...
FF 59 to be released on 2018-03-13 (https://wiki.mozilla.org/RapidRelease/Calendar)
 */

// Associate the tag and the class
window.customElements.define('basic-canvas', BasicCanvas);

/*
Could also be used like this:

window.customElements.define('basic-canvas', class extends HTMLElement {
  // Define behaviour here
});

 */
