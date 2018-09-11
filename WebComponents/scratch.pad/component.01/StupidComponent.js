const verbose = false;

/**
 * the 'data' member will be added to the shadowRoot, as innerHTML
 */
class StupidComponent extends HTMLElement {

	static get observedAttributes() { return ["data"]; }

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'});
		this.shadowRoot.innerHTML = ''; // <strong style="font-family: Verdana; color: red; padding:5px; border-radius:5px; overflow-y: scroll; border:1px solid #CCC;">Shadow dom super powers for the win!</strong>'; // Overrides whatever is in the tag (see html page).

		this._data = "Default"; // Init
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
		this._data = newVal;
		this.shadowRoot.innerHTML = '<strong>' + this.data + '</strong>';
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

	// Get the "open" property
	get data() {
		return this._data;
//	return this.hasAttribute("data"); // This one returns a boolean!
	}

	set shadowRoot(val) {
		this._shadowRoot = val;
	}

	get shadowRoot() {
		return this._shadowRoot;
	}

}

/* Note:
To enable custom elements and shadow DOM in Firefox, set the
dom.webcomponents.enabled ,
dom.webcomponents.shadowdom.enabled,
and dom.webcomponents.customelements.enabled preferences to true.
Support will be introduced in Firefox 59/60.

To do it, enter in the firefox url field: about:config

Even like that, Firefox 58 does not work well...
 */

// Associate the tag and the class
window.customElements.define('stupid-component', StupidComponent);

/*
Could also be used like this:

window.customElements.define('stupid-component', class extends HTMLElement {
  // Define behaviour here
});

 */
