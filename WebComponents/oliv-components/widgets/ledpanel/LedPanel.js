const ledPanelVerbose = false;
const LED_PANEL_TAG_NAME = 'led-panel';

const ledDefaultColorConfig = {
	bgColor: {
		from: 'gray',
		to: 'black'
	},
	fgColor: {
		from: 'red',
		to: 'orange'
	}
};

// Default for SSD1306 or so.
const NB_LINES = 32;
const NB_COLS = 128;

const Mode = {
	WHITE_ON_BLACK: 1,
	BLACK_ON_WHITE: 2
}

const withGradient = true;

/* global HTMLElement */

/**
 * Vanilla LED Panel
 * LEDs can be accessed one by one
 */
class LedPanel extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",                  // Integer. Canvas width
			"height",
			"nb-cols",
			"nb-lines"
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		let fallbackElemt = document.createElement("h1");
		let content = document.createTextNode("This is a LedPanel, on an HTML5 canvas");
		fallbackElemt.appendChild(content);
		this.canvas.appendChild(fallbackElemt);
		this.shadowRoot.appendChild(this.canvas);

		// For tests of the import
		// this.dummyDump();

		// Default values
		this._width       = 500;
		this._height      = 500;

		this._w  = NB_COLS;
		this._h  = NB_LINES;

		this.screenMatrix = []; // 2D array of leds
		this.initScreenMatrix();

		this.ledRadius = this.width / (2 * this._w);

		this._previousClassName = "";
		this.ledColorConfig = ledDefaultColorConfig;

		this._connected = false;
	}

	// Called whenever the custom element (Web Comp.) is inserted into the DOM.
	connectedCallback() {
		this._connected = true;
		if (ledPanelVerbose) {
			console.log("connectedCallback invoked");
		}
		// TODO The same for the others, init goes here.
		this.ledRadius = this.width / (2 * this._w);
    this.initScreenMatrix();
    this.repaint();
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (ledPanelVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (ledPanelVerbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "width":
				this._width = parseInt(newVal);
				break;
			case "height":
				this._height = parseInt(newVal);
				break;
			case "nb-cols":
				this._w = parseInt(newVal);
				break;
			case "nb-lines":
				this._h = parseInt(newVal);
				break;
			default:
				break;
		}
		this.ledRadius = this.width / (2 * this._w);
		this.initScreenMatrix();
		this.repaint();
	}

	// Called whenever the custom element has been moved into a new document.
	adoptedCallback() {
		if (ledPanelVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set width(val) {
		this.setAttribute("width", val);
	}
	set height(val) {
		this.setAttribute("height", val);
	}
	set nbCols(val) {
		this.setAttribute("nb-cols", val);
	}
	set nbLines(val) {
		this.setAttribute("nb-lines", val);
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
	get nbCols() {
		return this._w;
	}
	get nbLines() {
		return this._h;
	}

	get shadowRoot() {
		return this._shadowRoot;
	}

	/*
	 * Component methods
	 */
	getColorConfig(classNames) {
		let colorConfig = ledDefaultColorConfig;
		let classes = classNames.split(" ");
		for (let cls=0; cls<classes.length; cls++) {
			let cssClassName = classes[cls];
			for (let s=0; s<document.styleSheets.length; s++) {
				// console.log("Walking though ", document.styleSheets[s]);
				try {
					for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
						let selector = document.styleSheets[s].cssRules[r].selectorText;
						//			console.log(">>> ", selector);
						if (selector !== undefined && (selector === '.' + cssClassName || (selector.indexOf('.' + cssClassName) > -1 && selector.indexOf(LED_PANEL_TAG_NAME) > -1))) { // Cases like "tag-name .className"
						  //				console.log("  >>> Found it! [%s]", selector);
							let cssText = document.styleSheets[s].cssRules[r].style.cssText;
							let cssTextElems = cssText.split(";");
							cssTextElems.forEach(function (elem) {
								if (elem.trim().length > 0) {
									let keyValPair = elem.split(":");
									let key = keyValPair[0].trim();
									let value = keyValPair[1].trim();
									switch (key) {
										case '--bg-color-from':
											colorConfig.bgColor.from = value;
											break;
										case '--bg-color-to':
											colorConfig.bgColor.to = value;
											break;
										case '--fg-color-from':
											colorConfig.fgColor.from = value;
											break;
										case '--fg-color-to':
											colorConfig.fgColor.to = value;
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

	initScreenMatrix() {
		if (this._connected) {
		  if (ledPanelVerbose) {
		  	console.log("Init Matrix");
		  }
		  this.screenMatrix = [];
			for (let y=0; y<this._h; y++) {
				let line = [];
				for (let x=0; x<this._w; x++) {
					line.push(true);
				}
				this.screenMatrix.push(line);
			}
		  if (ledPanelVerbose) {
				console.log("Init Matrix %d x %d", this._w, this._h);
		  }
	  }
	}

	clear(mode) {
		if (this._connected) {
			mode = mode || Mode.WHITE_ON_BLACK;

			for (let i = 0; i < this._h; i++) {
				for (let j = 0; j < this._w; j++) {
					this.screenMatrix[i][j] = (mode === Mode.WHITE_ON_BLACK ? false : true);
				}
			}
		}
	}

  /**
   * Draw one led
   */
	fillCircle(context, pt, radius, color) {
		if (withGradient) {
			let grd = context.createRadialGradient(pt.x - (radius / 3), pt.y - (radius / 3), radius / 3, pt.x, pt.y, radius);
			grd.addColorStop(0, this.ledColorConfig.fgColor.from);
			grd.addColorStop(1, this.ledColorConfig.fgColor.to);
			context.fillStyle = grd; // color;
		} else {
			context.fillStyle = 'red'; // grd; // color;
		}
		context.beginPath();
		context.arc(pt.x, pt.y, radius, 0, radius * Math.PI);
		context.fill();
		context.closePath();
	}

	repaint() {
	  if (this._connected) {
		  this.drawLedPanel();
		}
	}

  getLedMatrix() {
    return this.screenMatrix;
  }

  setLedMatrix(ledMatrix) {
    this.screenMatrix = ledMatrix;
  }

	drawLedPanel() {
	  if (this._connected) {
//    console.log(" Repainting!!");
			let currentStyle = this.className;
			if (this._previousClassName !== currentStyle || true) {
				// Reload
				//	console.log("Reloading CSS");
				try {
					this.ledColorConfig = this.getColorConfig(currentStyle);
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

			if (false && withGradient) {
				let grd = context.createRadialGradient(this.width / 2, this.height / 2, 5, this.width / 2, this.height / 2, Math.max(this.height, this.width) / 2);
				grd.addColorStop(0, this.ledColorConfig.bgColor.from);
				grd.addColorStop(1, this.ledColorConfig.bgColor.to);
				context.fillStyle = grd;
			} else {
				context.fillStyle ='transparent'; // 'black'; // grd;
			}
			context.fillRect(0, 0, this.width, this.height);

	//	this.clear();

			let xStep = Math.round(this.width / this._w);
			let yStep = Math.round(this.height / this._h);

			for (let x=0; x<this._w; x++) {
			  for (let y=0; y<this._h; y++) {
					if (this.screenMatrix[y][x] === true) {
						this.fillCircle(context, {x: (x * xStep) + (xStep / 2), y: (y * yStep) + (yStep / 2)}, this.ledRadius - 1, this.ledColorConfig.fgColor);
					}
			  }
			}
    }
	}
}
// Associate the tag and the class
window.customElements.define(LED_PANEL_TAG_NAME, LedPanel);
