const marqueePanelVerbose = false;
const MARQUEE_PANEL_TAG_NAME = 'marquee-panel';

/* The map data */
import characters from "./character.matrixes/characters.js";
// import characters from "./character.matrixes/characters"; // minifyJs does NOT like the .js extension

const marqueeDefaultColorConfig = {
	bgColor: {
		from: 'gray',
		to: 'black'
	},
	fgColor: {
		from: 'red',
		to: 'orange'
	}
};

const NB_LINES = 32;
const NB_COLS = 128;
const FONT_SIZE = 10; // Cannot be changed.

const Mode = {
	WHITE_ON_BLACK: 1,
	BLACK_ON_WHITE: 2
};

/* global HTMLElement */
class MarqueePanel extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",                  // Integer. Canvas width
			"height",
			"nb-cols",
			"nb-lines",
			"display-data"
		];
	}

	static dummyDump() {
		console.log('We have %d character(s).', characters.length);
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		let fallbackElemt = document.createElement("h1");
		let content = document.createTextNode("This is a MarqueePanel, on an HTML5 canvas");
		fallbackElemt.appendChild(content);
		this.canvas.appendChild(fallbackElemt);
		this.shadowRoot.appendChild(this.canvas);

		// For tests of the import
		// MarqueePanel.dummyDump();

		// Default values
		this._width       = 500;
		this._height      = 500;

		this._w  = NB_COLS;
		this._h  = NB_LINES;

		this.screenMatrix = []; // 2D array of characters
		this.initScreenMatrix();

		this.ledRadius = this.width / (2 * this._w);

		this._previousClassName = "";
		this.marqueeColorConfig = marqueeDefaultColorConfig;

		this._displayData = {};

		this._lastUsedColumn;
		this._yOffset = 0;

	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (marqueePanelVerbose) {
			console.log("connectedCallback invoked");
		}
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (marqueePanelVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (marqueePanelVerbose) {
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
			case "display-data":
				this._displayData = JSON.parse(newVal);
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
		if (marqueePanelVerbose) {
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
	set displayData(val) {
		this.setAttribute("display-data", val);
	}
	set yOffset(val) {
		this._yOffset = val;
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
	get displayData() {
		return JSON.stringify(this._displayData);
	}
	get lastUsedColumn() {
		return this._lastUsedColumn;
	}
	get yOffset() {
		return this._yOffset;
	}

	get shadowRoot() {
		return this._shadowRoot;
	}

	/*
	 * Component methods
	 */
	getColorConfig(classNames) {
		let colorConfig = marqueeDefaultColorConfig;
		let classes = classNames.split(" ");
		for (let cls=0; cls<classes.length; cls++) {
			let cssClassName = classes[cls];
			for (let s=0; s<document.styleSheets.length; s++) {
				// console.log("Walking though ", document.styleSheets[s]);
				try {
					for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
						let selector = document.styleSheets[s].cssRules[r].selectorText;
						//			console.log(">>> ", selector);
						if (selector !== undefined && (selector === '.' + cssClassName || (selector.indexOf('.' + cssClassName) > -1 && selector.indexOf(MARQUEE_PANEL_TAG_NAME) > -1))) { // Cases like "tag-name .className"
						  //				console.log("  >>> Found it! [%s]", selector);
							let cssText = document.styleSheets[s].cssRules[r].style.cssText;
							let cssTextElems = cssText.split(";");
							cssTextElems.forEach((elem) => {
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
		for (let y=0; y<this._h; y++) {
			let line = [];
			for (let x=0; x<this._w; x++) {
				line.push(' ');
			}
			this.screenMatrix.push(line);
		}
	}

	static findCharacter(char) {
		let matrix;
		for (let c=0; c<characters.length; c++) {
			if (characters[c].key === char) {
				matrix = characters[c].matrix;
				break;
			}
		}
		return matrix;
	}

	clear(mode) {
		mode = mode || Mode.WHITE_ON_BLACK;

		for (let i = 0; i < this._h; i++) {
			for (let j = 0; j < this._w; j++) {
				this.screenMatrix[i][j] = (mode === Mode.WHITE_ON_BLACK ? ' ' : 'X');
			}
		}
	}

	fillCircle(context, pt, radius, color) {
		let grd = context.createRadialGradient(pt.x - (radius / 3), pt.y - (radius / 3), radius / 3, pt.x, pt.y, radius);
		grd.addColorStop(0, this.marqueeColorConfig.fgColor.from);
		grd.addColorStop(1, this.marqueeColorConfig.fgColor.to);

		context.beginPath();
		context.fillStyle = grd; // color;
		context.arc(pt.x, pt.y, radius, 0, radius * Math.PI);
		context.fill();
		context.closePath();
	}

	static invert(c) {
		return (c === ' ' ? 'X' : ' ');
	}

	repaint() {
		this.drawMarqueePanel();
	}

	drawMarqueePanel() {

		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
			//	console.log("Reloading CSS");
			try {
				this.marqueeColorConfig = this.getColorConfig(currentStyle);
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

		let grd = context.createRadialGradient(this.width / 2, this.height / 2, 5, this.width / 2, this.height / 2, Math.max(this.height, this.width) / 2);
		grd.addColorStop(0, this.marqueeColorConfig.bgColor.from);
		grd.addColorStop(1, this.marqueeColorConfig.bgColor.to);
		context.fillStyle = grd;
		context.fillRect(0, 0, this.width, this.height);

		this.clear();
		if (this._displayData.text !== undefined) {
			// Fill the screen matrix
			this._lastUsedColumn = this.displayText(this._displayData.text, this._displayData.x, this._displayData.y);
		} else if (this._displayData['text-array'] !== undefined) {
			let textArray = this._displayData['text-array'];
			// console.log(textArray);
			let deltaY = 0;
			for (let idx in textArray) {
				this._lastUsedColumn = this.displayText(textArray[idx], this._displayData.x, this._yOffset + this._displayData.y + deltaY);
				deltaY += 10;
			}
		}

		let xStep = Math.round(this.width / this._w);
		let yStep = Math.round(this.height / this._h);
		for (let x=0; x<this._w; x++) {
			for (let y=0; y<this._h; y++) {
				if (this.screenMatrix[y][x] !== ' ') {
					this.fillCircle(context, {x: (x * xStep) + (xStep / 2), y: (y * yStep) + (yStep / 2)}, this.ledRadius, this.marqueeColorConfig.fgColor);
				}
			}
		}

	}

	/**
	 *
	 * @param txt
	 * @param xPx
	 * @param yPx
	 * @param fontFact
	 * @param mode
	 * @param rotate
	 * @returns {*} The index of the last column used on the panel
	 */
	displayText(txt, xPx, yPx, fontFact, mode, rotate) {

		fontFact = fontFact || 1;
		mode = mode || Mode.WHITE_ON_BLACK;
		rotate = rotate || false;

		let screenColumn = xPx;
		for (let i = 0; i < txt.length; i++) {         // For each character of the string to display
			let c = txt.charAt(i);
			let matrix = MarqueePanel.findCharacter(c);

			if (matrix !== undefined) {
				// Assume all pixel lines have the same length
				for (let x = 0; x < matrix[0].length; x++) { // Each COLUMN of the character matrix
					for (let factX = 0; factX < fontFact; factX++) {
						let verticalBitmap = [];
						let vmY = 0;
						for (let my = 0; my < matrix.length; my++) {  // Each LINE of the character matrix
							for (let factY = 0; factY < fontFact; factY++) {
								verticalBitmap.push(matrix[my].charAt(x));
							}
						}
						// Write the character in the screen matrix
						// screenMatrix[line][col]
						for (let y = 0; y < (fontFact * FONT_SIZE); y++) { // One-character vertical bitmap
							let screenLine = (y + yPx - (FONT_SIZE - 1));
							if (!rotate) {
								if (screenLine >= 0 && screenLine < this._h && screenColumn >= 0 && screenColumn < this._w) {
									this.screenMatrix[screenLine][screenColumn] = (mode === Mode.WHITE_ON_BLACK ? verticalBitmap[y] : invert(verticalBitmap[y]));
								}
							} else { // 90 deg counter-clockwise
								if (screenLine >= 0 && screenLine < this._w && screenColumn >= 0 && screenColumn < this._h) {
									this.screenMatrix[this.h - screenColumn][screenLine] = (mode === Mode.WHITE_ON_BLACK ? verticalBitmap[y] : invert(verticalBitmap[y]));
								}
							}
						}
						screenColumn++;
					}
				}
			} else {
				console.log("Character not found for the OLED [" + c + "]");
			}
		}
//	console.log("End of text function, nbX: %d", nbX);
		return screenColumn; //
	}
}

// Associate the tag and the class
window.customElements.define(MARQUEE_PANEL_TAG_NAME, MarqueePanel);
