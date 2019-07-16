/**
 * This is a Knob-and-Display Web Component.
 * It has several attributes: width, height, value, and label, driving a canvas.
 * They have default values, respectively: 250, 100, 0, 'VAL'
 * Attributes are exposed and can be modified externally (from JavaScript)
 * In addition, there is a CSS colors management as well.
 */

const knobVerbose = false;
const KNOB_TAG_NAME = 'knob-and-display';

import * as Utilities from "./Utilities.js";

const knobDefaultColorConfig = {
	displayBackgroundGradient: {
		from: 'gray',
		to: 'black'
	},
	knobRadialGradient: {
		from: 'black',
		to: 'silver'
	},
	indexRadialGradient: {
		from: 'black',
		to: 'white'
	},
	displayOutlineColor: 'silver',
	displayBackgroundColor: 'white',
	displayTicksColor: 'darkgray',
	needleOutlineColor: 'black',
	needleFillColor: 'red',
	tickColor: 'rgba(0, 0, 0, 0.9)',
	valueColor: 'cyan',
	valueNbDecimal: 2,
	labelFont: 'Courier New',
	labelColor: 'cyan',
	valueFont: 'Arial'
};

/* global HTMLElement */
class KnobAndDisplay extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",   // Integer. Canvas width
			"height",  // Integer. Canvas height
			"minimum", // Float.
			"maximum", // Float.
			"value",   // Float. Numeric value to display
			"label",   // String, like Weight, Bias, etc
			"ratio",   // Float, default 1. How many turns the knob has to make to go from minimum to maximum. Unused for now.
			"onchange" // function (callback,with value, when value is modified from UI
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		let fallbackElemt = document.createElement("h1");
		let content = document.createTextNode("This is a WebComponent Knob with Display, on an HTML5 canvas");
		fallbackElemt.appendChild(content);
		this.canvas.appendChild(fallbackElemt);
		this.shadowRoot.appendChild(this.canvas);

		// Default values
		this._value = 0;
		this._width = 250;
		this._height = 250;
		this._label = "";
		this._minimum = 0;
		this._maximum = 1;
		this._ratio = 1;
		this._onchange = null;

		this._mousedown = false;
		this._knobRadius = 0;
		this._indexRadius = 0;
		this._indexCenter = {};

		this._previousClassName = "";
		this.knobColorConfig = knobDefaultColorConfig;

		if (knobVerbose) {
			console.log("Data in Constructor:", this._value);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		let self = this;
		if (knobVerbose) {
			console.log("connectedCallback invoked, 'value' is [", this.value, "]");
		}
		this._shadowRoot.addEventListener("mousemove", function(event) {
			// console.log('listened to event', event);
			if (self._mousedown) {
				// If dragged within the knob
				let mousePos = {
					x: event.offsetX,
					y: event.offsetY
				};
				let center = {
					x: self.width / 2,
					y: self.height / 2
				};
				let deltaX = center.x - mousePos.x;
				let deltaY = mousePos.y - center.y;
				let mouseToCenter = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
				// console.log("Mouse to Center", mouseToCenter, "Knob Radius", self._knobRadius);
				if (mouseToCenter < self._knobRadius) {
					let mouseAngle = Utilities.getDir(deltaX, deltaY);
					let newValue = self._minimum + (mouseAngle * (self._maximum - self._minimum) / 360);
					// console.log('Dragging! Angle is %f => value: %f', mouseAngle, newValue);
					self.value = Math.min(Math.max(newValue, self._minimum), self._maximum);
					self.repaint();
					if (self._onchange !== null) {
						window[self._onchange](self.value);
					}
				}
			}
		});
		this._shadowRoot.addEventListener("mousedown", function(event) {
			// Is the mouse pressed in the index?
			let mousePos = {
				x: event.offsetX,
				y: event.offsetY
			};
			if (mousePos.x < self._indexCenter.x + self._indexRadius &&
					mousePos.x > self._indexCenter.x - self._indexRadius &&
					mousePos.y < self._indexCenter.y + self._indexRadius &&
					mousePos.y > self._indexCenter.y - self._indexRadius) {
				self._mousedown = true;
				// console.log("Mouse down on Index");
			}

		});
		this._shadowRoot.addEventListener("mouseup", function(event) {
			self._mousedown = false;
		});
		this.repaint();
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (knobVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (knobVerbose) {
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
			case "label":
				this._label = newVal;
				break;
			case "minimum":
				this._minimum = parseFloat(newVal);
				break;
			case "maximum":
				this._maximum = parseFloat(newVal);
				break;
			case "ratio":
				this._ratio = parseFloat(newVal);
				break;
			case "onchange":
				this._onchange = newVal;
				break;
			default:
				break;
		}
		this.repaint();
	}

	// Called whenever the custom element has been moved into a new document.
	adoptedCallback() {
		if (knobVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set value(option) {
		this.setAttribute("value", option);
		if (knobVerbose) {
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

	set minimum(option) {
		this.setAttribute("minimum", option);
		if (knobVerbose) {
			console.log(">> Minimum option:", option);
		}
	}

	set maximum(option) {
		this.setAttribute("maximum", option);
		if (knobVerbose) {
			console.log(">> Maximum option:", option);
		}
	}

	set ratio(option) {
		this.setAttribute("ratio", option);
		if (knobVerbose) {
			console.log(">> Ratio option:", option);
		}
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

	get minimum() {
		return this._minimum;
	}

	get maximum() {
		return this._maximum;
	}

	get ratio() {
		return this._ratio;
	}

	get shadowRoot() {
		return this._shadowRoot;
	}

	// Component methods
	getColorConfig(classNames) {
		let colorConfig = knobDefaultColorConfig;
		let classes = classNames.split(" ");
		for (let cls = 0; cls < classes.length; cls++) {
			let cssClassName = classes[cls];
			for (let s = 0; s < document.styleSheets.length; s++) {
				// console.log("Walking though ", document.styleSheets[s]);
				try {
					for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
						let selector = document.styleSheets[s].cssRules[r].selectorText;
						//			console.log(">>> ", selector);
						if (selector !== undefined && (selector === '.' + cssClassName || (selector.indexOf('.' + cssClassName) > -1 && selector.indexOf(KNOB_TAG_NAME) > -1))) { // Cases like "tag-name .className"
							//				console.log("  >>> Found it! [%s]", selector);
							let cssText = document.styleSheets[s].cssRules[r].style.cssText;
							let cssTextElems = cssText.split(";");
							cssTextElems.forEach((elem) => {
								if (elem.trim().length > 0) {
									let keyValPair = elem.split(":");
									let key = keyValPair[0].trim();
									let value = keyValPair[1].trim();
									switch (key) {
										case '--display-background-gradient-from':
											colorConfig.displayBackgroundGradient.from = value;
											break;
										case '--display-background-gradient-to':
											colorConfig.displayBackgroundGradient.to = value;
											break;
										case '--knob-radial-gradient-from':
											colorConfig.knobRadialGradient.from = value;
											break;
										case '--knob-radial-gradient-to':
											colorConfig.knobRadialGradient.to = value;
											break;
										case '--index-radial-gradient-from':
											colorConfig.indexRadialGradient.from = value;
											break;
										case '--index-radial-gradient-to':
											colorConfig.indexRadialGradient.to = value;
											break;
										case '--display-outline-color':
											colorConfig.displayOutlineColor = value;
											break;
										case '--display-background-color':
											colorConfig.displayBackgroundColor = value;
											break;
										case '--display-ticks-color':
											colorConfig.displayTicksColor = value;
											break;
										case '--needle-outline-color':
											colorConfig.needleOutlineColor = value;
											break;
										case '--needle-fill-color':
											colorConfig.needleFillColor = value;
											break;
										case '--tick-color':
											colorConfig.tickColor = value;
											break;
										case '--value-color':
											colorConfig.valueColor = value;
											break;
										case '--label-color':
											colorConfig.labelColor = value;
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
		this.drawKnob();
	}

	drawKnob() {
		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
			//	console.log("Reloading CSS");
			try {
				this.knobColorConfig = this.getColorConfig(currentStyle);
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
		grd.addColorStop(0, this.knobColorConfig.displayBackgroundGradient.from); // 0  Beginning
		grd.addColorStop(1, this.knobColorConfig.displayBackgroundGradient.to); // 1  End
		context.fillStyle = grd;

		// Background
		KnobAndDisplay.roundRect(context, 0, 0, this.canvas.width, this.canvas.height, 10, true, false);

		// knob
		let center = {
			x: this.width / 2,
			y: this.height / 2
		};
		// Background
		this._knobRadius = Math.min(this.width, this.height) * 0.6 / 2;
		let radGrd = context.createRadialGradient(center.x, center.y, 1, center.x, center.y, this._knobRadius);
		radGrd.addColorStop(0, this.knobColorConfig.knobRadialGradient.from); // 0  Beginning
		radGrd.addColorStop(1, this.knobColorConfig.knobRadialGradient.to);   // 1  End
		context.fillStyle = radGrd;
		context.beginPath();
		context.arc(center.x, center.y, this._knobRadius, 0, 2 * Math.PI);
		context.fill();
		context.closePath();
		// Index
		this._indexRadius = this._knobRadius / 8;
		let indexPathRadiusFactor = 0.65;
		let indexAngle = 360 * ((this._value - this._minimum) / (this._maximum - this._minimum));
		this._indexCenter = {
			x: center.x + ((this._knobRadius * indexPathRadiusFactor) * Math.cos(Math.toRadians(indexAngle - 90))),
			y: center.y + ((this._knobRadius * indexPathRadiusFactor) * Math.sin(Math.toRadians(indexAngle - 90)))
		};
		context.beginPath();

		let indexGrd = context.createRadialGradient(this._indexCenter.x, this._indexCenter.y, 1, this._indexCenter.x, this._indexCenter.y, this._indexRadius);
		indexGrd.addColorStop(0, this.knobColorConfig.indexRadialGradient.from); // 0  Beginning
		indexGrd.addColorStop(1, this.knobColorConfig.indexRadialGradient.to);   // 1  End
		context.fillStyle = indexGrd;
		// context.fillStyle = this.knobColorConfig.indexColor;
		context.arc(this._indexCenter.x, this._indexCenter.y, this._indexRadius, 0, 2 * Math.PI);
		context.fill();

		context.closePath();
		// knob ticks
		context.beginPath();
		let exterior = 0.98;
		let interior = 0.85;
		let step = 3;
		for (let i = 0; i < 360; i += step) {
			let _currentAngle = Math.toRadians(indexAngle + i - 90);
			let xFrom = center.x - ((this._knobRadius * exterior) * Math.cos(_currentAngle));
			let yFrom = center.y - ((this._knobRadius * exterior) * Math.sin(_currentAngle));
			let xTo = center.x - ((this._knobRadius * interior) * Math.cos(_currentAngle));
			let yTo = center.y - ((this._knobRadius * interior) * Math.sin(_currentAngle));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
		}
		context.lineWidth = 1;
		context.strokeStyle = this.knobColorConfig.tickColor;
		context.stroke();
		context.closePath();
		// Label
		if (this._label.length > 0) {
			let strVal = this._label;
			context.fillStyle = this.knobColorConfig.labelColor;
			context.font = "bold " + Math.round(scale * 16) + "px " + this.knobColorConfig.labelFont;
			let metrics = context.measureText(strVal);
			let len = metrics.width;
			context.fillText(this.label, center.x - (len / 2), center.y + 8);
		}
		// Display
		// from -60 to +60
		let displayThickness = Math.min(this.width, this.height) * 0.3 / 2;

		let displayLeft = (1.5 * Math.PI) + Math.toRadians(-60);
		let arcWidth = Math.toRadians(120);
		let displayInnerRadius = (this._knobRadius * 1.1);

		// Outline
		context.beginPath();
		context.lineWidth = 3;
		context.strokeStyle = this.knobColorConfig.displayOutlineColor;
		context.arc(center.x, center.y, displayInnerRadius + displayThickness, displayLeft, displayLeft + arcWidth, false);
		context.arc(center.x, center.y, displayInnerRadius, displayLeft + arcWidth, displayLeft, true);
		context.lineTo(center.x - ((displayInnerRadius + displayThickness) * Math.cos(Math.toRadians(30))), center.y - ((displayInnerRadius + displayThickness) * Math.sin(Math.toRadians(30))));
		context.stroke();
		context.closePath();
		// Display background
		context.lineWidth = displayThickness;
		context.beginPath();
		context.strokeStyle = this.knobColorConfig.displayBackgroundColor;
		context.arc(center.x, center.y, displayInnerRadius + (displayThickness / 2), displayLeft, displayLeft + arcWidth, false);
		context.stroke();
		context.closePath();
		// display ticks
		let displayTickStep = 3;
		let tickLength = displayThickness / 4;
		context.beginPath();
		for (let i = -60; i <= 60; i += displayTickStep) {
			let _currentAngle = Math.toRadians(i + 90);
			let xFrom = center.x - ((displayInnerRadius + displayThickness) * Math.cos(_currentAngle));
			let yFrom = center.y - ((displayInnerRadius + displayThickness) * Math.sin(_currentAngle));
			let xTo = center.x - ((displayInnerRadius + displayThickness - tickLength) * Math.cos(_currentAngle));
			let yTo = center.y - ((displayInnerRadius + displayThickness - tickLength) * Math.sin(_currentAngle));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
		}
		context.lineWidth = 1;
		context.strokeStyle = this.knobColorConfig.displayTicksColor;
		context.stroke();
		context.closePath();
		// hand/needle
		let needleAngle = (120 * ((this._value - this._minimum) / (this._maximum - this._minimum))) - 60 + 90;
		if (knobVerbose) {
			console.log("Needle Angle:", needleAngle);
		}
		let needleTip = {
			x: center.x - ((displayInnerRadius + (displayThickness / 2)) * Math.cos(Math.toRadians(needleAngle))),
			y: center.y - ((displayInnerRadius + (displayThickness / 2)) * Math.sin(Math.toRadians(needleAngle)))
		};
		let needleBottomLeft = {
			x: center.x - ((displayInnerRadius) * Math.cos(Math.toRadians(Math.max(needleAngle - 2, 30)))),
			y: center.y - ((displayInnerRadius) * Math.sin(Math.toRadians(Math.max(needleAngle - 2, 30))))
		};
		let needleBottomRight = {
			x: center.x - ((displayInnerRadius) * Math.cos(Math.toRadians(Math.min(needleAngle + 2, 150)))),
			y: center.y - ((displayInnerRadius) * Math.sin(Math.toRadians(Math.min(needleAngle + 2, 150))))
		};
		context.beginPath();
		context.moveTo(needleBottomLeft.x, needleBottomLeft.y);
		context.lineTo(needleTip.x, needleTip.y);
		context.lineTo(needleBottomRight.x, needleBottomRight.y);
		context.fillStyle = this.knobColorConfig.needleFillColor;
		context.fill();
		context.lineWidth = 1;
		context.strokeStyle = this.knobColorConfig.needleOutlineColor;
		context.stroke();
		context.closePath();

		// Value
		context.fillStyle = this.knobColorConfig.valueColor;
		context.font = "bold " + Math.round(scale * 14) + "px " + this.knobColorConfig.valueFont;
		let strVal = this._value.toFixed(this.knobColorConfig.valueNbDecimal);
		let metrics = context.measureText(strVal);
		let len = metrics.width;

		context.fillText(strVal, this.canvas.width - len - 5, this.canvas.height - 5);
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
window.customElements.define(KNOB_TAG_NAME, KnobAndDisplay);
