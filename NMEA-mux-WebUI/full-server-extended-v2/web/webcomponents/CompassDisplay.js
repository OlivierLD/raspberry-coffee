const compassDisplayVerbose = false;
const COMPASS_DISPLAY_TAG_NAME = 'compass-display';
/*
* The current heading always show on top in this version.
*
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
const defaultCompassDisplayColorConfig = {
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
	valueColor: 'grey',
	valueOutlineColor: 'black',
	valueNbDecimal: 0,
	crosshairColor: 'red', // 'rgba(0, 0, 100, 0.25)',
	knobColor: 'DarkGrey',
	knobOutlineColor: 'black',
	font: 'Arial' /* 'Source Code Pro' */
};

// import * as Utilities from "../utilities/Utilities.js";

/* global HTMLElement */
class CompassDisplay extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",        // Integer. Canvas width
			"height",       // Integer. Canvas height
			"major-ticks",  // Float. value between major ticks (those with labels)
			"minor-ticks",  // Float. value between minor ticks
			"with-rose",    // Boolean, draw the rose or not
			"with-border",  // Boolean
			"label",        // String. HDG, COG, etc
			"value"         // Float. Heading to display
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		let fallbackElemt = document.createElement("h1");
		let content = document.createTextNode("This is a CompassDisplay, on an HTML5 canvas");
		fallbackElemt.appendChild(content);
		this.canvas.appendChild(fallbackElemt);
		this.shadowRoot.appendChild(this.canvas);

		// Default values
		this._value       =   0;
		this._width       = 150;
		this._height      = 150;
		this._major_ticks =  45;
		this._minor_ticks =   5;
		this._with_rose   = true;
		this._with_border = true;
		this._label       = undefined;

		this._previousClassName = "";
		this.compassDisplayColorConfig = defaultCompassDisplayColorConfig; // Init

		if (compassDisplayVerbose) {
			console.log("Data in Constructor:", this._value);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (compassDisplayVerbose) {
			console.log("connectedCallback invoked, 'value' is [", this.value, "]");
		}
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (compassDisplayVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (compassDisplayVerbose) {
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
			case "major-ticks":
				this._major_ticks = parseFloat(newVal);
				break;
			case "minor-ticks":
				this._minor_ticks = parseFloat(newVal);
				break;
			case "with-rose":
				this._with_rose = ("true" === newVal);
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
		if (compassDisplayVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set value(option) {
		this.setAttribute("value", option);
		if (compassDisplayVerbose) {
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
	set withRose(val) {
		this.setAttribute("with-rose", val);
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
	get withRose() {
		return this._with_rose;
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
		let colorConfig = defaultCompassDisplayColorConfig;
		let classes = classNames.split(" ");
		for (let cls=0; cls<classes.length; cls++) {
			let className = classes[cls];
			for (let s=0; s<document.styleSheets.length; s++) {
				//		console.log("Walking though ", document.styleSheets[s]);
				try {
					for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
						let selector = document.styleSheets[s].cssRules[r].selectorText;
						//			console.log(">>> ", selector);
						if (selector !== undefined && (selector === '.' + className || (selector.indexOf('.' + className) > -1 && selector.indexOf(COMPASS_DISPLAY_TAG_NAME) > -1))) { // Cases like "tag-name .className"
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
										case '--cross-hair-color':
											colorConfig.crosshairColor = value;
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


	drawDisplay(directionValue) {

		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
			//	console.log("Reloading CSS");
			try {
				this.compassDisplayColorConfig = this.getColorConfig(currentStyle);
			} catch (err) {
				// Absorb?
				console.log(err);
			}
			this._previousClassName = currentStyle;
		}

		let digitColor = this.compassDisplayColorConfig.digitColor;

		let context = this.canvas.getContext('2d');
		context.clearRect(0, 0, this.width, this.height);

		let radius = (Math.min(this.width, this.height) / 2) * 0.90;

		// Set the canvas size from its container.
		this.canvas.width = this.width;
		this.canvas.height = this.height;

		// Cleanup
		context.fillStyle = this.compassDisplayColorConfig.bgColor;
		context.fillRect(0, 0, this.canvas.width, this.canvas.height);

		context.beginPath();
		if (this.withBorder === true) {
			//context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);
			context.arc(this.canvas.width / 2, radius + 10, radius, 0, 2 * Math.PI, false);
			context.lineWidth = 5;
		}
		if (this.compassDisplayColorConfig.withGradient) {
			let grd = context.createLinearGradient(0, 5, 0, radius);
			grd.addColorStop(0, this.compassDisplayColorConfig.displayBackgroundGradient.from);// 0  Beginning
			grd.addColorStop(1, this.compassDisplayColorConfig.displayBackgroundGradient.to);  // 1  End
			context.fillStyle = grd;
		} else {
			context.fillStyle = this.compassDisplayColorConfig.displayBackgroundGradient.to;
		}

		if (this.compassDisplayColorConfig.withDisplayShadow) {
			context.shadowOffsetX = 3;
			context.shadowOffsetY = 3;
			context.shadowBlur = 3;
			context.shadowColor = this.compassDisplayColorConfig.shadowColor;
		} else {
			context.shadowOffsetX = 0;
			context.shadowOffsetY = 0;
			context.shadowBlur = 0;
			context.shadowColor = undefined;
		}
		context.lineJoin = "round";
		context.fill();
		context.strokeStyle = this.compassDisplayColorConfig.outlineColor;
		context.stroke();
		context.closePath();

		// Major Ticks
		context.beginPath();
		for (let i = 0; i < 360; i += this.majorTicks) {
			let heading = i - this._value;
			let xFrom = (this.canvas.width / 2) - ((radius * 0.95) * Math.cos(2 * Math.PI * (heading / 360)));
			let yFrom = (radius + 10) - ((radius * 0.95) * Math.sin(2 * Math.PI * (heading / 360)));
			let xTo = (this.canvas.width / 2) - ((radius * 0.85) * Math.cos(2 * Math.PI * (heading / 360)));
			let yTo = (radius + 10) - ((radius * 0.85) * Math.sin(2 * Math.PI * (heading / 360)));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
		}
		context.lineWidth = 3;
		context.strokeStyle = this.compassDisplayColorConfig.majorTickColor;
		context.stroke();
		context.closePath();

		// Minor Ticks
		if (this.minorTicks > 0) {
			context.beginPath();
			for (let i = 0; i <= 360; i += this.minorTicks) {
				let heading = i - this._value;
				let xFrom = (this.canvas.width / 2) - ((radius * 0.95) * Math.cos(2 * Math.PI * (heading / 360)));
				let yFrom = (radius + 10) - ((radius * 0.95) * Math.sin(2 * Math.PI * (heading / 360)));
				let xTo = (this.canvas.width / 2) - ((radius * 0.90) * Math.cos(2 * Math.PI * (heading / 360)));
				let yTo = (radius + 10) - ((radius * 0.90) * Math.sin(2 * Math.PI * (heading / 360)));
				context.moveTo(xFrom, yFrom);
				context.lineTo(xTo, yTo);
			}
			context.lineWidth = 1;
			context.strokeStyle = this.compassDisplayColorConfig.minorTickColor;
			context.stroke();
			context.closePath();
		}

		// with rose?
		if (this.withRose === true) {
			context.beginPath();

			context.lineWidth = 1;
			let outsideRadius = radius * 0.6;
			let insideRadius = radius * 0.1;

//    context.arc(canvas.width / 2, radius + 10, outsideRadius, 0, 2 * Math.PI, false);
//    context.arc(canvas.width / 2, radius + 10, insideRadius,  0, 2 * Math.PI, false);

			// NS/EW axis, the origin is -90 (W)
			let N = (0 + 90 - this._value) % 360;
			let S = (180 + 90 - this._value) % 360;
			let E = (90 + 90 - this._value) % 360;
			let W = (270 + 90 - this._value) % 360;

			let NE = (45 + 90 - this._value) % 360;
			let SE = (135 + 90 - this._value) % 360;
			let NW = (315 + 90 - this._value) % 360;
			let SW = (225 + 90 - this._value) % 360;

			let smallFactor = 0.8;

			// N-S
			let xFrom = (this.canvas.width / 2) - (outsideRadius * Math.cos(2 * Math.PI * (N / 360)));
			let yFrom = (radius + 10) - (outsideRadius * Math.sin(2 * Math.PI * (N / 360)));
			let xTo = (this.canvas.width / 2) - (outsideRadius * Math.cos(2 * Math.PI * (S / 360)));
			let yTo = (radius + 10) - (outsideRadius * Math.sin(2 * Math.PI * (S / 360)));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
			// E-W
			xFrom = (this.canvas.width / 2) - (outsideRadius * Math.cos(2 * Math.PI * (E / 360)));
			yFrom = (radius + 10) - (outsideRadius * Math.sin(2 * Math.PI * (E / 360)));
			xTo = (this.canvas.width / 2) - (outsideRadius * Math.cos(2 * Math.PI * (W / 360)));
			yTo = (radius + 10) - (outsideRadius * Math.sin(2 * Math.PI * (W / 360)));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
			// NE-SW
			xFrom = (this.canvas.width / 2) - (outsideRadius * smallFactor * Math.cos(2 * Math.PI * (NE / 360)));
			yFrom = (radius + 10) - (outsideRadius * smallFactor * Math.sin(2 * Math.PI * (NE / 360)));
			xTo = (this.canvas.width / 2) - (outsideRadius * smallFactor * Math.cos(2 * Math.PI * (SW / 360)));
			yTo = (radius + 10) - (outsideRadius * smallFactor * Math.sin(2 * Math.PI * (SW / 360)));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
			// NW-SE
			xFrom = (this.canvas.width / 2) - (outsideRadius * smallFactor * Math.cos(2 * Math.PI * (NW / 360)));
			yFrom = (radius + 10) - (outsideRadius * smallFactor * Math.sin(2 * Math.PI * (NW / 360)));
			xTo = (this.canvas.width / 2) - (outsideRadius * smallFactor * Math.cos(2 * Math.PI * (SE / 360)));
			yTo = (radius + 10) - (outsideRadius * smallFactor * Math.sin(2 * Math.PI * (SE / 360)));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);

			this.drawSpike(radius, outsideRadius, insideRadius, N, context);
			this.drawSpike(radius, outsideRadius, insideRadius, S, context);
			this.drawSpike(radius, outsideRadius, insideRadius, E, context);
			this.drawSpike(radius, outsideRadius, insideRadius, W, context);

			this.drawSpike(radius, outsideRadius * smallFactor, insideRadius, NE, context);
			this.drawSpike(radius, outsideRadius * smallFactor, insideRadius, SE, context);
			this.drawSpike(radius, outsideRadius * smallFactor, insideRadius, SW, context);
			this.drawSpike(radius, outsideRadius * smallFactor, insideRadius, NW, context);

			context.strokeStyle = this.compassDisplayColorConfig.displayLineColor;
			context.stroke();
			context.closePath();
		}

		// Numbers
		context.beginPath();
		let scale = 1;
		for (let i = 0; i < 360; i += this.majorTicks) {
			let heading = i - this._value;
			context.save();
			context.translate(this.canvas.width / 2, (radius + 10)); // canvas.height);
			context.rotate((2 * Math.PI * (heading / 360)));
			context.font = "bold " + Math.round(scale * 15) + "px Arial"; // Like "bold 15px Arial"
			context.fillStyle = digitColor;
			let str = i.toString();
			let len = context.measureText(str).width;
			context.fillText(str, -len / 2, (-(radius * .8) + 10));
			context.lineWidth = 1;
			context.strokeStyle = this.compassDisplayColorConfig.valueOutlineColor;
			context.strokeText(str, -len / 2, (-(radius * .8) + 10)); // Outlined
			context.restore();
		}
		context.closePath();
		// Value
		let text;
		let dv = parseFloat(directionValue); // TODO Redundant with this._value ?
		while (dv > 360) dv -= 360;
		while (dv < 0) dv += 360;
		try {
			text = dv.toFixed(this.compassDisplayColorConfig.valueNbDecimal);
		} catch (err) {
			console.log(err);
		}
		let len = 0;
		context.font = "bold " + Math.round(scale * 40) + "px " + this.compassDisplayColorConfig.font; // "bold 40px Arial"
		let metrics = context.measureText(text);
		len = metrics.width;

		context.beginPath();
		context.fillStyle = this.compassDisplayColorConfig.valueColor;
		context.fillText(text, (this.canvas.width / 2) - (len / 2), ((radius * .75) + 10));
		context.lineWidth = 1;
		context.strokeStyle = this.compassDisplayColorConfig.valueOutlineColor;
		context.strokeText(text, (this.canvas.width / 2) - (len / 2), ((radius * .75) + 10)); // Outlined
		context.closePath();

		// Label ?
		if (this.label !== undefined) {
			let fontSize = 20;
			let text = this.label;
			let len = 0;
			context.font = "bold " + Math.round(scale * fontSize) + "px " + this.compassDisplayColorConfig.font; // "bold 40px Arial"
			let metrics = context.measureText(text);
			len = metrics.width;

			context.beginPath();
			context.fillStyle = this.compassDisplayColorConfig.labelFillColor;
			context.fillText(text, (this.canvas.width / 2) - (len / 2), (2 * radius - (fontSize * scale * 2.1)));
			context.lineWidth = 1;
			context.strokeStyle = this.compassDisplayColorConfig.valueOutlineColor;
			context.strokeText(text, (this.canvas.width / 2) - (len / 2), (2 * radius - (fontSize * scale * 2.1))); // Outlined
			context.closePath();
		}

		// No Hand, cross-hair, fixed.
		context.beginPath();
		context.lineWidth = 1;
		context.strokeStyle = this.compassDisplayColorConfig.crosshairColor;
		// Left
		context.moveTo((this.canvas.width / 2) - 3, (radius + 10));
		context.lineTo((this.canvas.width / 2) - 3, 10);
		context.stroke();
		// Right
		context.moveTo((this.canvas.width / 2) + 3, (radius + 10));
		context.lineTo((this.canvas.width / 2) + 3, 10);
		context.stroke();

		context.closePath();

		// Knob
		context.beginPath();
		context.arc((this.canvas.width / 2), (radius + 10), 7, 0, 2 * Math.PI, false);
		context.closePath();
		context.fillStyle = this.compassDisplayColorConfig.knobColor;
		context.fill();
		context.strokeStyle = this.compassDisplayColorConfig.knobOutlineColor;
		context.stroke();
	}

	drawSpike(radius, outsideRadius, insideRadius, angle, context) {
		let xFrom = (this.canvas.width / 2) - (outsideRadius * Math.cos(2 * Math.PI * (angle / 360)));
		let yFrom = (radius + 10) - (outsideRadius * Math.sin(2 * Math.PI * (angle / 360)));
		//
		let xTo = (this.canvas.width / 2) - (insideRadius * Math.cos(2 * Math.PI * ((angle - 90) / 360)));
		let yTo = (radius + 10) - (insideRadius * Math.sin(2 * Math.PI * ((angle - 90) / 360)));
		context.moveTo(xFrom, yFrom);
		context.lineTo(xTo, yTo);
		//
		xTo = (this.canvas.width / 2) - (insideRadius * Math.cos(2 * Math.PI * ((angle + 90) / 360)));
		yTo = (radius + 10) - (insideRadius * Math.sin(2 * Math.PI * ((angle + 90) / 360)));
		context.moveTo(xFrom, yFrom);
		context.lineTo(xTo, yTo);
	}

}

// Associate the tag and the class
window.customElements.define(COMPASS_DISPLAY_TAG_NAME, CompassDisplay);
