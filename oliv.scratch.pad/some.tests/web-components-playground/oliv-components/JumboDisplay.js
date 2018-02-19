const jumboVerbose = false;

/* global HTMLElement */
class JumboDisplay extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",        // Integer. Canvas width
			"height",       // Integer. Canvas height
			"value",        // Float. Rain amount to display
			"title",        // String, like TWS, AWS, etc
			"text-color"    // Color. TODO Move to CSS style?
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		this.shadowRoot.appendChild(this.canvas);

		// Default values
		this._value       =   0;
		this._width       =  50;
		this._height      = 150;
		this._title       = "VAL";
		this._color       = 'white';

		if (jumboVerbose) {
			console.log("Data in Constructor:", this._value);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (jumboVerbose) {
			console.log("connectedCallback invoked, 'value' is [", this.value, "]");
		}
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (jumboVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (jumboVerbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "value":
				this._value = parseFloat(newVal);
				break;
			case "width":
				this._width = parseInt(newVal);
				break;
			case "height":
				this._height = parseInt(newVal);
				break;
			case "title":
				this._title = newVal;
				break;
			case "text-color":
				this._color = newVal;
				break;
			default:
				break;
		}
		this.repaint();
	}

	// Called whenever the custom element has been moved into a new document.
	adoptedCallback() {
		if (jumboVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set value(option) {
		this.setAttribute("value", option);
		if (jumboVerbose) {
			console.log(">> Value option:", option);
		}
//	this.repaint();
	}
	set width(val) {
		this.setAttribute("width", val);
	}
	set height(val) {
		this.setAttribute("height", val);
	}
	set title(val) {
		this.setAttribute("title", val);
	}
	set color(val) {
		this.setAttribute("text-color", val);
	}
	set shadowRoot(val) {
		this._shadowRoot = val;
	}

	get value() {
		return this._value;
	}
	get width() {
		return this._width;
	}
	get height() {
		return this._height;
	}
	get title() {
		return this._title;
	}
	get color() {
		return this._color;
	}

	get shadowRoot() {
		return this._shadowRoot;
	}

	// Component methods
	repaint() {
		this.drawJumbo(this._value);
	}

	drawJumbo(jumboValue) {

		let context = this.canvas.getContext('2d');
		let scale = 1.0;

		if (this.width === 0 || this.height === 0) { // Not visible
			return;
		}
		// Set the canvas size from its container.
		this.canvas.width = this.width;
		this.canvas.height = this.height;

		var grd = context.createLinearGradient(0, 5, 0, this.height);
		grd.addColorStop(0, 'black'); // 0  Beginning
		grd.addColorStop(1, 'gray');  // 1  End
		context.fillStyle = grd;

		// Background
		this.roundRect(context, 0, 0, this.canvas.width, this.canvas.height, 10, true, false);
		// Grid
		//  1 - vertical
		var nbVert = 5;
		context.strokeStyle = 'rgba(255, 255, 255, 0.7)';
		context.lineWidth = 0.5;
		for (var i = 1; i < nbVert; i++) {
			var x = i * (this.canvas.width / nbVert);
			context.beginPath();
			context.moveTo(x, 0);
			context.lineTo(x, this.canvas.height);
			context.closePath();
			context.stroke();
		}
		// 2 - Horizontal
		var nbHor = 3;
		for (var i = 1; i < nbHor; i++) {
			var y = i * (this.canvas.height / nbHor);
			context.beginPath();
			context.moveTo(0, y);
			context.lineTo(this.canvas.width, y);
			context.closePath();
			context.stroke();
		}

		context.fillStyle = this.color;
		// Title
		context.font = "bold " + Math.round(scale * 16) + "px Courier New"; // "bold 16px Arial"
		context.fillText(this.title, 5, 18);
		// Value
		context.font = "bold " + Math.round(scale * 60) + "px Arial";
		let strVal = jumboValue.toFixed(2);
		let metrics = context.measureText(strVal);
		let len = metrics.width;

		context.fillText(strVal, this.canvas.width - len - 5, this.canvas.height - 5);
	}

	roundRect(ctx, x, y, width, height, radius, fill, stroke) {
		if (fill === undefined) {
			fill = true;
		}
		if (stroke === undefined) {
			stroke = true;
		}
		if (radius === undefined) {
			radius = 5;
		}
		ctx.beginPath();
		ctx.moveTo(x + radius, y);
		ctx.lineTo(x + width - radius, y);
		ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
		ctx.lineTo(x + width, y + height - radius);
		ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
		ctx.lineTo(x + radius, y + height);
		ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
		ctx.lineTo(x, y + radius);
		ctx.quadraticCurveTo(x, y, x + radius, y);
		ctx.closePath();
		if (stroke) {
			ctx.stroke();
		}
		if (fill) {
			ctx.fill();
		}
	};
}

// Associate the tag and the class
window.customElements.define('jumbo-display', JumboDisplay);
