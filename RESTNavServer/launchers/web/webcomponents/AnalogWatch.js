const watchVerbose = false;
const WATCH_TAG_NAME = 'analog-watch';
/**
 *
 * See custom properties in CSS.
 * =============================
 * @see https://developer.mozilla.org/en-US/docs/Web/CSS/
 * Relies on those elements:
 *
.xxxxxxxxx {
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
	--hand-color: red;
	--hand-outline-color: black;
	--with-hand-shadow: true;
	--knob-color: DarkGrey;
	--knob-outline-color: black;
	--font: Arial;
}
 */
import * as Utilities from "./utilities/Utilities.js";

/**
 * Recurse from the top down, on styleSheets and cssRules
 *
 * document.styleSheets[0].cssRules[2].selectorText returns ".analogdisplay"
 * document.styleSheets[0].cssRules[2].cssText returns ".analogdisplay { --hand-color: red;  --face-color: white; }"
 * document.styleSheets[0].cssRules[2].style.cssText returns "--hand-color: red; --face-color: white;"
 *
 * spine-case to camelCase
 */
const defaultWatchColorConfig = {
	bgColor: 'rgba(0, 0, 0, 0)', /* transparent, 'white', */
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
	handColor: 'red', // 'rgba(0, 0, 100, 0.25)',
	handOutlineColor: 'black',
	withHandShadow: true,
	knobColor: 'DarkGrey',
	knobOutlineColor: 'black',
	font: 'Arial' /* 'Source Code Pro' */
};

/* global HTMLElement */
class AnalogWatch extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",            // Integer. Canvas width
			"height",           // Integer. Canvas height
			"hours-ticks",      // Integer. label of hours. 1: each hour, 3: every 3 hours, etc. Default 3
			"hours-flavor",     // String. 'roman' or 'arabic'. Default 'roman'
			"minutes-ticks",    // Integer, minutes ticks.  Default 1
			"with-second-hand", // Boolean, draw the seconds hand or not.
			"with-border",      // Boolean
			"digital-value",    // Integer 0, 4 or 6. 0 (default value) means no display, 4 means like 12:34, 6 means like 12:34:56
			"label",            // String, Optional.
			"value"             // Float. Time to display, HH:MM:SS format
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		this.shadowRoot.appendChild(this.canvas);

		// Default values
		this._value            = '00:00:00';
		this._digital_value    =   0; // none
		this._hours_flavor     = 'roman';
		this._width            = 150;
		this._height           = 150;
		this._hours_ticks      =   3;
		this._minutes_ticks    =   1;
		this._with_second_hand = false;
		this._with_border      = true;
		this._label            = undefined;

		this._previousClassName = "";
		this.watchColorConfig = defaultWatchColorConfig; // Init

		if (watchVerbose) {
			console.log("Data in Constructor:", this._value);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (watchVerbose) {
			console.log("connectedCallback invoked, 'value' is [", this.value, "]");
		}
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (watchVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (watchVerbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "value":
				this._value = newVal;
				break;
			case "digital-value":
				this._digital_value = parseInt(newVal);
				break;
			case "hours-flavor":
				this._hours_flavor = newVal;
				break;
			case "width":
				this._width = parseInt(newVal);
				break;
			case "height":
				this._height = parseInt(newVal);
				break;
			case "hours-ticks":
				this._hours_ticks = parseInt(newVal);
				break;
			case "minutes-ticks":
				this._minutes_ticks = parseInt(newVal);
				break;
			case "with-second-hand":
				this._with_second_hand = ("true" === newVal);
				break;
			case "with-border":
				this._with_border = ("true" === newVal);
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
		if (watchVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set value(option) {
		this.setAttribute("value", option);
		if (watchVerbose) {
			console.log(">> Value option:", option);
		}
//	this.repaint();
	}
	set digitalValue(val) {
		this._digital_value = val;
	}
	set hoursFlavor(val) {
		this._hours_flavor = val;
	}
	set width(val) {
		this.setAttribute("width", val);
	}
	set height(val) {
		this.setAttribute("height", val);
	}
	set hoursTicks(val) {
		this.setAttribute("hours-ticks", val);
	}
	set minutesTicks(val) {
		this.setAttribute("minutes-ticks", val);
	}
	set withSecondHand(val) {
		this.setAttribute("with-second-hand", val);
	}
	set withBorder(val) {
		this.setAttribute("with-border", val);
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
	get digitalValue() {
		return this._digital_value;
	}
	get hoursFlavor() {
		return this._hours_flavor;
	}
	get width() {
		return this._width;
	}
	get height() {
		return this._height;
	}
	get minutesTicks() {
		return this._minutes_ticks;
	}
	get hoursTicks() {
		return this._hours_ticks;
	}
	get withSecondHand() {
		return this._with_second_hand;
	}
	get withBorder() {
		return this._with_border;
	}
	get label() {
		return this._label;
	}
	get shadowRoot() {
		return this._shadowRoot;
	}

	// Component methods
	repaint() {
		this.drawDisplay(this._value);
	}

	getColorConfig(classNames) {
		let colorConfig = defaultWatchColorConfig;
		let classes = classNames.split(" ");
		for (let cls=0; cls<classes.length; cls++) {
			let className = classes[cls];
			for (let s=0; s<document.styleSheets.length; s++) {
	//		console.log("Walking though ", document.styleSheets[s]);
				try {
					for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
						let selector = document.styleSheets[s].cssRules[r].selectorText;
		//			console.log(">>> ", selector);
						if (selector !== undefined && (selector === '.' + className || (selector.indexOf('.' + className) > -1 && selector.indexOf(WATCH_TAG_NAME) > -1))) { // Cases like "tag-name .className"
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

	/**
	 *
	 * @param timeValue, format HH:MM:SS
	 */
	drawDisplay(timeValue) {

		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
			//	console.log("Reloading CSS");
			try {
				this.watchColorConfig = this.getColorConfig(currentStyle);
			} catch (err) {
				// Absorb?
				console.log(err);
			}
			this._previousClassName = currentStyle;
		}

		let digitColor = this.watchColorConfig.digitColor;

		let context = this.canvas.getContext('2d');
		context.clearRect(0, 0, this.width, this.height);

		let radius = (Math.min(this.width, this.height) / 2) * 0.90;

		// Set the canvas size from its container.
		this.canvas.width = this.width;
		this.canvas.height = this.height;

		let scale = radius / 100;
		// Cleanup
		context.fillStyle = this.watchColorConfig.bgColor;
		context.fillRect(0, 0, this.canvas.width, this.canvas.height);

		context.beginPath();
		if (this.withBorder === true) {
			//context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);
			context.arc(this.canvas.width / 2, radius + 10, radius, 0, 2 * Math.PI, false);
			context.lineWidth = 5;
		}
		if (this.watchColorConfig.withGradient) {
			let grd = context.createLinearGradient(0, 5, 0, radius);
			grd.addColorStop(0, this.watchColorConfig.displayBackgroundGradient.from);// 0  Beginning
			grd.addColorStop(1, this.watchColorConfig.displayBackgroundGradient.to);  // 1  End
			context.fillStyle = grd;
		} else {
			context.fillStyle = this.watchColorConfig.displayBackgroundGradient.to;
		}

		if (this.watchColorConfig.withDisplayShadow) {
			context.shadowOffsetX = 3;
			context.shadowOffsetY = 3;
			context.shadowBlur = 3;
			context.shadowColor = this.watchColorConfig.shadowColor;
		} else {
			context.shadowOffsetX = 0;
			context.shadowOffsetY = 0;
			context.shadowBlur = 0;
			context.shadowColor = undefined;
		}
		context.lineJoin = "round";
		context.fill();
		context.strokeStyle = this.watchColorConfig.outlineColor;
		context.stroke();
		context.closePath();

		// Hour ticks
		context.beginPath();
		for (let i = 0; i < 12; i += 1) {
			let xFrom = (this.canvas.width / 2) - ((radius * 0.95) * Math.cos(2 * Math.PI * (i / 12)));
			let yFrom = (radius + 10) - ((radius * 0.95) * Math.sin(2 * Math.PI * (i / 12)));
			let xTo = (this.canvas.width / 2) - ((radius * 0.85) * Math.cos(2 * Math.PI * (i / 12)));
			let yTo = (radius + 10) - ((radius * 0.85) * Math.sin(2 * Math.PI * (i / 12)));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
		}
		context.lineWidth = 3;
		context.strokeStyle = this.watchColorConfig.majorTickColor;
		context.stroke();
		context.closePath();

		// Minutes Ticks
		if (this.minutesTicks > 0) {
			context.beginPath();
			for (let i = 0; i < 60; i += this.minutesTicks) {
				let xFrom = (this.canvas.width / 2) - ((radius * 0.95) * Math.cos(2 * Math.PI * (i / 60)));
				let yFrom = (radius + 10) - ((radius * 0.95) * Math.sin(2 * Math.PI * (i / 60)));
				let xTo = (this.canvas.width / 2) - ((radius * 0.90) * Math.cos(2 * Math.PI * (i / 60)));
				let yTo = (radius + 10) - ((radius * 0.90) * Math.sin(2 * Math.PI * (i / 60)));
				context.moveTo(xFrom, yFrom);
				context.lineTo(xTo, yTo);
			}
			context.lineWidth = 1;
			context.strokeStyle = this.watchColorConfig.minorTickColor;
			context.stroke();
			context.closePath();
		}

		// Numbers
		context.beginPath();
		for (let i = 0; i < 12; i += this.hoursTicks) {
			context.save();
			context.translate(this.canvas.width / 2, (radius + 10)); // canvas.height);
			context.rotate((2 * Math.PI * (i / 12)));
			context.font = "bold " + Math.round(scale * 15) + "px Arial"; // Like "bold 15px Arial"
			context.fillStyle = digitColor;
			let str = (this._hours_flavor === 'roman' ? this.toRomanDigit(i === 0 ? 12 : i) : ((i === 0 ? 12 : i).toString()));
			let len = context.measureText(str).width;
			context.fillText(str, -len / 2, (-(radius * .8) + 10));
			context.lineWidth = 1;
			context.strokeStyle = this.watchColorConfig.valueOutlineColor;
			context.strokeText(str, -len / 2, (-(radius * .8) + 10)); // Outlined
			context.restore();
		}
		context.closePath();
		// Value
		let timeElements = timeValue.split(":");
		let hours = 0, minutes = 0, seconds = 0;

		if (timeElements[0] !== undefined) {
			hours = parseInt(timeElements[0]);
		}
		if (timeElements[1] !== undefined) {
			minutes = parseInt(timeElements[1]);
		}
		if (timeElements[2] !== undefined) {
			seconds = parseInt(timeElements[2]);
		}

		// Digital value
		if (this._digital_value > 0) {
			let fontSize = 20;
			let text = Utilities.lpad(hours.toString(), 2, '0') + ':' + Utilities.lpad(minutes.toString(), 2, '0') + (this._digital_value === 6 ? ':' + Utilities.lpad(seconds.toString(), 2, '0') : '');

			let len = 0;
			context.font = "bold " + Math.round(scale * fontSize) + "px " + this.watchColorConfig.font; // "bold 40px Arial"
			let metrics = context.measureText(text);
			len = metrics.width;

			context.beginPath();
			context.fillStyle = this.watchColorConfig.labelFillColor;
			context.fillText(text, (this.canvas.width / 2) - (len / 2), (radius - (scale * 10)));
			context.lineWidth = 1;
			context.strokeStyle = this.watchColorConfig.valueOutlineColor;
			context.strokeText(text, (this.canvas.width / 2) - (len / 2), (radius - (scale * 10))); // Outlined
			context.closePath();
		}

		// Label ?
		if (this.label !== undefined) {
			let fontSize = 20;
			let text = this.label;
			let len = 0;
			context.font = "bold " + Math.round(scale * fontSize) + "px " + this.watchColorConfig.font; // "bold 40px Arial"
			let metrics = context.measureText(text);
			len = metrics.width;

			context.beginPath();
			context.fillStyle = this.watchColorConfig.labelFillColor;
			context.fillText(text, (this.canvas.width / 2) - (len / 2), (2 * radius - (fontSize * scale * 2.1)));
			context.lineWidth = 1;
			context.strokeStyle = this.watchColorConfig.valueOutlineColor;
			context.strokeText(text, (this.canvas.width / 2) - (len / 2), (2 * radius - (fontSize * scale * 2.1))); // Outlined
			context.closePath();
		}

		// Hours Hand
		{
			let hoursInDegrees = ((hours % 12) * 30) + (((minutes * 6) + (seconds / 10)) / 12);

//		console.log("Hours %f, %f, %f in degrees: %f", hours, minutes, seconds, hoursInDegrees);

			context.beginPath();
			if (this.watchColorConfig.withHandShadow) {
				context.shadowColor = this.watchColorConfig.shadowColor;
				context.shadowOffsetX = 3;
				context.shadowOffsetY = 3;
				context.shadowBlur = 3;
			}
			// Center
			context.moveTo(this.canvas.width / 2, radius + 10);
			// Left
			let x = (this.canvas.width / 2) - ((radius * 0.05) * Math.cos((2 * Math.PI * (hoursInDegrees / 360)))); //  - (Math.PI / 2))));
			let y = (radius + 10) - ((radius * 0.05) * Math.sin((2 * Math.PI * (hoursInDegrees / 360)))); // - (Math.PI / 2))));
			context.lineTo(x, y);
			// Tip
			x = (this.canvas.width / 2) - ((radius * 0.60) * Math.cos(2 * Math.PI * (hoursInDegrees / 360) + (Math.PI / 2)));
			y = (radius + 10) - ((radius * 0.60) * Math.sin(2 * Math.PI * (hoursInDegrees / 360) + (Math.PI / 2)));
			context.lineTo(x, y);
			// Right
			x = (this.canvas.width / 2) - ((radius * 0.05) * Math.cos((2 * Math.PI * (hoursInDegrees / 360) + (2 * Math.PI / 2))));
			y = (radius + 10) - ((radius * 0.05) * Math.sin((2 * Math.PI * (hoursInDegrees / 360) + (2 * Math.PI / 2))));
			context.lineTo(x, y);

			context.closePath();
			context.fillStyle = this.watchColorConfig.handColor;
			context.fill();
			context.lineWidth = 1;
			context.strokeStyle = this.watchColorConfig.handOutlineColor;
			context.stroke();
		}

		// Minutes Hand
		{
			let minInDegrees = ((minutes * 6) + (seconds / 10));

//		console.log("Minutes %d %d in degrees:%f", minutes, seconds, minInDegrees);

			context.beginPath();
			if (this.watchColorConfig.withHandShadow) {
				context.shadowColor = this.watchColorConfig.shadowColor;
				context.shadowOffsetX = 3;
				context.shadowOffsetY = 3;
				context.shadowBlur = 3;
			}
			// Center
			context.moveTo(this.canvas.width / 2, radius + 10);
			// Left
			let x = (this.canvas.width / 2) - ((radius * 0.05) * Math.cos((2 * Math.PI * (minInDegrees / 360)))); //  - (Math.PI / 2))));
			let y = (radius + 10) - ((radius * 0.05) * Math.sin((2 * Math.PI * (minInDegrees / 360)))); // - (Math.PI / 2))));
			context.lineTo(x, y);
			// Tip
			x = (this.canvas.width / 2) - ((radius * 0.80) * Math.cos(2 * Math.PI * (minInDegrees / 360) + (Math.PI / 2)));
			y = (radius + 10) - ((radius * 0.80) * Math.sin(2 * Math.PI * (minInDegrees / 360) + (Math.PI / 2)));
			context.lineTo(x, y);
			// Right
			x = (this.canvas.width / 2) - ((radius * 0.05) * Math.cos((2 * Math.PI * (minInDegrees / 360) + (2 * Math.PI / 2))));
			y = (radius + 10) - ((radius * 0.05) * Math.sin((2 * Math.PI * (minInDegrees / 360) + (2 * Math.PI / 2))));
			context.lineTo(x, y);

			context.closePath();
			context.fillStyle = this.watchColorConfig.handColor;
			context.fill();
			context.lineWidth = 1;
			context.strokeStyle = this.watchColorConfig.handOutlineColor;
			context.stroke();
		}

		// Second hand

//	console.log("ID:" + this.id + ", with seconds:" + this._with_second_hand);
		if (this._with_second_hand) {
			let secInDegrees = (seconds * 6);

//		console.log("Seconds %f in degrees: %f", seconds, secInDegrees);

			context.beginPath();
			if (this.watchColorConfig.withHandShadow) {
				context.shadowColor = this.watchColorConfig.shadowColor;
				context.shadowOffsetX = 3;
				context.shadowOffsetY = 3;
				context.shadowBlur = 3;
			}
			// Center
			context.moveTo(this.canvas.width / 2, radius + 10);
			// Tip
			let x = (this.canvas.width / 2) - ((radius * 0.95) * Math.cos(2 * Math.PI * (secInDegrees / 360) + (Math.PI / 2)));
			let y = (radius + 10) - ((radius * 0.95) * Math.sin(2 * Math.PI * (secInDegrees / 360) + (Math.PI / 2)));
			context.lineTo(x, y);

			context.closePath();
			// context.fillStyle = this.directionColorConfig.handColor;
			// context.fill();
			context.lineWidth = 2;
			context.strokeStyle = this.watchColorConfig.handOutlineColor;
			context.stroke();
		}

		// Knob
		context.beginPath();
		context.arc((this.canvas.width / 2), (radius + 10), 7, 0, 2 * Math.PI, false);
		context.closePath();
		context.fillStyle = this.watchColorConfig.knobColor;
		context.fill();
		context.strokeStyle = this.watchColorConfig.knobOutlineColor;
		context.stroke();
	}

	toRomanDigit(num) {
		let roman = "";
		switch (num) {
			case 1:
				roman = "I";
				break;
			case 2:
				roman = "II";
				break;
			case 3:
				roman = "III";
				break;
			case 4:
				roman = "IIII";
				break;
			case 5:
				roman = "V";
				break;
			case 6:
				roman = "VI";
				break;
			case 7:
				roman = "VII";
				break;
			case 8:
				roman = "VIII";
				break;
			case 9:
				roman = "IX";
				break;
			case 10:
				roman = "X";
				break;
			case 11:
				roman = "XI";
				break;
			case 0:
			case 12:
				roman = "XII";
				break;
		}
		return roman;
	}
}

// Associate the tag and the class
window.customElements.define(WATCH_TAG_NAME, AnalogWatch);
