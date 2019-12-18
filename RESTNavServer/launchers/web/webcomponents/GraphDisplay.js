/**
 * This is the graphDisplay Web Component.
 */

const graphDisplayVerbose = false;
const GRAPH_TAG_NAME = 'graph-display';

const graphDisplayDefaultColorConfig = {
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
class GraphDisplay extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",  // Integer. Canvas width
			"height", // Integer. Canvas height
			"value",  // String. Numeric (or so) value to display (null by default)
			"label",  // String, like TWS, AWS, etc
			"data"    // Curve(s) data
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		let fallbackElemt = document.createElement("h1");
		let content = document.createTextNode("This is a WebComponent GraphDisplay, on an HTML5 canvas");
		fallbackElemt.appendChild(content);
		this.canvas.appendChild(fallbackElemt);
		this.shadowRoot.appendChild(this.canvas);

		// Default values
		this._value = null;
		this._width = 250;
		this._height = 100;
		this._label = "VAL";
		this._data = null;

		this._previousClassName = "";
		this.graphDisplayColorConfig = graphDisplayDefaultColorConfig;

		if (graphDisplayVerbose) {
			console.log("Data in Constructor:", this._value);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (graphDisplayVerbose) {
			console.log("connectedCallback invoked, 'value' is [", this.value, "]");
		}
		this.repaint();
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (graphDisplayVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (graphDisplayVerbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "value":
				this._value = (newVal === 'null' ? null : newVal); // parseFloat(newVal));
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
			case "data":
				this._data = JSON.parse(newVal);
				break;
			default:
				break;
		}
		this.repaint();
	}

	// Called whenever the custom element has been moved into a new document.
	adoptedCallback() {
		if (graphDisplayVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set value(option) {
		this.setAttribute("value", option);
		if (graphDisplayVerbose) {
			console.log(">> Value option:", option);
		}
//	this.repaint(); // Done in attributeChangedCallback
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

	set data(val) {
		this._data = val;
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

	get data() {
		return this._data;
	}

	get shadowRoot() {
		return this._shadowRoot;
	}

	// Component methods
	getColorConfig(classNames) {
		let colorConfig = graphDisplayDefaultColorConfig;
		let classes = classNames.split(" ");
		for (let cls = 0; cls < classes.length; cls++) {
			let cssClassName = classes[cls];
			for (let s = 0; s < document.styleSheets.length; s++) {
				// console.log("Walking though ", document.styleSheets[s]);
				try {
					for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
						let selector = document.styleSheets[s].cssRules[r].selectorText;
						//			console.log(">>> ", selector);
						if (selector !== undefined && (selector === '.' + cssClassName || (selector.indexOf('.' + cssClassName) > -1 && selector.indexOf(GRAPH_TAG_NAME) > -1))) { // Cases like "tag-name .className"
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
		this.drawGraph();
	}

	drawGraph() {

		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
			//	console.log("Reloading CSS");
			try {
				this.graphDisplayColorConfig = this.getColorConfig(currentStyle);
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
		grd.addColorStop(0, this.graphDisplayColorConfig.displayBackgroundGradient.from); // 0  Beginning
		grd.addColorStop(1, this.graphDisplayColorConfig.displayBackgroundGradient.to); // 1  End
		context.fillStyle = grd;

		// Background
		GraphDisplay.roundRect(context, 0, 0, this.canvas.width, this.canvas.height, 10, true, false);

		context.fillStyle = this.graphDisplayColorConfig.displayColor;
		// Label
		context.font = "bold " + Math.round(scale * 16) + "px " + this.graphDisplayColorConfig.labelFont;
		context.fillText(this.label, 5, 18);
		// Value
		if (this._value !== null) {
			context.font = "bold " + Math.round(scale * 30) + "px " + this.graphDisplayColorConfig.valueFont;
			let strVal = this._value; // .toFixed(this.graphDisplayColorConfig.valueNbDecimal);
			let metrics = context.measureText(strVal);
			let len = metrics.width;

			context.fillText(strVal, this.canvas.width - len - 5, this.canvas.height - 5);
		}

		// Curves
		if (this._data !== null) {
			let minX = this._data.minX;
			let maxX = this._data.maxX;
			// console.log(`From ${minX} to ${maxX}`);
			let xAmpl = maxX - minX;
			let yAmpl = this._data.maxY - this._data.minY;
			let xRatio = this._width /  xAmpl;
			let xOffset = minX;
			let yRatio = this._height / yAmpl;
			let yOffset = this._data.minY;
			for (let i=0; i<this._data.data.length; i++) {
				let curve = this._data.data[i];
				if (curve.values.length !== curve.x.length) {
					console.log(`Cardinality mismatch at index ${i}: ${curve.x.length} x, ${curve.values.length} y`);
					continue;
				}
				// Curve
				context.beginPath();
				let _x = (curve.x[0] - xOffset) * xRatio;
				let _y = this._height - ((curve.values[0] - yOffset) * yRatio);
				if (graphDisplayVerbose) {
					console.log(`Moving to ${_x} / ${_y}`);
				}
				context.moveTo(_x, _y);
				for (let x=1; x<curve.x.length; x++) {
					_x =(curve.x[x] - xOffset) * xRatio;
					_y = this._height - ((curve.values[x] - yOffset) * yRatio);
					if (graphDisplayVerbose) {
						console.log(`Lining to ${_x} / ${_y}`);
					}
					context.lineTo(_x, _y);
				}
				context.lineWidth = curve.thickness;
				context.strokeStyle = curve.lineColor;
				context.stroke();
				context.closePath();
			}
			// Grid?

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
window.customElements.define(GRAPH_TAG_NAME, GraphDisplay);
