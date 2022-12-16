/**
 * Compass Globe Web Component (like Plastimo).
 * It has several attributes: width, height, value, and label, driving a canvas.
 * They have default values, respectively: 250, 100, 0, 'VAL'
 * Attributes are exposed and can be modified externally (from JavaScript)
 * In addition, there is a CSS colors management as well.
 * 
 * Colors: See https://htmlcolorcodes.com/color-names/
 */

const compassGlobeVerbose = false;
const COMPASS_GLOBE_TAG_NAME = 'compass-globe';

const compassGlobeDefaultColorConfig = {
	bgColor: 'transparent',
	displayBackgroundGradient: {
		from: 'black',
		to: 'gray'
	},
	displayGlobeGradient: {
		from: 'black',
		to: 'rgb(47, 79, 79)'
	},
	displayCylinderGradient: {
		from: 'black',
		to: 'darkgray'
	},
	indexColor: 'red',
	displayColor: 'rgb(220, 220, 220)',  // 'silver'
	valueNbDecimal: 0,
	labelFont: 'Courier New',
	valueFont: 'Verdana',
	valueColor: 'cyan'
};

const MAJOR_TICK_SIZE = 20;    
const MINOR_TICK_SIZE = 10;    
const BASE_FONT_SIZE = 30;
const CYLINDER_HALF_SIZE = 60; 

/* global HTMLElement */
class CompassGlobeDisplay extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",  // Integer. Canvas width
			"height", // Integer. Canvas height
			"value",  // Float. Numeric value to display
			"label"   // String, like HDG, HDM, etc
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		let fallbackElemt = document.createElement("h1");
		let content = document.createTextNode("This is a Compass-Globe WebComponent, on an HTML5 canvas");
		fallbackElemt.appendChild(content);
		this.canvas.appendChild(fallbackElemt);
		this.shadowRoot.appendChild(this.canvas);

		// Default values
		this._value = 0;
		this._width = 250;
		this._height = 250;
		this._label = "VAL";

		this._previousClassName = "";
		this.compassGlobeColorConfig = compassGlobeDefaultColorConfig;

		if (compassGlobeVerbose) {
			console.log("Data in Constructor:", this._value);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (compassGlobeVerbose) {
			console.log("connectedCallback invoked, 'value' is [", this.value, "]");
		}
		this.repaint();
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (compassGlobeVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (compassGlobeVerbose) {
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
			default:
				break;
		}
		this.repaint();
	}

	// Called whenever the custom element has been moved into a new document.
	adoptedCallback() {
		if (compassGlobeVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set value(option) {
		this.setAttribute("value", option);
		if (compassGlobeVerbose) {
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

	get shadowRoot() {
		return this._shadowRoot;
	}

	// Component methods
	getColorConfig(classNames) {
		let colorConfig = compassGlobeDefaultColorConfig;
		let classes = classNames.split(" ");
		for (let cls = 0; cls < classes.length; cls++) {
			let cssClassName = classes[cls];
			for (let s = 0; s < document.styleSheets.length; s++) {
				// console.log("Walking though ", document.styleSheets[s]);
				try {
					for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
						let selector = document.styleSheets[s].cssRules[r].selectorText;
						//			console.log(">>> ", selector);
						if (selector !== undefined && (selector === '.' + cssClassName || (selector.indexOf('.' + cssClassName) > -1 && selector.indexOf(COMPASS_GLOBE_TAG_NAME) > -1))) { // Cases like "tag-name .className"
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
										case '--display-globe-gradient-from':
											colorConfig.displayGlobeGradient.from = value;
											break;
										case '--display-globe-gradient-to':
											colorConfig.displayGlobeGradient.to = value;
											break;
										case '--display-cylinder-gradient-from':
											colorConfig.displayCylinderGradient.from = value;
											break;
										case '--display-cylinder-gradient-to':
											colorConfig.displayCylinderGradient.to = value;
											break;
										case '--index-color':
											colorConfig.indexColor = value;
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
										case '--value-color':
											colorConfig.valueColor = value;
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
		this.drawCompassGlobe();
	}

	drawCompassGlobe() {

		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
			//	console.log("Reloading CSS");
			try {
				this.compassGlobeColorConfig = this.getColorConfig(currentStyle);
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

		// set the sale
		scale = Math.min(this.width, this.height) / 400;
		if (compassGlobeVerbose) {
			console.log(`Scale is now ${scale}`);
		}

		// Background, behind the sphere.
		if (this.compassGlobeColorConfig.displayBackgroundGradient.from !== 'none' &&
			this.compassGlobeColorConfig.displayBackgroundGradient.to !== 'none') {
			let grd = context.createLinearGradient(0, 5, 0, this.height);
			// console.log(`BG Gradient from ${this.compassGlobeColorConfig.displayBackgroundGradient.from}, to ${this.compassGlobeColorConfig.displayBackgroundGradient.to}`);
			grd.addColorStop(0, this.compassGlobeColorConfig.displayBackgroundGradient.from); // 0  Beginning
			grd.addColorStop(1, this.compassGlobeColorConfig.displayBackgroundGradient.to);   // 1  End
			context.fillStyle = grd;
		} else {
			context.fillStyle = this.compassGlobeColorConfig.bgColor;
		}
		CompassGlobeDisplay.roundRect(context, 0, 0, this.canvas.width, this.canvas.height, 10, true, false);

		context.fillStyle = this.compassGlobeColorConfig.displayColor;
		// Label
		context.font = "bold " + Math.round(scale * 16) + "px " + this.compassGlobeColorConfig.labelFont;
		context.fillText(this.label, 5, 18);

		// The globe
		// Colors: See https://htmlcolorcodes.com/color-names/
		// 1 - Background, the sphere.
		let radius = Math.min(this.canvas.height, this.canvas.width) / 2;
		let globeGrd = context.createRadialGradient(
			this.canvas.width / 4, this.canvas.height / 4, 5, 
			this.canvas.width / 4, this.canvas.height / 4, radius);
		globeGrd.addColorStop(0, this.compassGlobeColorConfig.displayGlobeGradient.from);  // 0  Beginning
		globeGrd.addColorStop(1, this.compassGlobeColorConfig.displayGlobeGradient.to);    // 1  End
		context.fillStyle = globeGrd;
		context.beginPath();
		context.arc(this.canvas.width / 2, this.canvas.height / 2, radius, 0, 2 * Math.PI, false);
		context.closePath();
		context.fill();
		// 2 - The reflection on top
		if (false) {
			globeGrd = context.createRadialGradient(
				this.canvas.width / 4, this.canvas.height / 4, 5, 
				this.canvas.width / 4, this.canvas.height / 4, radius);
			globeGrd.addColorStop(0, 'rgba(211, 211, 211, 0.25)');  // 0  Beginning
			globeGrd.addColorStop(1, 'rgba(255, 255, 255, 0.25)');  // 1  End
			context.fillStyle = globeGrd;
			context.beginPath();
			context.ellipse(this.canvas.width / 2, this.canvas.height / 4, radius * 0.5, radius * 0.25, 0, 2 * Math.PI, false);
			context.closePath();
			context.fill();
		}

		let fontSize = Math.round(scale * BASE_FONT_SIZE);
		context.font = "bold " + fontSize + "px " + this.compassGlobeColorConfig.valueFont;

		// The rose, oriented. 
		// A - The cylinder
		globeGrd = context.createLinearGradient(
			this.canvas.width / 2, (this.canvas.height / 2) - Math.round(scale * CYLINDER_HALF_SIZE), 
			this.canvas.width / 2, (this.canvas.height / 2) + Math.round(scale * CYLINDER_HALF_SIZE));
		globeGrd.addColorStop(0, this.compassGlobeColorConfig.displayCylinderGradient.from);  // 0  Beginning
		globeGrd.addColorStop(1, this.compassGlobeColorConfig.displayCylinderGradient.to);    // 1  End
		context.fillStyle = globeGrd;

		context.beginPath();
		// context.fillRect(0, 
		// 	             (this.canvas.height / 2) - Math.round(scale * CYLINDER_HALF_SIZE), 
		//                  this.canvas.width, 
		// 				 Math.round(scale * (2 * CYLINDER_HALF_SIZE)));
		CompassGlobeDisplay.roundRect(context, 0, 
			(this.canvas.height / 2) - Math.round(scale * CYLINDER_HALF_SIZE), 
			this.canvas.width, 
			Math.round(scale * (2 * CYLINDER_HALF_SIZE)), 10, true, false);	 
		context.closePath();
		context.fill();

		// B - Notches and graduations
		context.fillStyle = this.compassGlobeColorConfig.displayColor;
		context.strokeStyle = this.compassGlobeColorConfig.displayColor;
		for (let i = 0; i < 360; i++) {
			let notch = this._value - i;
			// console.log(`i: ${i}, notch=${notch}, cos(notch): ${Math.cos(Math.toRadians(notch))}`);
			context.lineWidth = 1;
			if (Math.cos(Math.toRadians(i)) >= 0) {  // Visible side of the rose
				if (notch % 5 === 0) {
					let xOffset = radius * Math.sin(Math.toRadians(i));
					// console.log(`i: ${i}, notch=${notch}, cos(notch): ${Math.cos(Math.toRadians(notch))}, offset: ${xOffset}`);
					if (notch % 45 === 0) {
						// Labels ? (TODO need to spin the string...)
						context.lineWidth = 5;
						let str = notch.toFixed(0);
						switch (notch) {
							case 0:
								str = "N";
								break;
							case 45:
							case -315:
								str = "NE";
								break;
							case -45:
							case 315:
								str = "NW";
								break;
							case 90:
							case -270:
								str = "E";
								break;
							case 270:
							case -90:
								str = "W";
								break;
							case 135:
							case -225:
								str = "SE";
								break;
							case -135:
							case 225:
								str = "SW";
								break;
							case 180:
							case -180:
								str = "S";
								break;
							default:
								break;

						}
						if (compassGlobeVerbose) {
							console.log(`>> Notch is ${notch} => ${str} (i: ${i})`);
						}

						if (i > 290 || i < 70) { // 20 degs on each side.
							// Set fontSize, smaller on the sides, bigger in the middle
							let _fontSize = Math.round(scale * BASE_FONT_SIZE * Math.cos(Math.toRadians(i)));
							context.font = "bold " + _fontSize + "px " + this.compassGlobeColorConfig.valueFont;
							let metrics = context.measureText(str);
							let len = metrics.width;
					
							context.save();
							let newX = (this.canvas.width / 2) + xOffset - (len / 2);
							let newY = (this.canvas.height / 2) + Math.round(scale * MAJOR_TICK_SIZE) + (_fontSize / 1);
							context.translate(newX, newY);
							let rot = (i > 270) ? i - 360 : i;
 							context.rotate(-Math.sin(Math.toRadians(rot / 9)));  // horizontal rotation: 10 degrees max (9 = 90 / 10)
							context.fillText(str, 0, 0,); // newX, newY);
							context.restore();
						}				
					}
					context.beginPath();
					if (notch % 15 === 0) { // ticks: Major / Minor
						context.moveTo((this.canvas.width / 2) + xOffset, (this.canvas.height / 2) - Math.round(scale * MAJOR_TICK_SIZE));
						context.lineTo((this.canvas.width / 2) + xOffset, (this.canvas.height / 2) + Math.round(scale * MAJOR_TICK_SIZE));
					} else {
						context.moveTo((this.canvas.width / 2) + xOffset, (this.canvas.height / 2) - Math.round(scale * MINOR_TICK_SIZE));
						context.lineTo((this.canvas.width / 2) + xOffset, (this.canvas.height / 2) + Math.round(scale * MINOR_TICK_SIZE));
					}
					context.closePath();
					context.stroke();
				}
			}
		}
		context.font = "bold " + fontSize + "px " + this.compassGlobeColorConfig.valueFont;

		// C - Value
		let strVal = this._value.toFixed(this.compassGlobeColorConfig.valueNbDecimal);
		let metrics = context.measureText(strVal);
		let len = metrics.width;

		context.fillStyle = this.compassGlobeColorConfig.valueColor;
		context.fillText(strVal, (this.canvas.width / 2) - (len / 2), (this.canvas.height / 2) - Math.round(scale * MAJOR_TICK_SIZE) - 2); // (0 * fontSize / 2));

		// 'red' Axis
		context.lineWidth = 1;
		context.strokeStyle = this.compassGlobeColorConfig.indexColor;
		context.beginPath();
		context.moveTo((this.canvas.width / 2), 0);
		context.lineTo((this.canvas.width / 2), this.canvas.height);
		context.closePath();
		context.stroke();
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
window.customElements.define(COMPASS_GLOBE_TAG_NAME, CompassGlobeDisplay);
