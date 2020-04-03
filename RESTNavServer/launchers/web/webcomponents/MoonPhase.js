/**
 * MoonPhase
 * TODO Invert visual if observed from South hemisphere
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
			"label"   // String, like Phase, etc
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
		this._width = 200;
		this._height = 200;
		this._label = "Moon Phase";

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
			case "width":
				this._width = parseInt(newVal);
				break;
			case "height":
				this._height = parseInt(newVal);
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

	set width(val) {
		this.setAttribute("width", val);
	}

	set height(val) {
		this.setAttribute("height", val);
	}

	set label(val) {
		this.setAttribute("label", val);
	}

	set shadowRoot(val) {
		this._shadowRoot = val;
	}

	get phase() {
		return this._phase;
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
		let strVal = this.phase.toFixed(this.moonPhaseColorConfig.valueNbDecimal) + "°";
		let metrics = context.measureText(strVal);
		let len = metrics.width;

		context.fillText(strVal, this.canvas.width - len - 5, this.canvas.height - 5);

		// Draw the moon here
		let radius = Math.min(this.width, this.height) / 3;
		let center = {
			x: this.width / 2,
			y: this.height / 2
		};
		// White BG
		context.fillStyle = 'rgb(255,250,205)'; //  'white';
		context.beginPath();
		context.arc(center.x, center.y, radius, 0, radius * Math.PI);
		context.fill();
		context.closePath();

		// Draw the border (rim) here, phase is this.phase
		if (moonPhaseVerbose) {
			console.log('Radius', radius);
		}
		context.beginPath();
		// Move on top (of the moon)
		context.moveTo(center.x, center.y - radius);
		// Draw disc rim
		// Phase > 180, phase < 180, see in lineTo below
		for (let i=180; i>=0; i-=10) { // Bottom to top
			let x = radius * Math.sin(Math.toRadians(i));
			let y = radius * Math.cos(Math.toRadians(i));
			context.lineTo(center.x + ((this.phase > 180 ? 1 : -1) * x), center.y - y);
		}
		let phaseRimOrientation = Math.cos(Math.toRadians(this.phase));
		for (let i=0; i<=180; i+=10) { // Top to bottom
			let x = radius * Math.sin(Math.toRadians(i)) * phaseRimOrientation;
			let y = radius * Math.cos(Math.toRadians(i));
			context.lineTo(center.x + ((this.phase > 180 ? -1 : 1) * x), center.y - y);
			if (moonPhaseVerbose) {
				console.log('Ph: ', this.phase,  '=> i=', i, 'X:', x, 'Y:', y);
			}
		}
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
