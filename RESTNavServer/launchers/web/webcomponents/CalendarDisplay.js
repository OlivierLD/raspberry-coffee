const calendarVerbose = false;
const CALENDAR_TAG_NAME = 'calendar-display';

const calendarDefaultColorConfig = {
	bgColor: 'white',
	displayBackgroundGradient: {
		from: 'white',
		to: 'lightgray'
	},
	dayColor: 'red',
	monthYearColor: 'black',
	valueFont: 'Arial'
};

const MONTH_NAMES = [
	"January", "February", "March",
	"April", "May", "June",
	"July", "August", "September",
	"October", "November", "December"
];

/* global HTMLElement */
class CalendarDisplay extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",        // Integer. Canvas width
			"height",       // Integer. Canvas height
			"value"         // String, DD-MM-YYYY
		];
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		this.shadowRoot.appendChild(this.canvas);

		// Default values
		this._value       = '01-01-2018';
		this._width       = 150;
		this._height      = 180;

		this._previousClassName = "";
		this.calendarColorConfig = calendarDefaultColorConfig;

		if (calendarVerbose) {
			console.log("Data in Constructor:", this._value);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (calendarVerbose) {
			console.log("connectedCallback invoked, 'value' is [", this.value, "]");
		}
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (calendarVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (calendarVerbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "value":
				this._value = newVal;
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
		if (calendarVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set value(option) {
		this.setAttribute("value", option);
		if (calendarVerbose) {
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
		let colorConfig = calendarDefaultColorConfig;
		let classes = classNames.split(" ");
		for (let cls=0; cls<classes.length; cls++) {
			let cssClassName = classes[cls];
			for (let s=0; s<document.styleSheets.length; s++) {
				// console.log("Walking though ", document.styleSheets[s]);
				try {
					for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
						let selector = document.styleSheets[s].cssRules[r].selectorText;
						//			console.log(">>> ", selector);
						if (selector !== undefined && (selector === '.' + cssClassName || (selector.indexOf('.' + cssClassName) > -1 && selector.indexOf(CALENDAR_TAG_NAME) > -1))) { // Cases like "tag-name .className"
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
										case '--day-color':
											colorConfig.dayColor = value;
											break;
										case '--month-year-color':
											colorConfig.monthYearColor = value;
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
		this.drawCalendar(this._value);
	}

	drawCalendar(dateValue) {

		let currentStyle = this.className;
		if (this._previousClassName !== currentStyle || true) {
			// Reload
			//	console.log("Reloading CSS");
			try {
				this.calendarColorConfig = this.getColorConfig(currentStyle);
			} catch (err) {
				// Absorb?
				console.log(err);
			}

			this._previousClassName = currentStyle;
		}

		let context = this.canvas.getContext('2d');
		let scale = this._height / 180;

		if (this._width === 0 || this._height === 0) { // Not visible
			return;
		}
		// Set the canvas size from its container.
		this.canvas.width = this._width;
		this.canvas.height = this._height;

		var grd = context.createLinearGradient(0, 5, 0, this.height);
		grd.addColorStop(0, this.calendarColorConfig.displayBackgroundGradient.from); // 0  Beginning
		grd.addColorStop(1, this.calendarColorConfig.displayBackgroundGradient.to);  // 1  End
		context.fillStyle = grd;

		// Background
		this.roundRect(context, 0, 0, this.canvas.width, this.canvas.height, 10, true, false);

		let dateElem = this._value.split("-");

		// Day
		let day = parseInt(dateElem[0]);
		context.fillStyle = this.calendarColorConfig.dayColor;
		context.font = "bold " + Math.round(scale * 130) + "px " + this.calendarColorConfig.valueFont;
		let dayVal = day.toString();
		let metrics = context.measureText(dayVal);
		let len = metrics.width;
		context.fillText(dayVal, (this.canvas.width / 2) - (len / 2), (this.canvas.height / 2) + (Math.round(scale * 130) / 2) - (20 * scale));

		// Week day - optional
		if (dateElem[3] !== undefined) {
			let weekDay = dateElem[3].toUpperCase();
			context.fillStyle = 'rgba(0, 0, 0, 0.5)'; // this.calendarColorConfig.dayColor;
			context.font = "bold " + Math.round(scale * 16) + "px " + this.calendarColorConfig.valueFont;
			let metrics = context.measureText(weekDay);
			let len = metrics.width;
			context.fillText(weekDay, (this.canvas.width / 2) - (len / 2), (this.canvas.height / 2)  + (Math.round(scale * 16) / 4 ));
		}

		// Month
		let month = parseInt(dateElem[1]) - 1;
		context.fillStyle = this.calendarColorConfig.monthYearColor;
		context.font = "bold " + Math.round(scale * 20) + "px " + this.calendarColorConfig.valueFont;
		let monthVal = MONTH_NAMES[month];
		metrics = context.measureText(monthVal);
		len = metrics.width;
		context.fillText(monthVal, (this.canvas.width / 2) - (len / 2), this.canvas.height - (scale * 15));
		// Year
		let year = parseInt(dateElem[2]);
		context.fillStyle = this.calendarColorConfig.monthYearColor;
		context.font = "bold " + Math.round(scale * 20) + "px " + this.calendarColorConfig.valueFont;
		let yearVal = year.toString();
		metrics = context.measureText(yearVal);
		len = metrics.width;
		context.fillText(yearVal, (this.canvas.width / 2) - (len / 2), (scale * 30));
	}

	roundRect(ctx, x, y, width, height, radius, fill, stroke) {
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
window.customElements.define(CALENDAR_TAG_NAME, CalendarDisplay);
