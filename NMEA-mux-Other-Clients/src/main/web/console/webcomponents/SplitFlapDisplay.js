const spliFlapVerbose = false;
const SPLIT_FLAP_TAG_NAME = 'split-flap-display';

const SPLIT_FLAP_CHARACTERS = [
	"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
	"N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
	"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
	"-", ":", ".", ",", "?", "!", "+", "=", "/", " " // Add more here if needed
];

const splitFlapDefaultColorConfig = {
	bgColor: 'transparent',
	displayBackgroundGradient: {
		from: 'black',
		to: 'gray'
	},
	displayColor: 'white',
	frameColor: 'silver',
	valueFont: 'Arial'
};

import * as Utilities from "./utilities/Utilities.js";

/* global HTMLElement */
class SplitFlapDisplay extends HTMLElement {

	static get observedAttributes() {
		return [
			"font-size",    // Integer. Font size in px. Default 30
			"nb-char",      // Integer. Nb flaps. Default 1
			"value",        // Initial value, default blank
			"justified"     // LEFT (default), RIGHT or CENTER
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		let fallbackElemt = document.createElement("h1");
		let content = document.createTextNode("This is a Split-Flap Display, on an HTML5 canvas");
		fallbackElemt.appendChild(content);
		this.canvas.appendChild(fallbackElemt);
		this.shadowRoot.appendChild(this.canvas);

		this._connected = false;

		// Default values
		this._value      = "";
		this._paddedValue = "";
		this._font_size  = 30;
		this._nb_char    =  1;
		this._justified  = "LEFT";

		this._previousClassName = "";
		this.splitFlapColorConfig = splitFlapDefaultColorConfig;

		if (spliFlapVerbose) {
			console.log("Data in Constructor:", this._value);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		this._connected = true;
		if (spliFlapVerbose) {
			console.log("connectedCallback invoked, 'value' is [", this._value, "]");
		}
		this.repaint();
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (spliFlapVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (spliFlapVerbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "value":
				this._value = newVal;
				break;
			case "font-size":
				this._font_size = parseInt(newVal);
				break;
			case "nb-char":
				this._nb_char = parseInt(newVal);
				break;
			case "justified":
				this._justified = (newVal === 'RIGHT' ? 'RIGHT' : 'LEFT');
				break;
			default:
				break;
		}
		this.repaint();
	}

	// Called whenever the custom element has been moved into a new document.
	adoptedCallback() {
		if (spliFlapVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set value(val) {
		this.setAttribute("value", val);
		if (spliFlapVerbose) {
			console.log(">> Value:", val);
		}
		this.repaint();
	}
	set fontSize(val) {
		this.setAttribute("font-size", val);
		this.repaint();
	}
	set nbChar(val) {
		this.setAttribute("nb-char", val);
		this.repaint();
	}
	set justified(val) {
		this.setAttribute("justified", val);
		this.repaint();
	}
	set shadowRoot(val) {
		this._shadowRoot = val;
	}

	get value() {
		return this._value;
	}
	get paddedValue() {
		return this._paddedValue;
	}
	get fontSize() {
		return this._font_size;
	}
	get nbChar() {
		return this._nb_char;
	}
	get justified() {
		return this._justified;
	}
	get shadowRoot() {
		return this._shadowRoot;
	}

	// Component methods
	getColorConfig(classNames) {
		let colorConfig = splitFlapDefaultColorConfig;
		let classes = classNames.split(" ");
		for (let cls=0; cls<classes.length; cls++) {
			let cssClassName = classes[cls];
			for (let s=0; s<document.styleSheets.length; s++) {
				// console.log("Walking though ", document.styleSheets[s]);
				try {
					for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
						let selector = document.styleSheets[s].cssRules[r].selectorText;
						//			console.log(">>> ", selector);
						if (selector !== undefined && (selector === '.' + cssClassName || (selector.indexOf('.' + cssClassName) > -1 && selector.indexOf(SPLIT_FLAP_TAG_NAME) > -1))) { // Cases like "tag-name .className"
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
										case '--display-color':
											colorConfig.displayColor = value;
											break;
										case '--frame-color':
											colorConfig.frameColor = value;
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
		if (this._connected) {
			this.drawSplitFlap(this._value);
		}
	}

	getCharAt(idx) {
		let char = null;
		if (idx < this._nb_char) {
			char = this._paddedValue.split('')[idx];
		}
		return char;
	}

	getNextChar(char) {
		let idx = -1;
		for (let i=0; i<SPLIT_FLAP_CHARACTERS.length; i++) {
			if (SPLIT_FLAP_CHARACTERS[i] === char.toUpperCase()) {
				idx = i;
				break;
			}
		}
		idx += 1;
		if (idx >= SPLIT_FLAP_CHARACTERS.length) {
			idx = 0;
		}
		return SPLIT_FLAP_CHARACTERS[idx];
	}

	setCharAt(idx, char) {
		let newArray = this._paddedValue.split('');
		newArray[idx] = char;
		this._paddedValue = newArray.join(''); // join() keeps the ',' between characters.
		this.drawPaddedString();
	}

	cleanString(str) {
		let clean = str;
		let cleanArr = clean.split('');
		for (let i=0; i<cleanArr.length; i++) {
			cleanArr[i] = cleanArr[i].toUpperCase();
			if (!SPLIT_FLAP_CHARACTERS.includes(cleanArr[i])) {
				cleanArr[i] = ' ';
			}
		}
		return cleanArr.join('');
	}

	drawOneFlap(context, char, x, y, w, h, scale) {
		let grd = context.createLinearGradient(x, y, x + w, y + h);
		grd.addColorStop(0, this.splitFlapColorConfig.displayBackgroundGradient.from); // 0  Beginning
		grd.addColorStop(1, this.splitFlapColorConfig.displayBackgroundGradient.to);   // 1  End

		context.fillStyle = grd;
		context.strokeStyle = this.splitFlapColorConfig.frameColor;
		context.lineWidth = 0.5;
		// Background
		SplitFlapDisplay.roundRect(context, x, y, w, h, 1, true, false);

		context.beginPath();
		context.moveTo(x, y + (h / 2));
		context.lineTo(x + w, y + (h / 2));
		context.closePath();
		context.stroke();

		let str = char;
		if (!SPLIT_FLAP_CHARACTERS.includes(char)) {
			str = ' ';
		}
		context.fillStyle = this.splitFlapColorConfig.displayColor;
		// Value
		context.font = "bold " + Math.round(scale * this._font_size) + "px " + this.splitFlapColorConfig.valueFont;
		let strVal = str;
		let metrics = context.measureText(strVal);
		let len = metrics.width;

		let xOffset =  x + (w / 2) - (len / 2);
		let yOffset = y + (h / 2);
		context.textBaseline = "middle";
		context.fillText(strVal, xOffset, yOffset);
	}

	getPaddedValue(val) {
		let paddedVal = val;
		if (val.length > this._nb_char) {
			switch (this._justified) {
				case "LEFT":
					paddedVal = val.toUpperCase().substring(0, this._nb_char);
					break;
				case "RIGHT":
					paddedVal = val.toUpperCase().substring(val.length - this._nb_char);
					break;
				case "CENTER": // TODO
				default:
					break;
			}
		}
		switch (this._justified) {
			case "LEFT":
				paddedVal = Utilities.rpad(val, this._nb_char);
				break;
			case "RIGHT":
				paddedVal = Utilities.lpad(val, this._nb_char);
				break;
			case "CENTER": // TODO
			default:
				break;
		}
		return paddedVal;
	}

	drawSplitFlap(textValue) {
		if (this._connected) {
			this._paddedValue = this.getPaddedValue(textValue);
		}
		this.drawPaddedString();
	}

	drawPaddedString() {
		let upperCaseValue = this._paddedValue.toUpperCase().split(''); // Char array

		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
			//	console.log("Reloading CSS");
			try {
				this.splitFlapColorConfig = this.getColorConfig(currentStyle);
			} catch (err) {
				// Absorb?
				console.log(err);
			}

			this._previousClassName = currentStyle;
		}

		let context = this.canvas.getContext('2d');
		let scale = 1.0;

		let height = Math.round(this._font_size * 1.1); // cell height
		let oneWidth = Math.round(height * 0.9);        // cell width
		let width = this._nb_char * oneWidth;

		// Set the canvas size from its container.
		this.canvas.width = width;
		this.canvas.height = height;

		context.fillStyle = this.splitFlapColorConfig.bgColor;
		// Background
		SplitFlapDisplay.roundRect(context, 0, 0, this.canvas.width, this.canvas.height, 5, true, false);

		for (let i=0; i<upperCaseValue.length; i++) {
			this.drawOneFlap(context, upperCaseValue[i], i * oneWidth, 0, oneWidth, height, scale);
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
window.customElements.define(SPLIT_FLAP_TAG_NAME, SplitFlapDisplay);
