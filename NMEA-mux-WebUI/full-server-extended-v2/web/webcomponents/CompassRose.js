const compassRoseVerbose = false;
const COMPASS_ROSE_TAG_NAME = 'compass-rose';

const compassRoseDefaultColorConfig = {
	bgColor:           'white',
	digitColor:        '#404040',
	withGradient:      true,
	displayBackgroundGradient: {
		from: 'gray',
		to: 'white'
	},
	tickColor:         'darkGray',
	indexColor:        'red',
	font:              'Arial'
};

/* global HTMLElement */
class CompassRose extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",        // Integer. Canvas width
			"height",       // Integer. Canvas height
			"value"         // Float. Numeric value to display
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		this.shadowRoot.appendChild(this.canvas);

		// Default values
		this._value       =   0;
		this._width       =  50;
		this._height      = 500;

		this.totalViewAngle = 60; // must be even...

		this._previousClassName = "";
		this.compassRoseColorConfig = compassRoseDefaultColorConfig;

		if (compassRoseVerbose) {
			console.log("Data in Constructor:", this._value);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (compassRoseVerbose) {
			console.log("connectedCallback invoked, 'value' is [", this.value, "]");
		}
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (compassRoseVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (compassRoseVerbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "value":
				let value = parseFloat(newVal);
				this._value = parseInt(value.toFixed(0));
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
		if (compassRoseVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set value(option) {
		this.setAttribute("value", option);
		if (compassRoseVerbose) {
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
	getColorConfig(classNames) {
		let colorConfig = compassRoseDefaultColorConfig;
		let classes = classNames.split(" ");
		for (let cls=0; cls<classes.length; cls++) {
			let cssClassName = classes[cls];
			for (let s=0; s<document.styleSheets.length; s++) {
				// console.log("Walking though ", document.styleSheets[s]);
				try {
					for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
						let selector = document.styleSheets[s].cssRules[r].selectorText;
		//			console.log(">>> ", selector);
						if (selector !== undefined && (selector === '.' + cssClassName || (selector.indexOf('.' + cssClassName) > -1 && selector.indexOf(COMPASS_ROSE_TAG_NAME) > -1))) { // Cases like "tag-name .className"
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
										case '--with-gradient':
											colorConfig.withGradient = (value === 'true');
											break;
										case '--display-background-gradient-from':
											colorConfig.displayBackgroundGradient.from = value;
											break;
										case '--display-background-gradient-to':
											colorConfig.displayBackgroundGradient.to = value;
											break;
										case '--digit-color':
											colorConfig.digitColor = value;
											break;
										case '--tick-color':
											colorConfig.tickColor = value;
											break;
										case '--index-color':
											colorConfig.indexColor = value;
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


	repaint() {
		this.drawCompassRose(this._value);
	}

	drawCompassRose(compassValue) {

		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
			//	console.log("Reloading CSS");
			try {
				this.compassRoseColorConfig = this.getColorConfig(currentStyle);
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

		if (this.compassRoseColorConfig.withGradient === true) {
			let grd = context.createLinearGradient(0, 5, 0, this.height);
			grd.addColorStop(0, this.compassRoseColorConfig.displayBackgroundGradient.from); // 0  Beginning
			grd.addColorStop(1, this.compassRoseColorConfig.displayBackgroundGradient.to);   // 1  End
			context.fillStyle = grd;
		} else {
			context.fillStyle = this.compassRoseColorConfig.displayBackgroundGradient.to;
		}

		// Background
		this.roundRect(context, 0, 0, this.width, this.height, 10, true, false);

		// Ticks
		context.strokeStyle = this.compassRoseColorConfig.tickColor;
		context.lineWidth   = 1;

		let startValue = compassValue - (this.totalViewAngle / 2);
		let endValue   = compassValue + (this.totalViewAngle / 2);
		for (let tick=startValue; tick<=endValue; tick++) {
			let tickHeight = this.height / 4;
			if (tick % 5 === 0) {
				tickHeight = this.height / 2;
			}
			let x = (tick - startValue) * (this.width / this.totalViewAngle);
			context.strokeStyle = this.compassRoseColorConfig.tickColor; // 'rgba(255, 255, 255, 0.7)';
			context.beginPath();
			context.moveTo(x, 0);
			context.lineTo(x, tickHeight);
			context.closePath();
			context.stroke();
			if (tick % 15 === 0) {
				let tk = tick;
				while (tk < 0) { tk += 360 };
				let txt = tk.toString();
				if (tick % 45 === 0) {
					if (tick === 0) { txt = "N"; }
					if (tick === 45) { txt = "NE"; }
					if (tick === 90) { txt = "E"; }
					if (tick === 135) { txt = "SE"; }
					if (tick === 180) { txt = "S"; }
					if (tick === 225) { txt = "SW"; }
					if (tick === 270) { txt = "W"; }
					if (tick === 315) { txt = "NW"; }
					if (tick === 360) { txt = "N"; }
				}
				context.font = "bold " + Math.round(scale * 20) + "px " + this.compassRoseColorConfig.font; // "bold 16px Arial"
				let metrics = context.measureText(txt);
				let len = metrics.width;
				context.fillStyle = this.compassRoseColorConfig.digitColor;
				context.fillText(txt, x - (len / 2), this.height - 10);
			}
		}

		// Value, top left corner
		if (this.compassRoseColorConfig.withGradient === true) {
			let grd = context.createLinearGradient(0, 5, 0, this.height);
			grd.addColorStop(0, this.compassRoseColorConfig.displayBackgroundGradient.from); // 0  Beginning
			grd.addColorStop(1, this.compassRoseColorConfig.displayBackgroundGradient.to);   // 1  End
			context.fillStyle = grd;
		} else {
			context.fillStyle = this.compassRoseColorConfig.displayBackgroundGradient.to;
		}
		this.roundRect(context, 2, 1, 42, 16, 3, true, true);

		context.fillStyle = this.compassRoseColorConfig.digitColor;
		context.font = "bold " + Math.round(scale * 16) + "px Courier New"; // "bold 16px Arial"
		let toDisplay = compassValue;
		while (toDisplay < 0) { toDisplay += 360; }
		while (toDisplay > 360) { toDisplay -= 360; }
		context.fillText(toDisplay.toFixed(0) + "°", 5, 14);
		context.closePath();
		// Index
		context.beginPath();
		context.moveTo(this.width / 2, 0);
		context.lineTo(this.width / 2, this.height);

		context.lineWidth   = 2;
		context.strokeStyle = this.compassRoseColorConfig.indexColor; // The index
		context.stroke();
		context.closePath();
	}

	 roundRect(ctx, x, y, width, height, radius, fill, stroke)  {
		if (fill === undefined)  {
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
		if (stroke) {
			ctx.stroke();
		}
		if (fill) {
			ctx.fill();
		}
		ctx.closePath();
	}
}

// Associate the tag and the class
window.customElements.define(COMPASS_ROSE_TAG_NAME, CompassRose);
