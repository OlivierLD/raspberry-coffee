const dir16Verbose = false;
const DIR_16_TAG_NAME = 'dir-16-points';
/*
* See custom properties in CSS.
* =============================
* @see https://developer.mozilla.org/en-US/docs/Web/CSS/
* Relies on those elements:
*
.xxxxxxxxx {
	--bg-color: rgba(0, 0, 0, 0);
	--with-gradient: true;
	--display-background-gradient-from: LightGrey;
	--display-background-gradient-to: white;
	--value-color: cyan;
	--font: Arial;
}
*/

/**
 * Recurse from the top down, on styleSheets and cssRules
 *
 * document.styleSheets[0].cssRules[2].selectorText returns ".analogdisplay"
 * document.styleSheets[0].cssRules[2].cssText returns ".analogdisplay { --hand-color: red;  --face-color: white; }"
 * document.styleSheets[0].cssRules[2].style.cssText returns "--hand-color: red; --face-color: white;"
 *
 * spine-case to camelCase
 */
const default16PointsColorConfig = {
	bgColor: 'rgba(0, 0, 0, 0)', /* transparent, 'white', */
	valueColor: 'cyan',
	withGradient: true,
	displayBackgroundGradient: {
		from: 'LightGrey',
		to: 'white'
	},
	font: 'Arial' /* 'Source Code Pro' */
};

const cardValues = [
	{ name: 'N', value: 0 },
	{ name: 'NNE', value: 22.5 },
	{ name: 'NE', value: 45 },
	{ name: 'ENE', value: 67.5 },
	{ name: 'E', value: 90 },
	{ name: 'ESE', value: 112.5 },
	{ name: 'SE', value: 135 },
	{ name: 'SSE', value: 157.5 },
	{ name: 'S', value: 180 },
	{ name: 'SSW', value: 202.5 },
	{ name: 'SW', value: 225 },
	{ name: 'WSW', value: 247.5 },
	{ name: 'W', value: 270 },
	{ name: 'WNW', value: 292.5 },
	{ name: 'NW', value: 315 },
	{ name: 'NNW', value: 337.5 }
];

const FONT_SIZE = 60;

/* global HTMLElement */
class Raw16PointsDir extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",        // Integer. Canvas width
			"height",       // Integer. Canvas height
			"value"         // Object like { name: 'NNE', value: 22.5 }
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		this.shadowRoot.appendChild(this.canvas);

		// Default values
		this._value       = { name: 'N', value: 0 };
		this._width       = 150;
		this._height      = 150;

		this.scale = Math.min(this._width, this._height) / 150;

		this._previousClassName = "";
		this.colorConfig = default16PointsColorConfig; // Init

		if (dir16Verbose) {
			console.log("Data in Constructor:", this._value);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (dir16Verbose) {
			console.log("connectedCallback invoked, 'value' is [", this.value, "]");
		}
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (dir16Verbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (dir16Verbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "value":
				for (var i=0; i<cardValues.length; i++) {
					if (cardValues[i].name === newVal) {
						this._value = cardValues[i];
						break;
					}
				}
				break;
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
		if (dir16Verbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set value(option) {
		this.setAttribute("value", option);
		if (dir16Verbose) {
			console.log(">> Value option:", option);
		}
//	this.repaint();
	}
	set width(val) {
		this.setAttribute("width", val);
		this.scale = Math.min(this._width, this._height) / 150;
	}
	set height(val) {
		this.setAttribute("height", val);
		this.scale = Math.min(this._width, this._height) / 150;
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
	get shadowRoot() {
		return this._shadowRoot;
	}

	// Component methods
	repaint() {
		this.drawDisplay(this._value);
	}

	getColorConfig(classNames) {
		let colorConfig = default16PointsColorConfig;
		let classes = classNames.split(" ");
		for (let cls=0; cls<classes.length; cls++) {
			let className = classes[cls];
			for (let s=0; s<document.styleSheets.length; s++) {
	//		console.log("Walking though ", document.styleSheets[s]);
				for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
					let selector = document.styleSheets[s].cssRules[r].selectorText;
	//			console.log(">>> ", selector);
					if (selector !== undefined && (selector === '.' + className || (selector.indexOf('.' + className) > -1 && selector.indexOf(DIR_16_TAG_NAME) > -1))) { // Cases like "tag-name .className"
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
									case '--value-color':
										colorConfig.valueColor = value;
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
			}
		}
		return colorConfig;
	}

	/**
	 *
	 * @param dirValue, format { name: 'NNE', value: 22.5 }
	 */
	drawDisplay(dirValue) {

		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
			//	console.log("Reloading CSS");
			try {
				this.colorConfig = this.getColorConfig(currentStyle);
			} catch (err) {
				// Absorb?
				console.log(err);
			}
			this._previousClassName = currentStyle;
		}

		let valueColor = this.colorConfig.valueColor;

		let context = this.canvas.getContext('2d');
		context.clearRect(0, 0, this.width, this.height);

		let radius = (Math.min(this.width, this.height) / 2) * 0.90;

		// Set the canvas size from its container.
		this.canvas.width = this.width;
		this.canvas.height = this.height;

		// Cleanup
		context.fillStyle = this.colorConfig.bgColor;
		context.fillRect(0, 0, this.canvas.width, this.canvas.height);

		context.beginPath();
		if (this.colorConfig.withGradient) {
			let grd = context.createLinearGradient(0, 5, 0, radius);
			grd.addColorStop(0, this.colorConfig.displayBackgroundGradient.from);// 0  Beginning
			grd.addColorStop(1, this.colorConfig.displayBackgroundGradient.to);  // 1  End
			context.fillStyle = grd;
			context.fillRect(0, 0, this.canvas.width, this.canvas.height);
		}

		// Value
		let text = dirValue.name;
		let fontSize = FONT_SIZE;
		let len = 0;
		context.font = Math.round(this.scale * fontSize) + "px " + this.colorConfig.font; // "40px Arial"
		let metrics = context.measureText(text);
		len = metrics.width;

		context.beginPath();
		context.fillStyle = valueColor;
		context.fillText(text, (this.width / 2) - (len / 2), (this.height / 2) + ((fontSize * this.scale) / 2));
		context.closePath();

		// LEDs
		for (var led=0; led<cardValues.length; led++) {
			let angle = this.toRadians(cardValues[led].value + 90);
			// Led centrer
			let xLedCenter = (this.width / 2) - ((radius * 0.95) * Math.cos(angle));
			let yLedCenter = (this.height / 2) - ((radius * 0.95) * Math.sin(angle));
			let color = (dirValue.value === cardValues[led].value ? 'red' : 'gray');
			this.fillLed(context, { x: xLedCenter, y: yLedCenter }, 6, color);
		}
	}

	fillLed(context, pt, radius, color) {
		// let grd = context.createRadialGradient(pt.x - (radius / 3), pt.y - (radius / 3), radius / 3, pt.x, pt.y, radius);
		// grd.addColorStop(0, this.marqueeColorConfig.fgColor.from);
		// grd.addColorStop(1, this.marqueeColorConfig.fgColor.to);

		context.beginPath();
		context.fillStyle = color;
//	context.arc(pt.x - (radius / 2), pt.y - (radius / 2), radius, 0, radius * Math.PI);
		context.arc(pt.x, pt.y, radius, 0, radius * Math.PI);
		context.fill();
		context.closePath();
	}

	toRadians(deg) {
		return deg * (Math.PI / 180);
	}

	toDegrees(rad) {
		return rad * (180 / Math.PI);
	}
}

// Associate the tag and the class
window.customElements.define(DIR_16_TAG_NAME, Raw16PointsDir);
