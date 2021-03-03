const scrollDigitVerbose = false;
const SCROLL_DIGIT_TAG_NAME = 'scroll-digit-display';

const SCROLL_DIGIT_CHARACTERS = [
	"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
	".", " " // Add more here if needed
];

const scrollDigitDefaultColorConfig = {
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
class ScrollDigitDisplay extends HTMLElement {

	static get observedAttributes() {
		return [
			"font-size",    // Integer. Font size in px. Default 30
			"nb-char",      // Integer. Nb flaps. Default 1
			"value",        // Initial value, default blank. Must be numeric of not blank
			"nb-dec",       // Number of decimal positions, default 0
			"justified"     // LEFT (default), RIGHT or CENTER
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		let fallbackElemt = document.createElement("h1");
		let content = document.createTextNode("This is a Scroll-Digit Display, on an HTML5 canvas");
		fallbackElemt.appendChild(content);
		this.canvas.appendChild(fallbackElemt);``
		this.shadowRoot.appendChild(this.canvas);

		this._connected = false;

		// Default values
		this._value      = "";
		this._paddedValue = "";
		this._font_size  = 30;
		this._nb_char    =  1;
		this._justified  = "LEFT";
		this._nb_dec = 0;

		this._previousClassName = "";
		this.scrollDigitColorConfig = scrollDigitDefaultColorConfig;

		if (scrollDigitVerbose) {
			console.log("Data in Constructor:", this._value);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		this._connected = true;
		if (scrollDigitVerbose) {
			console.log("connectedCallback invoked, 'value' is [", this._value, "]");
		}
		this.repaint();
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (scrollDigitVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (scrollDigitVerbose) {
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
			case "nb-dec":
				this._nb_dec = parseInt(newVal);
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
		if (scrollDigitVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set value(val) {
		this.setAttribute("value", val);
		if (scrollDigitVerbose) {
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
	get nbDec() {
		return this._nb_dec;
	}
	get justified() {
		return this._justified;
	}
	get shadowRoot() {
		return this._shadowRoot;
	}

	// Component methods
	getColorConfig(classNames) {
		let colorConfig = scrollDigitDefaultColorConfig;
		let classes = classNames.split(" ");
		for (let cls=0; cls<classes.length; cls++) {
			let cssClassName = classes[cls];
			for (let s=0; s<document.styleSheets.length; s++) {
				// console.log("Walking though ", document.styleSheets[s]);
				try {
					for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
						let selector = document.styleSheets[s].cssRules[r].selectorText;
						//			console.log(">>> ", selector);
						if (selector !== undefined && (selector === '.' + cssClassName || (selector.indexOf('.' + cssClassName) > -1 && selector.indexOf(SCROLL_DIGIT_TAG_NAME) > -1))) { // Cases like "tag-name .className"
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
			this.drawScrollDigit(this._value);
		}
	}

	getCharAt(idx) {
		let char = null;
		if (idx < this._nb_char) {
			char = this._paddedValue.split('')[idx];
		}
		return char;
	}

	getNextChar(char, sign) {

		if (sign === 0) {
			console.log("Sign = 0, should not happen...");
			debugger;
		}

		let intValue = parseInt(char); // (char === ' ' ? 0 : parseInt(char));
		intValue += sign;
		if (intValue > 9) {
			intValue = 0;
		}
		if (intValue < 0) {
			intValue = 9;
		}
		return intValue.toFixed(0);
	}

	setCharAt(idx, char, sign) {
		let newArray = this._paddedValue.split('');
		let fromValue = []; // Original string
		newArray.forEach(ch => fromValue.push(ch));
		newArray[idx] = char;
		this._paddedValue = newArray.join(''); // join() keeps the ',' between characters.
		let toValue = this._paddedValue;
		this.drawPaddedString(fromValue, toValue, sign);
	}

	cleanString(str) {
		if (typeof(str) !== "string") {
			console.debug('Not a string!!');
			debugger;
		}
		let clean = str;
		let cleanArr = clean.split('');
		for (let i=0; i<cleanArr.length; i++) {
			cleanArr[i] = cleanArr[i].toUpperCase();
			if (!SCROLL_DIGIT_CHARACTERS.includes(cleanArr[i])) {
				cleanArr[i] = ' ';
			}
		}
		return cleanArr.join('');
	}

	drawOneFlap(context, char, fromChar, sign, x, y, w, h, scale) {
		let grd = context.createLinearGradient(x, y, x + w, y + h);
		grd.addColorStop(0, this.scrollDigitColorConfig.displayBackgroundGradient.from); // 0  Beginning
		grd.addColorStop(1, this.scrollDigitColorConfig.displayBackgroundGradient.to);   // 1  End

		context.fillStyle = grd;
		context.strokeStyle = this.scrollDigitColorConfig.frameColor;
		context.lineWidth = 0.5;

		let str = char;
		if (!SCROLL_DIGIT_CHARACTERS.includes(char)) {
			str = ' ';
		}
		// Background
		ScrollDigitDisplay.roundRect(context, x, y, w, h, 1, true, false);
		context.fillStyle = this.scrollDigitColorConfig.displayColor;
		// Value
		context.font = "bold " + Math.round(scale * this._font_size) + "px " + this.scrollDigitColorConfig.valueFont;
		let strVal = str;
		let metrics = context.measureText(strVal);
		let len = metrics.width;

		let xOffset =  x + (w / 2) - (len / 2);
		if (fromChar !== null && fromChar !== char) {
			let yOffset = y + (h / 2); // Original
			let startOffset = yOffset;
			if (scrollDigitVerbose) {
				console.log(">>> Scrolling From ", fromChar, " to ", char, (sign > 0) ? "up" : "down", ", startYOffset",
					yOffset, "sign", sign, "height", Math.round(scale * this._font_size));
			}
			let fontSize = Math.round(scale * this._font_size);
			let instance = this;
			function getYOffset() {
				// console.log("Offset", yOffset, "Ref", startOffset);
				if (Math.abs(yOffset - startOffset) < fontSize) {
					setTimeout(() => {
						context.textBaseline = "middle";
						yOffset -= sign;
						context.fillStyle = grd;
						ScrollDigitDisplay.roundRect(context, x, y, w, h, 1, true, false);
						context.fillStyle = instance.scrollDigitColorConfig.displayColor;
						metrics = context.measureText(fromChar);
						len = metrics.width;
						xOffset =  x + (w / 2) - (len / 2);
						context.fillText(fromChar /*strVal*/, xOffset, yOffset);
						metrics = context.measureText(fromChar);
						len = metrics.width;
						xOffset =  x + (w / 2) - (len / 2);
						context.fillText(char /*strVal*/, xOffset, yOffset + (sign * fontSize));
						// Re-loop
						getYOffset();
					}, 10);
				} // else exit!
			}
			getYOffset();
		} else {
			let yOffset = y + (h / 2);
			context.textBaseline = "middle";
			context.fillText(strVal, xOffset, yOffset);
		}
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
		return paddedVal; // .trim();
	}

	drawScrollDigit(textValue) {
		if (this._connected) {
			let newVal = this.getPaddedValue(textValue);
			if (newVal.indexOf("NaN") > 0) {
				debugger;
			}
			this._paddedValue = newVal;
		}
		this.drawPaddedString();
	}

	drawPaddedString(fromValue, toValue, sign) {
		// From, to "12.34" to "23.45"
		if (fromValue !== undefined && toValue !== undefined) {
			// debugger;
			if (scrollDigitVerbose) {
				console.log("Scrolling from", fromValue, "to", toValue);
			}
		}
		let upperCaseValue = this._paddedValue.toUpperCase().split(''); // Char array

		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
			//	console.log("Reloading CSS");
			try {
				this.scrollDigitColorConfig = this.getColorConfig(currentStyle);
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

		context.fillStyle = this.scrollDigitColorConfig.bgColor;
		// Background
		ScrollDigitDisplay.roundRect(context, 0, 0, this.canvas.width, this.canvas.height, 5, true, false);

		for (let i=0; i<upperCaseValue.length; i++) {
			// TODO all characters one-by-one, but waiting for the previous one to be done?
			this.drawOneFlap(context,
				upperCaseValue[i],
				(fromValue !== undefined ? fromValue[i] : null),
				sign,
				i * oneWidth,
				0,
				oneWidth,
				height,
				scale);
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
			radius = 5; // Default
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
window.customElements.define(SCROLL_DIGIT_TAG_NAME, ScrollDigitDisplay);
