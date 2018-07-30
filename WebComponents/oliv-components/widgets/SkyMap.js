const skyMapVerbose = true;
const SKY_MAP_TAG_NAME = 'sky-map';

/* The map data */
import constellations from "./stars/constellations.js";
// import constellations from "./stars/constellations"; // minifyJs does NOT like the .js extension

/* global HTMLElement */
class SkyMap extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",                  // Integer. Canvas width
			"height"
		];
	}

	dummyDump() {
		console.log('We have %d constellation(s).', constellations.length);
		for (let i=0; i<constellations.length; i++) {
			console.log("- %s: %d star(s)", constellations[i].name, constellations[i].stars.length);
		}
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		this.shadowRoot.appendChild(this.canvas);

		// For tests of the import
		this.dummyDump();

		// Default values
		this._width       = 500;
		this._height      = 500;
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (skyMapVerbose) {
			console.log("connectedCallback invoked");
		}
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (skyMapVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (skyMapVerbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "width":
				this._width = parseInt(newVal);
				break;
			case "height":
				this._height = parseInt(newVal);
				break;
			default:
				break;
		}
		this.repaint();
	}

	// Called whenever the custom element has been moved into a new document.
	adoptedCallback() {
		if (skyMapVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set width(val) {
		this.setAttribute("width", val);
	}
	set height(val) {
		this.setAttribute("height", val);
	}

	set shadowRoot(val) {
		this._shadowRoot = val;
	}

	get width() {
		return this._width;
	}
	get height() {
		return this._height;
	}

	get shadowRoot() {
		return this._shadowRoot;
	}

	/*
	 * Component methods
	 */
	repaint() {
//	this.drawSkyMap();
	}


	toRadians(deg) {
		return deg * (Math.PI / 180);
	}

	toDegrees(rad) {
		return rad * (180 / Math.PI);
	}

}

// Associate the tag and the class
window.customElements.define(SKY_MAP_TAG_NAME, SkyMap);
