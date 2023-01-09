const tempVerbose = false;
const THERMOMETER_TAG_NAME = 'thermo-meter';

const thermometerColorConfigDefault = {
	bgColor: 'white',
	digitColor: 'red',
	withGradient: true,
	displayBackgroundGradient: {
		from: 'black',
		to: 'LightGrey'
	},
	withDisplayShadow: true,
	shadowColor: 'rgba(0, 0, 0, 0.75)',
	majorTickColor: 'DarkGrey',
	minorTickColor: 'DarkGrey',
	valueColor: 'LightRed',
	valueOutlineColor: 'black',
	valueNbDecimal: 2,
	font: 'Arial' /* 'Source Code Pro' */
};

/* global HTMLElement */
class Thermometer extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",        // Integer. Canvas width
			"height",       // Integer. Canvas height
			"min-value",    // Float. Min value for temperature
			"max-value",    // Float. Max value for temperature
			"major-ticks",  // Float. value between major ticks (those with labels)
			"minor-ticks",  // Float. value between minor ticks
			"value",        // Float. Temperature to display
			"unit"          // String. C (for Celsius) or F (for Fahrenheit), or any thing. ' ' means no unit.
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		let fallbackElemt = document.createElement("h1");
		let content = document.createTextNode("This is a Thermometer, on an HTML5 canvas");
		fallbackElemt.appendChild(content);
		this.canvas.appendChild(fallbackElemt);
		this.shadowRoot.appendChild(this.canvas);

		// Default values
		this._value       =   0;
		this._width       =  50;
		this._height      = 150;
		this._min_value   =   0;
		this._max_value   =  10;
		this._major_ticks =   1;
		this._minor_ticks =   0.25;
		this._unit        = '';

		this._previousClassName = "";
		this.thermometerColorConfig = thermometerColorConfigDefault; // Init

		if (tempVerbose) {
			console.log("Data in Constructor:", this._value);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (tempVerbose) {
			console.log("connectedCallback invoked, 'value' is [", this.value, "]");
		}
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (tempVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (tempVerbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "value":
				try {
					this._value = parseFloat(newVal);
				} catch (err) {
					// NaN ?
				}
				break;
			case "width":
				this._width = parseInt(newVal);
				break;
			case "height":
				this._height = parseInt(newVal);
				break;
			case "min-value":
				this._min_value = parseFloat(newVal);
				break;
			case "max-value":
				this._max_value = parseFloat(newVal);
				break;
			case "major-ticks":
				this._major_ticks = parseFloat(newVal);
				break;
			case "minor-ticks":
				this._minor_ticks = parseFloat(newVal);
				break;
			case "unit":
				this._unit = newVal;
				break;
			default:
				break;
		}
		this.repaint();
	}

	// Called whenever the custom element has been moved into a new document.
	adoptedCallback() {
		if (tempVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set value(option) {
		this.setAttribute("value", option);
		if (tempVerbose) {
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
	set minValue(val) {
		this.setAttribute("min-value", val);
	}
	set maxValue(val) {
		this.setAttribute("max-value", val);
	}
	set majorTicks(val) {
		this.setAttribute("major-ticks", val);
	}
	set minorTicks(val) {
		this.setAttribute("minor-ticks", val);
	}
	set unit(val) {
		this.setAttribute("unit", val);
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
	get minValue() {
		return this._min_value;
	}
	get maxValue() {
		return this._max_value;
	}
	get minorTicks() {
		return this._minor_ticks;
	}
	get majorTicks() {
		return this._major_ticks;
	}
	get unit() {
		return this._unit;
	}

	get shadowRoot() {
		return this._shadowRoot;
	}

	// Component methods
	/**
	 * Recurse from the top down, on styleSheets and cssRules
	 *
	 * document.styleSheets[0].cssRules[2].selectorText returns ".analogdisplay"
	 * document.styleSheets[0].cssRules[2].cssText returns ".analogdisplay { --hand-color: red;  --face-color: white; }"
	 * document.styleSheets[0].cssRules[2].style.cssText returns "--hand-color: red; --face-color: white;"
	 *
	 * spine-case to camelCase
	 */
	getColorConfig(cssClassNames) {
		let colorConfig = thermometerColorConfigDefault;
		let classes = cssClassNames.split(" ");
		for (let cls=0; cls<classes.length; cls++) {
			let cssClassName = classes[cls];
			for (let s=0; s<document.styleSheets.length; s++) {
	      // console.log("Walking though ", document.styleSheets[s]);
				try {
					for (let r=0; document.styleSheets[s].cssRules !== null && r<document.styleSheets[s].cssRules.length; r++) {
						let selector = document.styleSheets[s].cssRules[r].selectorText;
						//			console.log(">>> ", selector);
						if (selector !== undefined && (selector === '.' + cssClassName || (selector.indexOf('.' + cssClassName) > -1 && selector.indexOf(THERMOMETER_TAG_NAME) > -1))) { // Cases like "tag-name .className"
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
										case '--digit-color':
											colorConfig.digitColor = value;
											break;
										case '--with-gradient':
											colorConfig.withGradient = (value === 'true');
											break;
										case '--display-background-gradient-from':
											colorConfig.displayBackgroundGradient.from = value;
											break;
										case '--display-background-gradient-to':
											colorConfig.displayBackgroundGradient.to = value;
											break;
										case '--with-display-shadow':
											colorConfig.withDisplayShadow = (value === 'true');
											break;
										case '--shadow-color':
											colorConfig.shadowColor = value;
											break;
										case '--outline-color':
											colorConfig.outlineColor = value;
											break;
										case '--major-tick-color':
											colorConfig.majorTickColor = value;
											break;
										case '--minor-tick-color':
											colorConfig.minorTickColor = value;
											break;
										case '--value-color':
											colorConfig.valueColor = value;
											break;
										case '--value-outline-color':
											colorConfig.valueOutlineColor = value;
											break;
										case '--value-nb-decimal':
											colorConfig.valueNbDecimal = value;
											break;
										case '--font':
											colorConfig.font = value;
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
		this.drawThermometer(this._value);
	}

	drawThermometer(tempValue) {

		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
	//	console.log("Reloading CSS");
			try {
				this.thermometerColorConfig = this.getColorConfig(currentStyle);
			} catch (err) {
				// Absorb?
				console.log(err);
			}

			this._previousClassName = currentStyle;
		}

		let digitColor = this.thermometerColorConfig.digitColor;
		let context = this.canvas.getContext('2d');

		if (this.width === 0 || this.height === 0) { // Not visible
			return;
		}
		let radius = 10; // The ball at the bottom. The tube is (radius / 2) wide.

		// Set the canvas size from its container.
		this.canvas.width = this.width;
		this.canvas.height = this.height;

		// Cleanup
		//context.fillStyle = "#ffffff";
		context.fillStyle = this.thermometerColorConfig.bgColor;
		//context.fillStyle = "transparent";
		context.fillRect(0, 0, this.canvas.width, this.canvas.height);
		//context.fillStyle = 'rgba(255, 255, 255, 0.0)';
		//context.fillRect(0, 0, canvas.width, canvas.height);

		// Bottom of the tube at (canvas.height - 10 - radius)
		let bottomTube = (this.canvas.height - 10 - radius);
		let topTube = 40;// Top of the tube at y = 20

		let tubeLength = bottomTube - topTube;

		// Major Ticks
		context.beginPath();
		for (let i = 0; i <= (this.maxValue -this.minValue); i += this.majorTicks) {
			let xFrom = (this.canvas.width / 2) - 20;
			let yFrom = bottomTube - ((tubeLength) * (i / (this.maxValue - this.minValue)));
			let xTo = (this.canvas.width / 2) + 20;
			let yTo = yFrom;
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
		}
		context.lineWidth = 1;
		context.strokeStyle = this.thermometerColorConfig.majorTickColor;
		context.stroke();
		context.closePath();

		// Minor Ticks
		if (this.minorTicks > 0) {
			context.beginPath();
			for (let i = 0; i <= (this.maxValue - this.minValue); i += this.minorTicks) {
				let xFrom = (this.canvas.width / 2) - 15;
				let yFrom = bottomTube - ((tubeLength) * (i / (this.maxValue - this.minValue)));
				let xTo = (this.canvas.width / 2) + 15;
				let yTo = yFrom;
				context.moveTo(xFrom, yFrom);
				context.lineTo(xTo, yTo);
			}
			context.lineWidth = 1;
			context.strokeStyle = this.thermometerColorConfig.minorTickColor;
			context.stroke();
			context.closePath();
		}

		// Tube
		context.beginPath();
		//context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);
		context.arc(this.canvas.width / 2, this.canvas.height - 10 - radius, radius, 5 * Math.PI / 4, 7 * Math.PI / 4, true);
		context.lineTo((this.canvas.width / 2) + (radius * Math.cos(Math.PI / 4)), topTube); // right side of the tube
		context.arc(this.canvas.width / 2, topTube, (radius / 2), 0, Math.PI, true);
		context.lineWidth = 1;

		if (this.thermometerColorConfig.withGradient) {
			let grd = context.createLinearGradient(0, 5, 0, radius);
			grd.addColorStop(0, this.thermometerColorConfig.displayBackgroundGradient.from);// 0  Beginning
			grd.addColorStop(1, this.thermometerColorConfig.displayBackgroundGradient.to);// 1  End
			context.fillStyle = grd;
		} else {
			context.fillStyle = this.thermometerColorConfig.displayBackgroundGradient.to;
		}
		if (this.thermometerColorConfig.withDisplayShadow) {
			context.shadowBlur = 0;
			context.shadowColor = this.thermometerColorConfig.shadowColor; // 'black';
		}
		context.lineJoin = "round";
		context.fill();
		context.strokeStyle = 'DarkGrey';
		context.stroke();
		context.closePath();

		// Numbers
		context.beginPath();
		for (let i = this.minValue; i <= this.maxValue; i += this.majorTicks) {
			let xTo = (this.canvas.width / 2) + 20;
			let yTo = bottomTube - ((tubeLength) * ((i - this.minValue) / (this.maxValue - this.minValue)));

			context.font = "bold 10px Arial";
			context.fillStyle = digitColor;
			let str = i.toString();
//		len = context.measureText(str).width;
			context.fillText(str, xTo, yTo + 3); // 5: half font size
		}
		context.closePath();

		// Value
		// console.log(`tempValue: ${tempValue}`);
		if (!isNaN(tempValue)) {
			let text = tempValue.toFixed(this.thermometerColorConfig.valueNbDecimal) + (this.unit.trim().length > 0 ? `°${this.unit}` : '' );
			context.font = "bold 20px Arial";
			let metrics = context.measureText(text);
			let len = metrics.width;

			context.beginPath();
			context.fillStyle = this.thermometerColorConfig.valueColor;
			context.fillText(text, (this.canvas.width / 2) - (len / 2), ((radius * .75) + 10));
			context.lineWidth = 1;
			context.strokeStyle = this.thermometerColorConfig.valueOutlineColor;
			context.strokeText(text, (this.canvas.width / 2) - (len / 2), ((radius * .75) + 10)); // Outlined
			context.closePath();

			// Liquid in the tube
			let valInBoundaries = Math.min(tempValue, this._max_value);
			valInBoundaries = Math.max(valInBoundaries, this._min_value);
			context.beginPath();
			//context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);
			context.arc(this.canvas.width / 2, this.canvas.height - 10 - (radius * 0.75), (radius * 0.75), 5 * Math.PI / 4, 7 * Math.PI / 4, true);
			let y = bottomTube - ((tubeLength) * ((valInBoundaries - this.minValue) / (this.maxValue - this.minValue)));

			context.lineTo((this.canvas.width / 2) + ((radius * 0.75) * Math.cos(Math.PI / 4)), y); // right side of the tube
			context.lineTo((this.canvas.width / 2) - ((radius * 0.75) * Math.cos(Math.PI / 4)), y); // top of the liquid

			context.lineWidth = 1;

			let _grd = context.createLinearGradient(0, topTube, 0, tubeLength);
			_grd.addColorStop(0, 'red');    // 0  Beginning
			_grd.addColorStop(0.6, 'red');
			_grd.addColorStop(0.8, 'blue');
			_grd.addColorStop(1, 'navy');   // 1  End
			context.fillStyle = _grd;

			context.shadowBlur = 20;
			context.shadowColor = 'black';

			context.lineJoin = "round";
			context.fill();
			context.strokeStyle = 'DarkGrey';
			context.stroke();
			context.closePath();
		}
	}
}

// Associate the tag and the class
window.customElements.define(THERMOMETER_TAG_NAME, Thermometer);
