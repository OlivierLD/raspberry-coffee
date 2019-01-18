const windAngleVerbose = false;
const WIND_ANGLE_TAG_NAME = 'wind-angle-display';

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
const defaultWindAngleDisplayColorConfig = {
	bgColor: 'rgba(0, 0, 0, 0)', /* transparent, 'white', TODO */
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
	valueNbDecimal: 0,
	handColor: 'red', // 'rgba(0, 0, 100, 0.25)',
	handOutlineColor: 'black',
	withHandShadow: true,
	knobColor: 'DarkGrey',
	knobOutlineColor: 'black',
	font: 'Arial', /* 'Source Code Pro' */
	valueFontSizeFactor: 1
};

import * as Utilities from "./utilities/Utilities.js";

/* global HTMLElement */
class WindAngleDisplay extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",            // Integer. Canvas width
			"height",           // Integer. Canvas height
			"major-ticks",      // Float. value between major ticks (those with labels)
			"minor-ticks",      // Float. value between minor ticks
			"with-digits",      // Boolean, default true. Index Values for major-ticks
			"with-border",      // Boolean, default true
			"label",            // String. Displayed under the know (like 'App Wind')
			"hand",             // String. 'regular' (default) or 'wind'
			"value"             // JSON Obj. Value to display, like { wa: 0, ws: 0 }
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		let fallbackElemt = document.createElement("h1");
		let content = document.createTextNode("This is a Wind Angle Display, on an HTML5 canvas");
		fallbackElemt.appendChild(content);
		this.canvas.appendChild(fallbackElemt);
		this.shadowRoot.appendChild(this.canvas);

		// Default values
		this._value            = { wa: 0, ws: 0 };
		this._width            = 200;
		this._height           = 200;
		this._major_ticks      =  45;
		this._minor_ticks      =   5;
		this._with_digits      = false;
		this._with_border      = true;
		this._hand             = 'regular';
		this._label            = undefined;

		this._previousClassName = "";
		this.analogDisplayColorConfig = defaultWindAngleDisplayColorConfig; // Init

		if (windAngleVerbose) {
			console.log("Data in Constructor:", this._value);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (windAngleVerbose) {
			console.log("connectedCallback invoked, 'value' is [", this.value, "]");
		}
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (windAngleVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (windAngleVerbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "value":
				this._value = JSON.parse(newVal);
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
			case "with-digits":
				this._with_digits = ("true" === newVal);
				break;
			case "with-border":
				this._with_border = ("true" === newVal);
				break;
			case "label":
				this._label = newVal;
				break;
			case "hand":
				this._hand = (newVal === 'wind' ? 'wind' : 'regular');
				break;
			default:
				break;
		}
		this.repaint();
	}

	// Called whenever the custom element has been moved into a new document.
	adoptedCallback() {
		if (windAngleVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set value(option) {
		this.setAttribute("value", option);
		if (windAngleVerbose) {
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
	set majorTicks(val) {
		this.setAttribute("major-ticks", val);
	}
	set minorTicks(val) {
		this.setAttribute("minor-ticks", val);
	}
	set withDigits(val) {
		this.setAttribute("with-digits", val);
	}
	set withBorder(val) {
		this.setAttribute("with-border", val);
	}
	set label(val) {
		this.setAttribute("label", val);
	}
	set hand(val) {
		this.setAttribute("hand", val);
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
	get minorTicks() {
		return this._minor_ticks;
	}
	get majorTicks() {
		return this._major_ticks;
	}
	get withDigits() {
		return this._with_digits;
	}
	get withBorder() {
		return this._with_border;
	}
	get label() {
		return this._label;
	}
	get hand() {
		return this._hand;
	}
	get shadowRoot() {
		return this._shadowRoot;
	}

	// Component methods
	repaint() {
		this.drawDisplay(this._value);
	}

	getColorConfig(classNames) {
		let colorConfig = defaultWindAngleDisplayColorConfig;
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


	drawDisplay(windValue) {

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
		context.clearRect(0, 0, this._width, this._height);

		let radius = 0.9 * Math.min(this._width, this._height) / 2;

		// Set the canvas size from its container.
		this.canvas.width = this._width;
		this.canvas.height = this._height;

		context.beginPath();

		if (this._with_border === true) {
			//  context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);
			context.arc(this.canvas.width / 2, radius + 10, radius, 0, 2 * Math.PI, false);
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

		// Major Ticks
		context.beginPath();
		for (let i = 0; i <= 360; i++) {
			if (i % this.majorTicks === 0) {
				let currentAngle = Utilities.toRadians(i);
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
			for (let i = 0; i <= 360; i += this.minorTicks) {
				let _currentAngle = Utilities.toRadians(i);
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

		let scale = radius / 100;
		// Numbers (indexes) on major ticks
		if (this._with_digits) {
			context.beginPath();
			for (let i = 0; i < 360; i += this.majorTicks) {
				context.save();
				context.translate(this.canvas.width / 2, (radius + 10)); // canvas.height);
				context.rotate((2 * Math.PI * (i / 360)));
				context.font = "bold " + Math.round(scale * 10) + "px " + this.analogDisplayColorConfig.font; // Like "bold 15px Arial"
				context.fillStyle = digitColor;
				let angle = i;
				if (angle > 180) {// [-180..+180] , with no sign.
					angle = 360 - angle;
				}
				let str = angle.toString();
				let len = context.measureText(str).width;
				context.fillText(str, -len / 2, (-(radius * .8) + 10));
				// TODO Make sure we want this
				context.lineWidth = 1;
				context.strokeStyle = this.analogDisplayColorConfig.valueOutlineColor;
				context.strokeText(str, -len / 2, (-(radius * .8) + 10)); // Outlined
				context.restore();
			}
			context.closePath();
		}

		// Arcs
		{
			context.beginPath();
			let x = this.canvas.width / 2;
			let y = this.canvas.height / 2;
			context.lineWidth = 20;
			let top = 1.5 * Math.PI;
			let arcWidth = Utilities.toRadians(120);

			// Starboard
			if (this.analogDisplayColorConfig.outlinedPortStarboard === true) {
				context.beginPath();
				context.lineWidth = 2;
				context.strokeStyle = 'rgba(0, 255, 0, 0.75)';
				context.arc(x, y, radius * .75, 1.5 * Math.PI, top + arcWidth, false);
				context.arc(x, y, radius * .55, top + arcWidth, 1.5 * Math.PI, true);
				context.lineTo(x, y - (radius * .75));
				context.stroke();
				context.closePath();
			} else {
				context.beginPath();
				context.strokeStyle = 'rgba(0, 255, 0, 0.25)';
				context.arc(x, y, radius * .75, 1.5 * Math.PI, top + arcWidth, false);
				context.stroke();
				context.closePath();
			}
			// Port
			if (this.analogDisplayColorConfig.outlinedPortStarboard === true) {
				context.beginPath();
				context.lineWidth = 2;
				context.strokeStyle = 'rgba(255, 0, 0, 0.75)';
				context.arc(x, y, radius * .75, 1.5 * Math.PI, top - arcWidth, true);
				context.arc(x, y, radius * .55, top - arcWidth, 1.5 * Math.PI, false);
				context.lineTo(x, y - (radius * .75));
				context.stroke();
				context.closePath();
			} else {
				context.beginPath();
				context.strokeStyle = 'rgba(255, 0, 0, 0.25)';
				context.arc(x, y, radius * .75, 1.5 * Math.PI, top - arcWidth, true);
				context.stroke();
				context.closePath();
			}
		}

		// Value
		let text = windValue.ws.toFixed(this.analogDisplayColorConfig.valueNbDecimal);
		let len = 0;
		context.font = "bold " + Math.round(scale * 40 * this.analogDisplayColorConfig.valueFontSizeFactor) + "px " + this.analogDisplayColorConfig.font; // "bold 40px Arial"
		let metrics = context.measureText(text);
		len = metrics.width;

		context.beginPath();
		context.fillStyle = this.analogDisplayColorConfig.valueColor;
		context.fillText(text, (this.canvas.width / 2) - (len / 2), ((radius * .85) + 10));
		context.lineWidth = 1;
		context.strokeStyle = this.analogDisplayColorConfig.valueOutlineColor;
		context.strokeText(text, (this.canvas.width / 2) - (len / 2), ((radius * .85) + 10)); // Outlined
		context.closePath();

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
			context.fillText(text, (this.canvas.width / 2) - (len / 2), (2 * radius - (fontSize * scale * 2.1)));
			context.lineWidth = 1;
			context.strokeStyle = this.analogDisplayColorConfig.valueOutlineColor;
			context.strokeText(text, (this.canvas.width / 2) - (len / 2), (2 * radius - (fontSize * scale * 2.1))); // Outlined
			context.closePath();
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
		let centerX = this.canvas.width / 2;
		let centerY = radius + 10;
		context.moveTo(centerX, centerY);

		// Left
		let x = centerX - ((radius * 0.05) * Math.cos(Utilities.toRadians(windValue.wa))); //  - (Math.PI / 2))));
		let y = centerY - ((radius * 0.05) * Math.sin(Utilities.toRadians(windValue.wa))); // - (Math.PI / 2))));
		context.lineTo(x, y);
		if (this.hand !== 'wind') { // Regular needle
			// Tip
			x = centerX - ((radius * 0.90) * Math.cos(Utilities.toRadians(windValue.wa) + (Math.PI / 2)));
			y = centerY - ((radius * 0.90) * Math.sin(Utilities.toRadians(windValue.wa) + (Math.PI / 2)));
			context.lineTo(x, y);
		} else {                    // Then draw wind arrow
			/*
			    +-+
			    | |
			    | |
			  +-+ +-+
			   \   /
			    + +
			    | |
			    | |
			    +-+
			 */

			let arrowPoints = [
				{ x: - radius * 0.04, y: - radius * 0.30 }, // Left pointy side of the arrow head
				{ x: - radius * 0.20, y: - radius * 0.60 }, // Left back fat side of the arrow head
				{ x: - radius * 0.04, y: - radius * 0.60 }, // Left back narrow side of the arrow head
				{ x: - radius * 0.04, y: - radius * 0.90 }, // Left tip
				{ x: + radius * 0.04, y: - radius * 0.90 }, // Right tip
				{ x: + radius * 0.04, y: - radius * 0.60 }, // Right back narrow side of the arrow head
				{ x: + radius * 0.20, y: - radius * 0.60 }, // Right back fat side of the arrow head
				{ x: + radius * 0.04, y: - radius * 0.30 }  // Right pointy side of the arrow head
			];
			let radAngle = Utilities.toRadians(windValue.wa); // + (Math.PI / 2);
			// Apply rotation to the points of the needle
			arrowPoints.forEach(pt => {
				x = centerX + ((pt.x * Math.cos(radAngle)) - (pt.y * Math.sin(radAngle)));
				y = centerY + ((pt.x * Math.sin(radAngle)) + (pt.y * Math.cos(radAngle)));
				context.lineTo(x, y);
			});
		}
		// Right
		x = centerX - ((radius * 0.05) * Math.cos(Utilities.toRadians(windValue.wa) + (2 * Math.PI / 2)));
		y = centerY - ((radius * 0.05) * Math.sin(Utilities.toRadians(windValue.wa) + (2 * Math.PI / 2)));
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
window.customElements.define(WIND_ANGLE_TAG_NAME, WindAngleDisplay);
