/**
 * MoonPhase
 */

const moonPhaseVerbose = false;
const MOON_PHASE_TAG_NAME = 'moon-phase';

const moonPhaseDefaultColorConfig = {
	bgColor: 'white',
	displayBackgroundGradient: {
		from: 'black',
		to: 'gray'
	},
	gridColor: 'rgba(255, 255, 255, 0.7)',
	displayColor: 'cyan',
	valueNbDecimal: 1,
	labelFont: 'Courier New',
	valueFont: 'Arial'
};

/* global HTMLElement */
class MoonPhaseDisplay extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",  // Integer. Canvas width
			"height", // Integer. Canvas height
			"phase",  // Float. Phase value to display
			"tilt",   // Float. (ObsLat - MoonD) - 90. Default 0
			"label",  // String, like Phase, etc
			"small-label" // String, like for the tilt (optional)
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		let fallbackElemt = document.createElement("h1");
		let content = document.createTextNode("This is a WebComponent MoonPhase, on an HTML5 canvas");
		fallbackElemt.appendChild(content);
		this.canvas.appendChild(fallbackElemt);
		this.shadowRoot.appendChild(this.canvas);

		// this.fullMoonImage = document.createElement("img");
		// this.fullMoonImage.setAttribute("src", "./widgets/moonphase/fullmoon.jpg");
		// this.fullMoonImage.setAttribute("width", "200");
		// this.fullMoonImage.setAttribute("height", "200");
		//
		// let ctx = this.canvas.getContext("2d");
		// ctx.drawImage(this.fullMoonImage, 0, 0, 200, 200)
		// // this.shadowRoot.appendChild(this.fullMoonImage);

		// Default values
		this._phase = 0;
		this._tilt = 0;
		this._width = 200;
		this._height = 200;
		this._label = "Moon Phase";
		this._small_label = "";

		this._previousClassName = "";
		this.moonPhaseColorConfig = moonPhaseDefaultColorConfig;

		if (moonPhaseVerbose) {
			console.log("Data in Constructor:", this._phase);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (moonPhaseVerbose) {
			console.log("connectedCallback invoked, 'phase' is [", this.phase, "]");
		}
		this.repaint();
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (moonPhaseVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (moonPhaseVerbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "phase":
				this._phase = parseFloat(newVal);
				break;
			case "tilt":
				this._tilt = parseFloat(newVal);
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
			case "small-label":
				this._small_label = newVal;
				break;
			default:
				break;
		}
		this.repaint();
	}

	// Called whenever the custom element has been moved into a new document.
	adoptedCallback() {
		if (moonPhaseVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set phase(option) {
		this.setAttribute("phase", option);
		if (moonPhaseVerbose) {
			console.log(">> Phase option:", option);
		}
//	this.repaint(); // Done in attributeChangedCallback
	}

	set tilt(val) {
		this.setAttribute("tilt", val);
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

	set smallLabel(val) {
		this.setAttribute("small-label", val);
	}

	set shadowRoot(val) {
		this._shadowRoot = val;
	}

	get phase() {
		return this._phase;
	}

	get tilt() {
		return this._tilt;
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

	get smallLabel() {
		return this._small_label;
	}

	get shadowRoot() {
		return this._shadowRoot;
	}

	// Component methods
	getColorConfig(classNames) {
		let colorConfig = moonPhaseDefaultColorConfig;
		let classes = classNames.split(" ");
		for (let cls = 0; cls < classes.length; cls++) {
			let cssClassName = classes[cls];
			for (let s = 0; s < document.styleSheets.length; s++) {
				// console.log("Walking though ", document.styleSheets[s]);
				try {
					for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
						let selector = document.styleSheets[s].cssRules[r].selectorText;
						//			console.log(">>> ", selector);
						if (selector !== undefined && (selector === '.' + cssClassName || (selector.indexOf('.' + cssClassName) > -1 && selector.indexOf(MOON_PHASE_TAG_NAME) > -1))) { // Cases like "tag-name .className"
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
		this.drawMoonPhase();
	}

	drawMoonPhase() {

		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
			//	console.log("Reloading CSS");
			try {
				this.moonPhaseColorConfig = this.getColorConfig(currentStyle);
			} catch (err) {
				// Absorb?
				console.log(err);
			}

			this._previousClassName = currentStyle;
		}

		let context = this.canvas.getContext('2d');
		let scale = this.height / 200;
		if (this.width === 0 || this.height === 0) { // Not visible
			return;
		}

		// Set the canvas size from its container.
		this.canvas.width = this.width;
		this.canvas.height = this.height;

		// return;

		let grd = context.createLinearGradient(0, 5, 0, this.height);
		grd.addColorStop(0, this.moonPhaseColorConfig.displayBackgroundGradient.from); // 0  Beginning
		grd.addColorStop(1, this.moonPhaseColorConfig.displayBackgroundGradient.to); // 1  End
		context.fillStyle = grd;

		// Background
		MoonPhaseDisplay.roundRect(context, 0, 0, this.canvas.width, this.canvas.height, 10, true, false);
		// TODO Full Moon image ?

		context.fillStyle = this.moonPhaseColorConfig.displayColor;
		// Label
		context.font = "bold " + Math.round(scale * 16) + "px " + this.moonPhaseColorConfig.labelFont;
		context.fillText(this.label, 5, 18);

		// Phase Value
		context.font = "bold " + Math.round(scale * 24) + "px " + this.moonPhaseColorConfig.valueFont;
		let strVal = Math.abs(this.phase).toFixed(this.moonPhaseColorConfig.valueNbDecimal) + "°" + (this.phase < 0 ? "." : " ");
		let metrics = context.measureText(strVal);
		let len = metrics.width;

		context.fillText(strVal, this.canvas.width - len - 5, this.canvas.height - 5);

		// Small Label
		if (this.smallLabel !== undefined && this._small_label.length > 0) {
			context.fillStyle = 'orange'; // this.moonPhaseColorConfig.displayColor; // TODO Style this
			context.font = "bold " + Math.round(scale * 16) + "px " + this.moonPhaseColorConfig.labelFont;
			context.fillText(this.smallLabel, 5, this.canvas.height - 5);
		}

		// Draw the moon here
		let radius = Math.min(this.width, this.height) / 3;
		let center = {
			x: this.width / 2,
			y: this.height / 2
		};

		context.save();
		context.translate(center.x, center.y);
		context.rotate(Math.toRadians(this.tilt));

		// White BG
		context.fillStyle = 'rgb(255,250,205)'; //  'white';
		context.beginPath();
		context.arc(0, 0, radius, 0, radius * Math.PI);
		context.fill();
		context.closePath();

		// Draw the border (rim) here, phase is this.phase
		if (moonPhaseVerbose) {
			console.log('Radius', radius);
		}

		// For debug, draw tilt axis
		context.beginPath();
		context.moveTo(0, - (radius * 1.2));
		// context.moveTo(center.x + (radius * Math.sin(Math.toRadians(this.tilt))),
		// 		center.y - (radius * Math.cos(Math.toRadians(this.tilt))));
		context.lineTo(0, (radius * 1.2));
		// context.lineTo(center.x - (radius * Math.sin(Math.toRadians(this.tilt))),
		// 		center.y + (radius * Math.cos(Math.toRadians(this.tilt))));
		context.strokeStyle = 'rgb(255,250,205)'; //  'orange';  // this.graphDisplayColorConfig.gridColor;
		context.stroke();
		context.closePath();

		// Shade on the moon
		context.beginPath();
		// Move on top (of the moon)
		// context.moveTo(center.x, center.y - radius);
		context.moveTo(radius * Math.sin(Math.toRadians(this.tilt)),
				- (radius * Math.cos(Math.toRadians(this.tilt))));

		let correctedPhase = this.phase;
		while (correctedPhase < 0) {
			correctedPhase += 360;
		}
		// Draw disc rim
		// Phase > 180, phase < 180, see in lineTo below
		// let tilt = -this.tilt;


		for (let i=180; i>=0; i-=10) { // Bottom to top
			let x = radius * Math.sin(Math.toRadians(i));
			let y = radius * Math.cos(Math.toRadians(i));
			context.lineTo(((correctedPhase > 180 ? 1 : -1) * x), - y);
		}
		let phaseRimOrientation = Math.cos(Math.toRadians(correctedPhase));
		for (let i=0; i<=180; i+=10) { // Top to bottom
			let x = radius * Math.sin(Math.toRadians(i)) * phaseRimOrientation;
			let y = radius * Math.cos(Math.toRadians(i));
			context.lineTo(((correctedPhase > 180 ? -1 : 1) * x), - y);
			if (moonPhaseVerbose) {
				console.log('Ph: ', correctedPhase,  '=> i=', i, 'X:', x, 'Y:', y);
			}
		}
		context.restore();

		context.lineWidth = 0.5;
		context.strokeStyle = 'black';  // this.graphDisplayColorConfig.gridColor;
		context.fillStyle = 'rgba(0, 0, 0, 0.8)';  // this.graphDisplayColorConfig.gridColor;
		// context.stroke();
		context.fill();
		context.closePath();
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
window.customElements.define(MOON_PHASE_TAG_NAME, MoonPhaseDisplay);
