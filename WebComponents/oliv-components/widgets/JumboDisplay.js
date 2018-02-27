const jumboVerbose = false;
const JUMBO_TAG_NAME = 'jumbo-display';

const jumboDefaultColorConfig = {
	bgColor: 'white',
	displayBackgroundGradient: {
		from: 'black',
		to: 'gray'
	},
	gridColor: 'rgba(255, 255, 255, 0.7)',
	displayColor: 'cyan',
	valueNbDecimal: 2,
	labelFont: 'Courier New',
	valueFont: 'Arial'
};

/* global HTMLElement */
class JumboDisplay extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",        // Integer. Canvas width
			"height",       // Integer. Canvas height
			"value",        // Float. Numeric value to display
			"label"         // String, like TWS, AWS, etc
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
		this._label       = "VAL";

		this._previousClassName = "";
		this.compassRoseColorConfig = jumboDefaultColorConfig;

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
			case "label":
				this._label = newVal;
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
	set label(val) {
		this.setAttribute("label", val);
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
	get label() {
		return this._label;
	}
	get shadowRoot() {
		return this._shadowRoot;
	}

	// Component methods
	getColorConfig(classNames) {
		let colorConfig = jumboDefaultColorConfig;
		let classes = classNames.split(" ");
		for (let cls=0; cls<classes.length; cls++) {
			let cssClassName = classes[cls];
			for (let s=0; s<document.styleSheets.length; s++) {
				// console.log("Walking though ", document.styleSheets[s]);
				for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
					let selector = document.styleSheets[s].cssRules[r].selectorText;
	//			console.log(">>> ", selector);
					if (selector !== undefined && (selector === '.' + cssClassName || (selector.indexOf('.' + cssClassName) > -1 && selector.indexOf(JUMBO_TAG_NAME) > -1))) { // Cases like "tag-name .className"
	//				console.log("  >>> Found it! [%s]", selector);
						let cssText = document.styleSheets[s].cssRules[r].style.cssText;
						let cssTextElems = cssText.split(";");
						cssTextElems.forEach(function (elem) {
							if (elem.trim().length > 0) {
								let keyValPair = elem.split(":");
								let key = keyValPair[0].trim();
								let value = keyValPair[1].trim();
								switch (key) {
									case '--bg-color':
										colorConfig.bgColor = value;
										break;
									case '--display-background-gradient-from':
										colorConfig.displayBackgroundGradient.from = value;
										break;
									case '--display-background-gradient-to':
										colorConfig.displayBackgroundGradient.to = value;
										break;
									case '--grid-color':
										colorConfig.gridColor = value;
										break;
									case '--display-color':
										colorConfig.displayColor = value;
										break;
									case '--value-nb-decimal':
										colorConfig.valueNbDecimal = value;
										break;
									case '--label-font':
										colorConfig.labelFont = value;
										break;
									case '--value-font':
										colorConfig.valueFont = value;
										break;
									default:
										break;
								}
							}
						});
					}
				}
			}
		}
		return colorConfig;
	};


	repaint() {
		this.drawJumbo(this._value);
	}

	drawJumbo(jumboValue) {

		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
			//	console.log("Reloading CSS");
			try {
				this.compassRoseColorConfig = this.getColorConfig(currentStyle);
			} catch (err) {
				// Absorb?
				console.log(err);
			}

			this._previousClassName = currentStyle;
		}

		let context = this.canvas.getContext('2d');
		let scale = 1.0;

		if (this.width === 0 || this.height === 0) { // Not visible
			return;
		}
		// Set the canvas size from its container.
		this.canvas.width = this.width;
		this.canvas.height = this.height;

		var grd = context.createLinearGradient(0, 5, 0, this.height);
		grd.addColorStop(0, this.compassRoseColorConfig.displayBackgroundGradient.from); // 0  Beginning
		grd.addColorStop(1, this.compassRoseColorConfig.displayBackgroundGradient.to);  // 1  End
		context.fillStyle = grd;

		// Background
		this.roundRect(context, 0, 0, this.canvas.width, this.canvas.height, 10, true, false);
		// Grid
		//  1 - vertical
		var nbVert = 5;
		context.strokeStyle = this.compassRoseColorConfig.gridColor;
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

		context.fillStyle = this.compassRoseColorConfig.displayColor;
		// Label
		context.font = "bold " + Math.round(scale * 16) + "px " + this.compassRoseColorConfig.labelFont;
		context.fillText(this.label, 5, 18);
		// Value
		context.font = "bold " + Math.round(scale * 60) + "px " + this.compassRoseColorConfig.valueFont;
		let strVal = jumboValue.toFixed(this.compassRoseColorConfig.valueNbDecimal);
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
	}
}

// Associate the tag and the class
window.customElements.define(JUMBO_TAG_NAME, JumboDisplay);
