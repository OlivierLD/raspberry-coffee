const analogVerbose = false;
const ANALOG_TAG_NAME = 'analog-display';

if (Math.toRadians === undefined) {
	Math.toRadians = (deg) => {
		return deg * (Math.PI / 180);
	};
}

/*
* See custom properties in CSS.
* =============================
* @see https://developer.mozilla.org/en-US/docs/Web/CSS/
* Relies on those elements:
*
.xxxxxxxx {
	--bg-color: rgba(0, 0, 0, 0);
	--digit-color: black;
	--with-gradient: true;
	--display-background-gradient-from: LightGrey;
	--display-background-gradient-to: white;
	--display-line-color: rgba(255, 255, 255, 0.5);
	--label-fill-color: rgba(255, 255, 255, 0.5);
	--with-display-shadow: false;
	--shadow-color: rgba(0, 0, 0, 0.75);
	--outline-color: DarkGrey;
	--major-tick-color: black;
	--minor-tick-color: black;
	--value-color: grey;
	--value-outline-color: black;
	--value-nb-decimal: 1;
	--hand-color: red;
	--hand-outline-color: black;
	--with-hand-shadow: true;
	--knob-color: DarkGrey;
	--knob-outline-color: black;
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
const defaultAnalogDisplayColorConfig = {
	bgColor: 'rgba(0, 0, 0, 0)', /* transparent, 'white' */
	digitColor: 'black',
	withGradient: true,
	displayBackgroundGradient: {
		from: 'LightGrey',
		to: 'white'
	},
	displayLineColor: 'rgba(0, 0, 0, 0.5)',
	labelFillColor: 'rgba(255, 255, 255, 0.5)',
	withDisplayShadow: true,
	shadowColor: 'rgba(0, 0, 0, 0.75)',
	outlineColor: 'DarkGrey',
	majorTickColor: 'black',
	minorTickColor: 'black',
	valueColor: 'grey',
	valueOutlineColor: 'black',
	valueNbDecimal: 1,
	handColor: 'red', // 'rgba(0, 0, 100, 0.25)',
	handOutlineColor: 'black',
	withHandShadow: true,
	knobColor: 'DarkGrey',
	knobOutlineColor: 'black',
	font: 'Arial', /* 'Source Code Pro' */
	valueFontSizeFactor: 1
};

// import * as Utilities from "../utilities/Utilities.js";

/* global HTMLElement */
class AnalogDisplay extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",            // Integer. Canvas width
			"height",           // Integer. Canvas height
			"major-ticks",      // Float. value between major ticks (those with labels)
			"minor-ticks",      // Float. value between minor ticks
			"min-value",        // Float. Min value. Default 0
			"max-value",        // Float. Max value.
			"overlap",          // Integer. Display overlap in degrees. Default 0.
			"with-min-max",     // Boolean, default false
			"with-digits",      // Boolean, default true. Index Values for major-ticks
			"with-border",      // Boolean, default true
			"label",            // String.
			"rotate-digits",    // Boolean, default true. false means display all values straight, no rotation
			"major-ticks-2",    // Float. For an inner scale, value between major ticks (those with labels)
			"minor-ticks-2",    // Float. For an inner scale, value between minor ticks
			"min-value-2",      // Float. For an inner scale, Min value. Default 0
			"max-value-2",      // Float. For an inner scale, Max value.
			"digital-data-len", // Integer, optional, to display instead of label, like log value along with BSP. Number of characters to display
			"digital-data-val", // Float, optional. Must be present if the above exists.
			"value",            // Float. Value to display
			"value-2",          // Float. Value to display, for scale 2
			"unit",             // String. Unit
			"unit-2"            // String. Unit 2
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		let fallbackElemt = document.createElement("h1");
		let content = document.createTextNode("This is an AnalogDisplay, on an HTML5 canvas");
		fallbackElemt.appendChild(content);
		this.canvas.appendChild(fallbackElemt);
		this.shadowRoot.appendChild(this.canvas);

		// Default values
		this._value            =   0;
		this._value_2          = undefined;
		this._width            = 150;
		this._height           = 150;
		this._major_ticks      =  10;
		this._minor_ticks      =   1;
		this._min_value        =   0.0;
		this._max_value        =   1.0;
		this._overlap          =   0;
		this._with_min_max     = false;
		this._with_digits      = true;
		this._with_border      = true;
		this._rotate_digits    = true;
		this._label            = undefined;
		this._digital_data_len = undefined;
		this._digital_data_val = undefined;

		this._major_ticks_2    = undefined;
		this._minor_ticks_2    = undefined;
		this._min_value_2      = 0;
		this._max_value_2      = undefined;

		this._unit             = undefined;
		this._unit_2           = undefined;

		this.miniVal =  10000000;
		this.maxiVal = -10000000;

		this._previousClassName = "";
		this.analogDisplayColorConfig = defaultAnalogDisplayColorConfig; // Init

		if (analogVerbose) {
			console.log("Data in Constructor:", this._value);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (analogVerbose) {
			console.log("connectedCallback invoked, 'value' is [", this.value, "]");
		}
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (analogVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (analogVerbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "value":
				this._value = parseFloat(newVal);
				this.miniVal = Math.min(Math.max(this._min_value, this._value), this.miniVal);
				this.maxiVal = Math.max(Math.min(this._max_value, this._value), this.maxiVal);
				break;
			case "value-2":
				this._value_2 = parseFloat(newVal);
				break;
			case "unit":
				this._unit = newVal;
				break;
			case "unit-2":
				this._unit_2 = newVal;
				break;
			case "width":
				this._width = parseInt(newVal);
				break;
			case "height":
				this._height = parseInt(newVal);
				break;
			case "major-ticks":
				this._major_ticks = parseFloat(newVal);
				break;
			case "minor-ticks":
				this._minor_ticks = parseFloat(newVal);
				break;
			case "min-value":
				this._min_value = parseFloat(newVal);
				break;
			case "max-value":
				this._max_value = parseFloat(newVal);
				break;
			case "major-ticks-2":
				this._major_ticks_2 = parseFloat(newVal);
				break;
			case "minor-ticks-2":
				this._minor_ticks_2 = parseFloat(newVal);
				break;
			case "min-value-2":
				this._min_value_2 = parseFloat(newVal);
				break;
			case "max-value-2":
				this._max_value_2 = parseFloat(newVal);
				break;
			case "overlap":
				this._overlap = parseInt(newVal);
				break;
			case "with-digits":
				this._with_digits = ("true" === newVal);
				break;
			case "rotate-digits":
				this._rotate_digits = ("true" === newVal);
				break;
			case "with-border":
				this._with_border = ("true" === newVal);
				break;
			case "with-min-max":
				this._with_min_max = ("true" === newVal);
				break;
			case "label":
				this._label = newVal;
				break;
			case "digital-data-len":
				this._digital_data_len = parseInt(newVal);
				break;
			case "digital-data-val":
				this._digital_data_val = parseFloat(newVal);
				break;
			default:
				break;
		}
		this.repaint();
	}

	// Called whenever the custom element has been moved into a new document.
	adoptedCallback() {
		if (analogVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set value(option) {
		// this.setAttribute("value", option);
		if (analogVerbose) {
			console.log(">> Value option:", option);
		}
		let delta = Math.abs(this._value - option);
		if (delta > 0.1 && delta < 2.0) {
			// Smooth? This is a test.
			// console.log(`Smooth Move!`);
			let incr = (this._value > option) ? -0.1 : 0.1; // TODO Make those values parameters
			let currVal = this._value;
			let instance = this;
			function smoothMove() {
				currVal += incr;
				instance.updateValue(currVal);
				// instance.setAttribute("value", option);
				if (Math.abs(currVal - option) > 0.1) {
					setTimeout(smoothMove, 50);
				}
			}
			smoothMove();
		} else {
			this.setAttribute("value", option);
		}
//	this.repaint();
	}

	updateValue = function(val) {
		this.setAttribute("value", val);
	}

	set value2(option) {
		this.setAttribute("value-2", option);
		if (analogVerbose) {
			console.log(">> Value2 option:", option);
		}
//	this.repaint();
	}
	set width(val) {
		this.setAttribute("width", val);
	}
	set height(val) {
		this.setAttribute("height", val);
	}
	set majorTicks(val) {
		this.setAttribute("major-ticks", val);
	}
	set minorTicks(val) {
		this.setAttribute("minor-ticks", val);
	}
	set majorTicks2(val) {
		this.setAttribute("major-ticks-2", val);
	}
	set minorTicks2(val) {
		this.setAttribute("minor-ticks-2", val);
	}
	set withMinMax(val) {
		this.setAttribute("with-min-max", val);
	}
	set withDigits(val) {
		this.setAttribute("with-digits", val);
	}
	set rotateDigits(val) {
		this.setAttribute("rotate-digits", val);
	}
	set withBorder(val) {
		this.setAttribute("with-border", val);
	}
	set overlap(val) {
		this.setAttribute("overlap", val);
	}
	set minValue(val) {
		this.setAttribute("min-value", val);
	}
	set maxValue(val) {
		this.setAttribute("max-value", val);
	}
	set minValue2(val) {
		this.setAttribute("min-value-2", val);
	}
	set maxValue2(val) {
		this.setAttribute("max-value-2", val);
	}
	set label(val) {
		this.setAttribute("label", val);
	}
	set unit(val) {
		this.setAttribute("unit", val);
	}
	set unit2(val) {
		this.setAttribute("unit-2", val);
	}
	set digitalDataLen(val) {
		this.setAttribute("digital-data-len", val);
	}
	set digitalDataVal(val) {
		this.setAttribute("digital-data-val", val);
	}
	set shadowRoot(val) {
		this._shadowRoot = val;
	}

	get value() {
		return this._value;
	}
	get value2() {
		return this._value_2;
	}
	get width() {
		return this._width;
	}
	get height() {
		return this._height;
	}
	get minorTicks() {
		return this._minor_ticks;
	}
	get majorTicks() {
		return this._major_ticks;
	}
	get minorTicks2() {
		return this._minor_ticks_2;
	}
	get majorTicks2() {
		return this._major_ticks_2;
	}
	get withMinMax() {
		return this._with_min_max;
	}
	get withDigits() {
		return this._with_digits;
	}
	get rotateDigits() {
		return this._rotate_digits;
	}
	get withBorder() {
		return this._with_border;
	}
	get minValue() {
		return this._min_value;
	}
	get maxValue() {
		return this._max_value;
	}
	get minValue2() {
		return this._min_value_2;
	}
	get maxValue2() {
		return this._max_value_2;
	}
	get overlap() {
		return this._overlap;
	}
	get label() {
		return this._label;
	}
	get unit() {
		return this._unit;
	}
	get unit2() {
		return this._unit_2;
	}
	get digitalDataLen() {
		return this._digital_data_len;
	}
	get digitalDataVal() {
		return this._digital_data_val;
	}
	get shadowRoot() {
		return this._shadowRoot;
	}

	// Component methods
	repaint() {
		this.drawDisplay(this._value, this._value_2);
	}

	getColorConfig(classNames) {
		let colorConfig = defaultAnalogDisplayColorConfig;
		let classes = classNames.split(" ");
		for (let cls=0; cls<classes.length; cls++) {
			let className = classes[cls];
			for (let s=0; s<document.styleSheets.length; s++) {
				//		console.log("Walking though ", document.styleSheets[s]);
				try {
					for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
						let selector = document.styleSheets[s].cssRules[r].selectorText;
						//			console.log(">>> ", selector);
						if (selector !== undefined && (selector === '.' + className || (selector.indexOf('.' + className) > -1 && selector.indexOf(ANALOG_TAG_NAME) > -1))) { // Cases like "tag-name .className"
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
										case '--display-line-color':
											colorConfig.displayLineColor = value;
											break;
										case '--label-fill-color':
											colorConfig.labelFillColor = value;
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
										case '--hand-color':
											colorConfig.handColor = value;
											break;
										case '--hand-outline-color':
											colorConfig.handOutlineColor = value;
											break;
										case '--with-hand-shadow':
											colorConfig.withHandShadow = (value === 'true');
											break;
										case '--knob-color':
											colorConfig.knobColor = value;
											break;
										case '--knob-outline-color':
											colorConfig.knobOutlineColor = value;
											break;
										case '--font':
											colorConfig.font = value;
											break;
										case '--value-font-size-factor':
											colorConfig.valueFontSizeFactor = parseFloat(value);
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

	drawDisplay(analogValue, analogValue2) {

		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
			//	console.log("Reloading CSS");
			try {
				this.analogDisplayColorConfig = this.getColorConfig(currentStyle);
			} catch (err) {
				// Absorb?
				console.log(err);
			}
			this._previousClassName = currentStyle;
		}

		let digitColor = this.analogDisplayColorConfig.digitColor;

		let context = this.canvas.getContext('2d');
		context.clearRect(0, 0, this.width, this.height);

		let radius = (Math.min(this.width, this.height) / (this.overlap > 0 ? 2 : 1)) * 0.90;

		// Set the canvas size from its container.
		this.canvas.width = this.width;
		this.canvas.height = this.height;

		let totalAngle = (Math.PI + (2 * (Math.toRadians(this.overlap))));

		context.beginPath();

		if (this.withBorder === true) {
			//  context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);
//    context.arc(canvas.width / 2, radius + 10, radius, Math.PI - toRadians(overlapOver180InDegree), (2 * Math.PI) + toRadians(overlapOver180InDegree), false);
			context.arc(this.canvas.width / 2, radius + 10, radius, Math.PI - Math.toRadians(this.overlap > 0 ? 90 : 0), (2 * Math.PI) + Math.toRadians(this.overlap > 0 ? 90 : 0), false);
			context.lineWidth = 5;
		}

		if (this.analogDisplayColorConfig.withGradient) {
			let grd = context.createLinearGradient(0, 5, 0, radius);
			grd.addColorStop(0, this.analogDisplayColorConfig.displayBackgroundGradient.from);// 0  Beginning
			grd.addColorStop(1, this.analogDisplayColorConfig.displayBackgroundGradient.to);  // 1  End
			context.fillStyle = grd;
		} else {
			context.fillStyle = this.analogDisplayColorConfig.displayBackgroundGradient.to;
		}
		if (this.analogDisplayColorConfig.withDisplayShadow) {
			context.shadowOffsetX = 3;
			context.shadowOffsetY = 3;
			context.shadowBlur = 3;
			context.shadowColor = this.analogDisplayColorConfig.shadowColor;
		} else {
			context.shadowOffsetX = 0;
			context.shadowOffsetY = 0;
			context.shadowBlur = 0;
			context.shadowColor = undefined;
		}
		context.lineJoin = "round";
		context.fill();
		context.strokeStyle = this.analogDisplayColorConfig.outlineColor;
		context.stroke();
		context.closePath();

		// Min-max?
		if (this.withMinMax && this.miniVal < this.maxiVal) {
			context.beginPath();

			let ___minAngle = (totalAngle * ((this.miniVal - this.minValue) / (this.maxValue - this.minValue))) - Math.toRadians(this.overlap) - (Math.PI);
			let ___maxAngle = (totalAngle * ((this.maxiVal - this.minValue) / (this.maxValue - this.minValue))) - Math.toRadians(this.overlap) - (Math.PI);

			//Center
			context.moveTo(this.canvas.width / 2, radius + 10);
			context.arc(this.canvas.width / 2, radius + 10, radius * 0.75,
					(___minAngle),
					(___maxAngle),
					false);

//    context.arc(288, 75, 70, 0, Math.PI, false);
			context.closePath();
			context.lineWidth = 1;
			context.fillStyle = 'gray';
			context.fill();
//    context.strokeStyle = '#550000';
//    context.stroke();
		}

		// Major Ticks
		context.beginPath();
		for (let i = 0; i <= (this.maxValue - this.minValue); i++) {
			if ((i + this.minValue) % this.majorTicks === 0) {
				let currentAngle = (totalAngle * (i / (this.maxValue - this.minValue))) - Math.toRadians(this.overlap);
				let xFrom = (this.canvas.width / 2) - ((radius * 0.95) * Math.cos(currentAngle));
				let yFrom = (radius + 10) - ((radius * 0.95) * Math.sin(currentAngle));
				let xTo = (this.canvas.width / 2) - ((radius * 0.85) * Math.cos(currentAngle));
				let yTo = (radius + 10) - ((radius * 0.85) * Math.sin(currentAngle));
				context.moveTo(xFrom, yFrom);
				context.lineTo(xTo, yTo);
			}
		}
		context.lineWidth = 3;
		context.strokeStyle = this.analogDisplayColorConfig.majorTickColor;
		context.stroke();
		context.closePath();

		// Minor Ticks
		if (this.minorTicks > 0) {
			context.beginPath();
			for (let i = 0; i <= (this.maxValue - this.minValue); i += this.minorTicks) {
				let _currentAngle = (totalAngle * (i / (this.maxValue - this.minValue))) - Math.toRadians(this.overlap);

				let xFrom = (this.canvas.width / 2) - ((radius * 0.95) * Math.cos(_currentAngle));
				let yFrom = (radius + 10) - ((radius * 0.95) * Math.sin(_currentAngle));
				let xTo = (this.canvas.width / 2) - ((radius * 0.90) * Math.cos(_currentAngle));
				let yTo = (radius + 10) - ((radius * 0.90) * Math.sin(_currentAngle));
				context.moveTo(xFrom, yFrom);
				context.lineTo(xTo, yTo);
			}
			context.lineWidth = 1;
			context.strokeStyle = this.analogDisplayColorConfig.minorTickColor;
			context.stroke();
			context.closePath();
		}

		let smallScaleRatio = 0.65;
		// Second scale?
		if (this.majorTicks2 !== undefined && this.maxValue2 !== undefined) {
			context.beginPath();
			for (let i = 0; i <= (this.maxValue2 - this.minValue2); i++) {
				if ((i + this.minValue2) % this.majorTicks2 === 0) {
					let currentAngle = (totalAngle * (i / (this.maxValue2 - this.minValue2))) - Math.toRadians(this.overlap);
					let xFrom = (this.canvas.width / 2) - ((radius * smallScaleRatio * 0.95) * Math.cos(currentAngle));
					let yFrom = (radius + 10) - ((radius * smallScaleRatio * 0.95) * Math.sin(currentAngle));
					let xTo = (this.canvas.width / 2) - ((radius * smallScaleRatio * 0.85) * Math.cos(currentAngle));
					let yTo = (radius + 10) - ((radius * smallScaleRatio * 0.85) * Math.sin(currentAngle));
					context.moveTo(xFrom, yFrom);
					context.lineTo(xTo, yTo);
				}
			}
			context.lineWidth = 2;
			context.strokeStyle = this.analogDisplayColorConfig.majorTickColor;
			context.stroke();
			context.closePath();

			// Minor Ticks ?
			if (this.minorTicks2 !== undefined && this.minorTicks2 > 0) {
				context.beginPath();
				for (let i = 0; i <= (this.maxValue2- this.minValue2); i += this.minorTicks2) {
					let _currentAngle = (totalAngle * (i / (this.maxValue2 - this.minValue2))) - Math.toRadians(this.overlap);

					let xFrom = (this.canvas.width / 2) - ((radius * smallScaleRatio * 0.95) * Math.cos(_currentAngle));
					let yFrom = (radius + 10) - ((radius * smallScaleRatio * 0.95) * Math.sin(_currentAngle));
					let xTo = (this.canvas.width / 2) - ((radius * smallScaleRatio * 0.90) * Math.cos(_currentAngle));
					let yTo = (radius + 10) - ((radius * smallScaleRatio * 0.90) * Math.sin(_currentAngle));
					context.moveTo(xFrom, yFrom);
					context.lineTo(xTo, yTo);
				}
				context.lineWidth = 1;
				context.strokeStyle = this.analogDisplayColorConfig.minorTickColor;
				context.stroke();
				context.closePath();
			}
		}

		let scale = radius / 100;
		// Numbers (indexes) on major (and major-2) ticks
		if (this.withDigits) {
			context.beginPath();
			for (let i = 0; i <= (this.maxValue - this.minValue); i++) {
				if ((i + this.minValue) % this.majorTicks === 0) {
					context.save();
					context.font = "bold " + Math.round(scale * 15) + "px " + this.analogDisplayColorConfig.font; // Like "bold 15px Arial"
					context.fillStyle = digitColor;
					let str = (i + this.minValue).toString();
					let len = context.measureText(str).width;
					let __currentAngle = (totalAngle * (i / (this.maxValue - this.minValue))) - Math.toRadians(this.overlap);
					if (this.rotateDigits) { // Rotate
						context.translate(this.canvas.width / 2, (radius + 10)); // canvas.height);
//          context.rotate((Math.PI * (i / maxValue)) - (Math.PI / 2));
						context.rotate(__currentAngle - (Math.PI / 2));
						context.fillText(str, -len / 2, (-(radius * .8) + 10));
						context.lineWidth = 1;
						context.strokeStyle = this.analogDisplayColorConfig.valueOutlineColor;
						context.strokeText(str, -len / 2, (-(radius * .8) + 10)); // Outlined
					} else {                // Don't rotate
						let xTo = (this.canvas.width / 2) - ((radius * 0.75) * Math.cos(__currentAngle));
						let yTo = (radius + 10) - ((radius * 0.75) * Math.sin(__currentAngle));
						context.fillText(str, xTo - (len / 2), yTo + (scale * 11 / 2));
						context.lineWidth = 1;
						context.strokeStyle = this.analogDisplayColorConfig.valueOutlineColor;
						context.strokeText(str, xTo - (len / 2), yTo + (scale * 11 / 2)); // Outlined
					}
					context.restore();
				}
			}
			context.closePath();

			if (this.majorTicks2 !== undefined && this.maxValue2 !== undefined) {
				context.beginPath();
				for (let i = 0; i <= (this.maxValue2 - this.minValue2); i++) {
					if ((i + this.minValue2) % this.majorTicks2 === 0) {
						context.save();
						context.font = "bold " + Math.round(scale * 8) + "px " + this.analogDisplayColorConfig.font; // Like "bold 15px Arial"
						context.fillStyle = digitColor;
						let str = (i + this.minValue2).toString();
						let len = context.measureText(str).width;
						let __currentAngle = (totalAngle * (i / (this.maxValue2 - this.minValue2))) - Math.toRadians(this.overlap);
						if (this.rotateDigits) { // Rotate
							context.translate(this.canvas.width / 2, (radius + 10)); // canvas.height);
//          context.rotate((Math.PI * (i / maxValue)) - (Math.PI / 2));
							context.rotate(__currentAngle - (Math.PI / 2));
							context.fillText(str, -len / 2, (-(radius * smallScaleRatio * .8) + 10));
							context.lineWidth = 1;
							context.strokeStyle = this.analogDisplayColorConfig.valueOutlineColor;
							context.strokeText(str, -len / 2, (-(radius * smallScaleRatio * .8) + 10)); // Outlined
						} else {                // Don't rotate
							let xTo = (this.canvas.width / 2) - ((radius * smallScaleRatio * 0.75) * Math.cos(__currentAngle));
							let yTo = (radius + 10) - ((radius * smallScaleRatio * 0.75) * Math.sin(__currentAngle));
							context.fillText(str, xTo - (len / 2), yTo + (scale * 5 / 2));
							context.lineWidth = 1;
							context.strokeStyle = this.analogDisplayColorConfig.valueOutlineColor;
							context.strokeText(str, xTo - (len / 2), yTo + (scale * 5 / 2)); // Outlined
						}
						context.restore();
					}
				}
				context.closePath();

			}
		}

		// Value
		let text = analogValue.toFixed(this.analogDisplayColorConfig.valueNbDecimal);
//  text = displayValue.toFixed(nbDecimal); // analogDisplayColorConfig.valueNbDecimal);
		let len = 0;
		let fontCoeff = 40;
		if (this.majorTicks2 !== undefined && this.maxValue2 !== undefined) { // Display value smaller if there is a second scale.
			fontCoeff /= 1.5;
		}
		context.font = "bold " + Math.round(scale * fontCoeff * this.analogDisplayColorConfig.valueFontSizeFactor) + "px " + this.analogDisplayColorConfig.font; // "bold 40px Arial"
		let metrics = context.measureText(text);
		len = metrics.width;

		let yValueOffsetCoeff = .75;
		if (this.majorTicks2 !== undefined && this.maxValue2 !== undefined) { // Display value smaller if there is a second scale.
			yValueOffsetCoeff = .85;
		}
		context.beginPath();
		context.fillStyle = this.analogDisplayColorConfig.valueColor;
		context.fillText(text, (this.canvas.width / 2) - (len / 2), ((radius * yValueOffsetCoeff) + 10));
		context.lineWidth = 1;
		context.strokeStyle = this.analogDisplayColorConfig.valueOutlineColor;
		context.strokeText(text, (this.canvas.width / 2) - (len / 2), ((radius * yValueOffsetCoeff) + 10)); // Outlined
		context.closePath();

		if (analogValue2 !== undefined) {
			let text = analogValue2.toFixed(this.analogDisplayColorConfig.valueNbDecimal);
//  text = displayValue.toFixed(nbDecimal); // analogDisplayColorConfig.valueNbDecimal);
			let len = 0;
			let fontCoeff = 20;
			context.font = "bold " + Math.round(scale * fontCoeff * this.analogDisplayColorConfig.valueFontSizeFactor) + "px " + this.analogDisplayColorConfig.font; // "bold 40px Arial"
			let metrics = context.measureText(text);
			len = metrics.width;

			let yValueOffsetCoeff = 1.25;
			context.beginPath();
			context.fillStyle = this.analogDisplayColorConfig.valueColor;
			context.fillText(text, (this.canvas.width / 2) - (len / 2), ((radius * yValueOffsetCoeff) + 10));
			context.lineWidth = 1;
			context.strokeStyle = this.analogDisplayColorConfig.valueOutlineColor;
			context.strokeText(text, (this.canvas.width / 2) - (len / 2), ((radius * yValueOffsetCoeff) + 10)); // Outlined
			context.closePath();
		}

		// Label ?
		if (this.label !== undefined) {
			let fontSize = 20;
			text = this.label;
			len = 0;
			context.font = "bold " + Math.round(scale * fontSize) + "px " + this.analogDisplayColorConfig.font; // "bold 40px Arial"
			metrics = context.measureText(text);
			len = metrics.width;

			context.beginPath();
			context.fillStyle = this.analogDisplayColorConfig.labelFillColor;
			context.fillText(text, (this.canvas.width / 2) - (len / 2), (2 * radius - (fontSize * scale * 2.1)) + (this.digitalDataLen !== undefined ? 15 : 0));
			context.lineWidth = 1;
			context.strokeStyle = this.analogDisplayColorConfig.valueOutlineColor;
			context.strokeText(text, (this.canvas.width / 2) - (len / 2), (2 * radius - (fontSize * scale * 2.1)) + (this.digitalDataLen !== undefined ? 15 : 0)); // Outlined
			context.closePath();
		}
		// Unit
		if (this.unit !== undefined) {
			let fontSize = 12;
			text = this.unit;
			len = 0;
			context.font = "bold " + Math.round(scale * fontSize) + "px " + this.analogDisplayColorConfig.font; // "bold 40px Arial"
			metrics = context.measureText(text);
			len = metrics.width;

			let lineOffset = 5;
			context.beginPath();
			context.fillStyle = this.analogDisplayColorConfig.valueColor;
			context.fillText(text, (this.canvas.width / 2) - (len / 2), (2 * radius - (lineOffset * scale * 2.1)) + (this.digitalDataLen !== undefined ? 15 : 0));
			context.lineWidth = 1;
			context.strokeStyle = this.analogDisplayColorConfig.valueOutlineColor;
			context.strokeText(text, (this.canvas.width / 2) - (len / 2), (2 * radius - (lineOffset * scale * 2.1)) + (this.digitalDataLen !== undefined ? 15 : 0)); // Outlined
			context.closePath();
		}
		// Unit 2
		if (this.unit2 !== undefined) {
			let fontSize = 8;
			text = this.unit2;
			len = 0;
			context.font = "bold " + Math.round(scale * fontSize) + "px " + this.analogDisplayColorConfig.font; // "bold 40px Arial"
			metrics = context.measureText(text);
			len = metrics.width;

			let lineOffset = 11;
			context.beginPath();
			context.fillStyle = this.analogDisplayColorConfig.valueColor;
			context.fillText(text, (this.canvas.width / 2) - (len / 2), (2 * radius - (lineOffset * scale * 2.1)) + (this.digitalDataLen !== undefined ? 15 : 0));
			context.lineWidth = 1;
			context.strokeStyle = this.analogDisplayColorConfig.valueOutlineColor;
			context.strokeText(text, (this.canvas.width / 2) - (len / 2), (2 * radius - (lineOffset * scale * 2.1)) + (this.digitalDataLen !== undefined ? 15 : 0)); // Outlined
			context.closePath();
		}

		// Digits? Note: not compatible with label (above), would hide it. Example: Log Value // TODO Look into this
		if (this.digitalDataLen !== undefined) {
			let oneDigitWidth = (this.canvas.width / 3) / this.digitalDataLen;
			let oneDigitHeight = oneDigitWidth * 1.4;

			if (this.analogDisplayColorConfig.withGradient) {
				let start = 1.025 * (this.canvas.height / 2);
				let grd = context.createLinearGradient(0, start, 0, start + oneDigitHeight);
				grd.addColorStop(0, this.analogDisplayColorConfig.displayBackgroundGradient.to);   // 0  Beginning
				grd.addColorStop(1, this.analogDisplayColorConfig.displayBackgroundGradient.from); // 1  End
				context.fillStyle = grd;
			} else {
				context.fillStyle = this.analogDisplayColorConfig.displayBackgroundGradient.to;
			}

			// The rectangles around each digit
			let distFactor = 1.1;
			let digitOrigin = (this.canvas.width / 2) - ((this.digitalDataLen * oneDigitWidth) / 2);
			for (let i = 0; i < this.digitalDataLen; i++) {
				context.beginPath();

				let x = digitOrigin + (i * oneDigitWidth);
				let y = distFactor * (this.canvas.height / 2);
				context.fillRect(x, y, oneDigitWidth, oneDigitHeight);
				context.lineWidth = 1;
				context.strokeStyle = this.analogDisplayColorConfig.displayLineColor;
				context.rect(x, y, oneDigitWidth, oneDigitHeight);
				context.stroke();
				context.closePath();
			}

			context.shadowOffsetX = 0;
			context.shadowOffsetY = 0;
			context.shadowBlur = 0;

			// Value
			if (this.digitalDataVal !== undefined) {
				let text = this.digitalDataVal.toFixed(0);
				while (text.length < this.digitalDataLen) {
					text = '0' + text;
				}
				let fontSize = Math.round(scale * 14);
				for (let i = 0; i < this.digitalDataLen; i++) {
					len = 0;
					context.font = "bold " + fontSize + "px Arial"; // "bold 40px Arial"
					let txt = text.substring(i, i + 1);
					let metrics = context.measureText(txt);
					len = metrics.width;
					let x = digitOrigin + (i * oneDigitWidth);
					let y = distFactor * (this.canvas.height / 2);
					context.beginPath();
					context.fillStyle = this.analogDisplayColorConfig.valueColor;
					context.fillText(txt, x + (oneDigitWidth / 2) - (len / 2), y + (oneDigitHeight / 2) + (fontSize / 2));
					context.lineWidth = 1;
					context.strokeStyle = this.analogDisplayColorConfig.valueOutlineColor;
					context.strokeText(txt, x + (oneDigitWidth / 2) - (len / 2), y + (oneDigitHeight / 2) + (fontSize / 2)); // Outlined
					context.closePath();
				}
			}
		}

		// Hand
		context.beginPath();
		if (this.analogDisplayColorConfig.withHandShadow) {
			context.shadowColor = this.analogDisplayColorConfig.shadowColor;
			context.shadowOffsetX = 3;
			context.shadowOffsetY = 3;
			context.shadowBlur = 3;
		}
		// Center
		context.moveTo(this.canvas.width / 2, radius + 10);
		let valInBoundaries = Math.min(analogValue, this._max_value);
		valInBoundaries = Math.max(valInBoundaries, this._min_value);

		let ___currentAngle = (totalAngle * ((valInBoundaries - this.minValue) / (this.maxValue - this.minValue))) - Math.toRadians(this.overlap);
		// Left
		let x = (this.canvas.width / 2) - ((radius * 0.05) * Math.cos((___currentAngle - (Math.PI / 2))));
		let y = (radius + 10) - ((radius * 0.05) * Math.sin((___currentAngle - (Math.PI / 2))));
		context.lineTo(x, y);
		// Tip
		x = (this.canvas.width / 2) - ((radius * 0.90) * Math.cos(___currentAngle));
		y = (radius + 10) - ((radius * 0.90) * Math.sin(___currentAngle));
		context.lineTo(x, y);
		// Right
		x = (this.canvas.width / 2) - ((radius * 0.05) * Math.cos((___currentAngle + (Math.PI / 2))));
		y = (radius + 10) - ((radius * 0.05) * Math.sin((___currentAngle + (Math.PI / 2))));
		context.lineTo(x, y);

		context.closePath();
		context.fillStyle = this.analogDisplayColorConfig.handColor;
		context.fill();
		context.lineWidth = 1;
		context.strokeStyle = this.analogDisplayColorConfig.handOutlineColor;
		context.stroke();
		// Knob
		context.beginPath();
		context.arc((this.canvas.width / 2), (radius + 10), 7, 0, 2 * Math.PI, false);
		context.closePath();
		context.fillStyle = this.analogDisplayColorConfig.knobColor;
		context.fill();
		context.strokeStyle = this.analogDisplayColorConfig.knobOutlineColor;
		context.stroke();
	}
}

// Associate the tag and the class
window.customElements.define(ANALOG_TAG_NAME, AnalogDisplay);
