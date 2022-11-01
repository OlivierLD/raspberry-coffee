const jumboPlusVerbose = false;
const JUMBO_PLUS_TAG_NAME = 'jumbo-plus-display';

const jumboDefaultColorConfig = {
	bgColor: 'white',
	displayBackgroundGradient: {
		from: 'black',
		to: 'gray'
	},
	gridColor: 'rgba(255, 255, 255, 0.7)',
	displayColor: 'cyan',
	labelFont: 'Courier New',
	valueFont: 'Arial'
};

/* global HTMLElement */
class JumboPlusDisplay extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",  // Integer. Canvas width
			"height", // Integer. Canvas height
			"value",  // String. Value to display. Split lines on '|'
			"label"   // String, like TWS, AWS, etc
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		let fallbackElemt = document.createElement("h1");
		let content = document.createTextNode("This is a JumboPlusDisplay, on an HTML5 canvas");
		fallbackElemt.appendChild(content);
		this.canvas.appendChild(fallbackElemt);
		this.shadowRoot.appendChild(this.canvas);

		// Default values
		this._value = "-"; // Split on '|'
		this._width = 50;
		this._height = 150;
		this._label = "VAL";

		this._previousClassName = "";
		this.jumboColorConfig = jumboDefaultColorConfig;

		if (jumboPlusVerbose) {
			console.log("Data in Constructor:", this._value);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (jumboPlusVerbose) {
			console.log("connectedCallback invoked, 'value' is [", this.value, "]");
		}
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (jumboPlusVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (jumboPlusVerbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "value":
				this._value = newVal;
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
		if (jumboPlusVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set value(option) {
		this.setAttribute("value", option);
		if (jumboPlusVerbose) {
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
		for (let cls = 0; cls < classes.length; cls++) {
			let cssClassName = classes[cls];
			for (let s = 0; s < document.styleSheets.length; s++) {
				// console.log("Walking though ", document.styleSheets[s]);
				try {
					for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
						let selector = document.styleSheets[s].cssRules[r].selectorText;
						//			console.log(">>> ", selector);
						if (selector !== undefined && (selector === '.' + cssClassName || (selector.indexOf('.' + cssClassName) > -1 && selector.indexOf(JUMBO_PLUS_TAG_NAME) > -1))) { // Cases like "tag-name .className"
							//				console.log("  >>> Found it! [%s]", selector);
							let cssText = document.styleSheets[s].cssRules[r].style.cssText;
							let cssTextElems = cssText.split(";");
							cssTextElems.forEach((elem) => {
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
				} catch (err) {
					// Absorb
				}
			}
		}
		return colorConfig;
	}

	repaint() {
		this.drawJumbo(this._value);
	}

	drawJumbo(jumboValue) {

		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
			//	console.log("Reloading CSS");
			try {
				this.jumboColorConfig = this.getColorConfig(currentStyle);
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

		let grd = context.createLinearGradient(0, 5, 0, this.height);
		grd.addColorStop(0, this.jumboColorConfig.displayBackgroundGradient.from); // 0  Beginning
		grd.addColorStop(1, this.jumboColorConfig.displayBackgroundGradient.to); // 1  End
		context.fillStyle = grd;

		// Background
		JumboPlusDisplay.roundRect(context, 0, 0, this.canvas.width, this.canvas.height, 10, true, false);
		// Grid
		//  1 - vertical
		let nbVert = 5;
		context.strokeStyle = this.jumboColorConfig.gridColor;
		context.lineWidth = 0.5;
		for (let i = 1; i < nbVert; i++) {
			let x = i * (this.canvas.width / nbVert);
			context.beginPath();
			context.moveTo(x, 0);
			context.lineTo(x, this.canvas.height);
			context.closePath();
			context.stroke();
		}
		// 2 - Horizontal
		let nbHor = 3;
		for (let i = 1; i < nbHor; i++) {
			let y = i * (this.canvas.height / nbHor);
			context.beginPath();
			context.moveTo(0, y);
			context.lineTo(this.canvas.width, y);
			context.closePath();
			context.stroke();
		}

		context.fillStyle = this.jumboColorConfig.displayColor;
		// Label
		context.font = "bold " + Math.round(scale * 16) + "px " + this.jumboColorConfig.labelFont;
		context.fillText(this.label, 5, 18);
		// Value
		context.font = "bold " + Math.round(scale * 60) + "px " + this.jumboColorConfig.valueFont;
		let strVal = jumboValue.split("|");
		for (let i=0; i<strVal.length; i++) {
			let metrics = context.measureText(strVal[i]);
			let len = metrics.width;
			let heightOffset = Math.round(scale * 60) * (strVal.length - i - 1);
			context.fillText(strVal[i], this.canvas.width - len - 5, this.canvas.height - heightOffset - 5);
		}
	}

	static roundRect(ctx, x, y, width, height, radius, fill, stroke) {
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
window.customElements.define(JUMBO_PLUS_TAG_NAME, JumboPlusDisplay);
