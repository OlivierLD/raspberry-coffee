const spVerbose = true;
const SAT_PLOT_TAG_NAME = 'satellite-plotter';

/*
* See custom properties in CSS.
* =============================
* @see https://developer.mozilla.org/en-US/docs/Web/CSS/
* Relies on those elements:
*
.xxxxxxxx {
	--border-color: rgba(0, 0, 0, 0);
	--with-gradient: true;
	--display-background-gradient-from: LightGrey;
	--display-background-gradient-to: white;
	--with-display-shadow: true;
	--shadow-color: rgba(0, 0, 0, 0.75);
	--line-color: 'grey';
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
const defaultSatPlotColorConfig = {
	borderColor: 'black',
	withGradient: true,
	displayBackgroundGradient: {
		from: 'DarkGrey',
		to: 'black'
	},
	withDisplayShadow: true,
	shadowColor: 'black',
	lineColor: 'DarkGrey',
	font: 'Arial'
};

// import * as Utilities from "../utilities/Utilities.js";
import * as Utilities from "./utilities/Utilities.js";

/* global HTMLElement */
class SatellitePlotter extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",            // Integer. Canvas width
			"height",           // Integer. Canvas height
			"with-border",      // Boolean
			"sat-in-view"       // Stringified JSON
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
		this._width            = 150;
		this._height           = 150;
		this._with_border      = true;

		this._satellites = {};

		this._previousClassName = "";
		this.satPlotColorConfig = defaultSatPlotColorConfig; // Init

		if (spVerbose) {
			console.log("Data in Constructor:", this._value);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (spVerbose) {
			console.log("connectedCallback invoked");
		}
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (spVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (spVerbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "width":
				this._width = parseInt(newVal);
				break;
			case "height":
				this._height = parseInt(newVal);
				break;
			case "with-border":
				this._with_border = ("true" === newVal);
				break;
			case "sat-in-view":
				this._satellites = JSON.parse(newVal);
				break;
			default:
				break;
		}
		this.repaint();
	}

	// Called whenever the custom element has been moved into a new document.
	adoptedCallback() {
		if (spVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set width(val) {
		this.setAttribute("width", val);
	}
	set height(val) {
		this.setAttribute("height", val);
	}
	set withBorder(val) {
		this.setAttribute("with-border", val);
	}
	set satInView(val) {
		this.setAttribute("sat-in-view", val);
	}
	set shadowRoot(val) {
		this._shadowRoot = val;
	}

	get width() {
		return this._width;
	}
	get height() {
		return this._height;
	}
	get withBorder() {
		return this._with_border;
	}
	get satInView() {
		return this._satellites;
	}
	get shadowRoot() {
		return this._shadowRoot;
	}

	// Component methods
	repaint() {
		this.drawDisplay();
	}

	setSatellites(sats) {
		this._satellites = sats;
	}

	getColorConfig(classNames) {
		let colorConfig = defaultSatPlotColorConfig;
		let classes = classNames.split(" ");
		for (let cls=0; cls<classes.length; cls++) {
			let className = classes[cls];
			for (let s=0; s<document.styleSheets.length; s++) {
				//		console.log("Walking though ", document.styleSheets[s]);
				try {
					for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
						let selector = document.styleSheets[s].cssRules[r].selectorText;
						//			console.log(">>> ", selector);
						if (selector !== undefined && (selector === '.' + className || (selector.indexOf('.' + className) > -1 && selector.indexOf(SAT_PLOT_TAG_NAME) > -1))) { // Cases like "tag-name .className"
							//				console.log("  >>> Found it! [%s]", selector);
							let cssText = document.styleSheets[s].cssRules[r].style.cssText;
							let cssTextElems = cssText.split(";");
							cssTextElems.forEach((elem) => {
								if (elem.trim().length > 0) {
									let keyValPair = elem.split(":");
									let key = keyValPair[0].trim();
									let value = keyValPair[1].trim();
									switch (key) {
										case '--border-color':
											colorConfig.borderColor = value;
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
										case '--with-display-shadow':
											colorConfig.withDisplayShadow = (value === 'true');
											break;
										case '--shadow-color':
											colorConfig.shadowColor = value;
											break;
										case '--line-color':
											colorConfig.lineColor = value;
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

	drawDisplay() {

		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
			//	console.log("Reloading CSS");
			try {
				this.satPlotColorConfig = this.getColorConfig(currentStyle);
			} catch (err) {
				// Absorb?
				console.log(err);
			}
			this._previousClassName = currentStyle;
		}

		let borderColor = this.satPlotColorConfig.borderColor;

		let context = this.canvas.getContext('2d');
		context.fillStyle = 'transparent';
		context.fillRect(0, 0, this._width, this._height);

		let radius = (Math.min(this._width, this._height) / 2) * 0.90;

		// Set the canvas size from its container.
		this.canvas.width = this._width;
		this.canvas.height = this._height;

		let center = {
			x: this.canvas.width / 2,
			y: (this.canvas.height / 2)
		};

		context.beginPath();

		if (this.withBorder === true) {
			context.strokeStyle = borderColor;
			// context.arc(this.canvas.width / 2, radius + 10, radius, 0, 2 * Math.PI, false);
			context.arc(center.x, center.y, radius, 0, 2 * Math.PI, false);
			context.lineWidth = 15;
			context.stroke();
		}

		if (this.satPlotColorConfig.withGradient) {
			let grd = context.createLinearGradient(0, 5, 0, radius);
			grd.addColorStop(0, this.satPlotColorConfig.displayBackgroundGradient.from);// 0  Beginning
			grd.addColorStop(1, this.satPlotColorConfig.displayBackgroundGradient.to);  // 1  End
			context.fillStyle = grd;
		} else {
			context.fillStyle = this.satPlotColorConfig.displayBackgroundGradient.to;
		}
		if (this.satPlotColorConfig.withDisplayShadow) {
			context.shadowOffsetX = 3;
			context.shadowOffsetY = 3;
			context.shadowBlur = 3;
			context.shadowColor = this.satPlotColorConfig.shadowColor;
		} else {
			context.shadowOffsetX = 0;
			context.shadowOffsetY = 0;
			context.shadowBlur = 0;
			context.shadowColor = undefined;
		}
		context.lineJoin = "round";
		context.fill();

		context.closePath();

		// Axis: N-S, E-W, NE-SW, NW-SE
		context.strokeStyle = this.satPlotColorConfig.lineColor;
		context.lineWidth = 1;
		context.setLineDash([3, 3]); // 3px dash, 3px space
		context.beginPath();
		// N-S
		context.moveTo(center.x, center.y - radius); // N
		context.lineTo(center.x, center.y + radius); // S
		// E-W
		context.moveTo(center.x - radius, center.y); // W
		context.lineTo(center.x + radius, center.y); // E
		// NW-SE
		context.moveTo(center.x - (radius * Math.sin(Math.PI / 4)), center.y - (radius * Math.sin(Math.PI / 4))); // NW
		context.lineTo(center.x + (radius * Math.sin(Math.PI / 4)), center.y + (radius * Math.sin(Math.PI / 4))); // SE
		// NE-SW
		context.moveTo(center.x - (radius * Math.sin(Math.PI / 4)), center.y + (radius * Math.sin(Math.PI / 4))); // NE
		context.lineTo(center.x + (radius * Math.sin(Math.PI / 4)), center.y - (radius * Math.sin(Math.PI / 4))); // SW

		// Altitude circles 30, 60.
		context.moveTo(center.x + (radius / 3), center.y); // 0 degrees is actually E
		context.arc(center.x, center.y, radius / 3, 0, 2 * Math.PI, false); // 60 degrees
		context.arc(center.x, center.y, 2 * radius / 3, 0, 2 * Math.PI, false); // 30 degrees

		context.stroke();
		context.closePath();

		context.setLineDash([0]); // 3px dash, 3px space

		// Plot satellites.
		const SAT_RADIUS = 6;
		if (this._satellites !== undefined && this._satellites !== {}) {
			for (let satNum in this._satellites) {
				context.beginPath();

				context.fillStyle = getSNRColor(this._satellites[satNum].snr);
//              let satCircleRadius = radius * (Math.cos(toRadians(demoSat[i].el)));
				let satCircleRadius = radius * ((90 - this._satellites[satNum].elevation) / 90);
				let centerSat = {
					x: center.x + (satCircleRadius * Math.sin(Math.toRadians(this._satellites[satNum].azimuth))),
					y: center.y - (satCircleRadius * Math.cos(Math.toRadians(this._satellites[satNum].azimuth)))
				};
				context.arc(centerSat.x, centerSat.y, SAT_RADIUS, 0, 2 * Math.PI, false);

				let text = this._satellites[satNum].svID;
				let scale = 1; // TODO Fix this
				context.font = "bold " + Math.round(scale * 12) + "px " + this.satPlotColorConfig.font; // "bold 40px Arial"
				let metrics = context.measureText(text);
				let len = metrics.width;

				context.fill();
				context.stroke();
				context.fillText(text, centerSat.x - (len / 2), centerSat.y - SAT_RADIUS - 2);

				context.closePath();
			}
		}
	}

	getSNRColor(snr) {
		let c = 'lightGray';
		if (snr !== undefined && snr !== null) {
			if (snr > 0) {
				c = 'red';
			}
			if (snr > 10) {
				c = 'orange';
			}
			if (snr > 20) {
				c = 'yellow';
			}
			if (snr > 30) {
				c = 'lightGreen';
			}
			if (snr > 40) {
				c = 'green';
			}
		}
		return c;
	}

}

// Associate the tag and the class
window.customElements.define(SAT_PLOT_TAG_NAME, SatellitePlotter);
