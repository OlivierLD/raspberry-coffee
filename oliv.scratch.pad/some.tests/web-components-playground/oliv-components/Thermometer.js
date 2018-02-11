const tempVerbose = true;

const thermometerColorConfigWhite = {
	bgColor: 'white',
	digitColor: 'red',
	withGradient: true,
	displayBackgroundGradient: {from: 'black', to: 'LightGrey'},
	withDisplayShadow: true,
	shadowColor: 'rgba(0, 0, 0, 0.75)',
	majorTickColor: 'DarkGrey',
	minorTickColor: 'DarkGrey',
	valueColor: 'LightRed',
	valueOutlineColor: 'black',
	valueNbDecimal: 2,
	font: 'Arial' /* 'Source Code Pro' */
};

const thermometerColorConfigBlack = {
	bgColor: 'black',
	digitColor: 'red',
	withGradient: true,
	displayBackgroundGradient: {from: 'black', to: 'LightGrey'},
	withDisplayShadow: true,
	shadowColor: 'rgba(0, 0, 0, 0.75)',
	majorTickColor: 'DarkGrey',
	minorTickColor: 'DarkGrey',
	valueColor: 'LightRed',
	valueOutlineColor: 'black',
	valueNbDecimal: 2,
	font: 'Arial' /* 'Source Code Pro' */
};

let thermometerColorConfig = thermometerColorConfigWhite;

/* global HTMLElement */
class Thermometer extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",        // Integer. Canvas width
			"height",       // Integer. Canvas height
			"min-value",    // Float. Min value for temperature
			"max-value",    // Float. Max value for temperature
			"major-ticks",  // Float. value between major ticks (those with labels)
			"minor-ticks",  // Float. value between minor ticks
			"value",        // Float. Temperature to display
			"animate"       // Boolean. smooth animation between different values, or not.
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
		this._height      = 150;
		this._min_value   =   0;
		this._max_value   =  10;
		this._major_ticks =   1;
		this._minor_ticks =   0.25;
		this._animate     = false;

		this._previous_value = 0.0;
		this._value_to_display = 0.0;
		this._interval_ID;

		if (tempVerbose) {
			console.log("Data in Constructor:", this._value);
		}
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (tempVerbose) {
			console.log("connectedCallback invoked, 'value' is [", this.value, "]");
		}
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (tempVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (tempVerbose) {
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
			case "min-value":
				this._min_value = parseFloat(newVal);
				break;
			case "max-value":
				this._max_value = parseFloat(newVal);
				break;
			case "major-ticks":
				this._major_ticks = parseFloat(newVal);
				break;
			case "minor-ticks":
				this._minor_ticks = parseFloat(newVal);
				break;
			case "animate":
				this._animate = (newVal === 'true');
				break;
			default:
				break;
		}
		this.repaint();
	}

	// Called whenever the custom element has been moved into a new document.
	adoptedCallback() {
		if (tempVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set value(option) {
		this.setAttribute("value", option);
		if (tempVerbose) {
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
	set minValue(val) {
		this.setAttribute("min-value", val);
	}
	set maxValue(val) {
		this.setAttribute("max-value", val);
	}
	set majorTicks(val) {
		this.setAttribute("major-ticks", val);
	}
	set minorTicks(val) {
		this.setAttribute("minor-ticks", val);
	}
	set animate(val) {
		this.setAttribute("animate", val);
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
	get minValue() {
		return this._min_value;
	}
	get maxValue() {
		return this._max_value;
	}
	get minorTicks() {
		return this._minor_ticks;
	}
	get majorTicks() {
		return this._major_ticks;
	}
	get animate() {
		return this._animate;
	}

	get shadowRoot() {
		return this._shadowRoot;
	}

	// Component methods
	repaint() {
		if (this.animate === true && this._value !== this._previous_value) {
			this.goAnimate(this._value)
		} else {
			this.drawThermometer(this._value);
		}
	}

	displayAndIncrement(inc, finalValue) {
		//console.log('Tic ' + inc + ', ' + finalValue);
		this.drawThermometer(this._value_to_display);
		this._value_to_display += inc;
		if ((inc > 0 && this._value_to_display > finalValue) || (inc < 0 && this._value_to_display < finalValue)) {
			//  console.log('Stop!')
			window.clearInterval(this._interval_ID);
			this._previous_value = finalValue;
			this.drawThermometer(finalValue);
		}
	};

	goAnimate(finalValue) {
		let value = finalValue;
//  console.log("Reaching Value :" + value + " from " + previousValue);
		let diff = value - this._previous_value;
		this._value_to_display = this._previous_value;

//  console.log(canvasName + " going from " + previousValue + " to " + value);
		let incr = 0;
		if (diff > 0) {
			incr = 0.1; // 0.1 * maxValue; // 0.01 is nicer, but too slow...
		} else {
			incr = -0.1; // -0.1 * maxValue;
		}
		let instance = this;
		this._interval_ID = window.setInterval(function () {
			instance.displayAndIncrement(incr, value);
		}, 50);
	};

	drawThermometer(tempValue) {

		let digitColor = thermometerColorConfig.digitColor;
		let context = this.canvas.getContext('2d');

		if (this.width === 0 || this.height === 0) { // Not visible
			return;
		}
		let radius = 10; // The ball at the bottom. The tube is (radius / 2) wide.

		// Set the canvas size from its container.
		this.canvas.width = this.width;
		this.canvas.height = this.height;

		// Cleanup
		//context.fillStyle = "#ffffff";
		context.fillStyle = thermometerColorConfig.bgColor;
		//context.fillStyle = "transparent";
		context.fillRect(0, 0, this.canvas.width, this.canvas.height);
		//context.fillStyle = 'rgba(255, 255, 255, 0.0)';
		//context.fillRect(0, 0, canvas.width, canvas.height);

		// Bottom of the tube at (canvas.height - 10 - radius)
		let bottomTube = (this.canvas.height - 10 - radius);
		let topTube = 40;// Top of the tube at y = 20

		let tubeLength = bottomTube - topTube;

		// Major Ticks
		context.beginPath();
		for (let i = 0; i <= (this.maxValue -this.minValue); i += this.majorTicks) {
			let xFrom = (this.canvas.width / 2) - 20;
			let yFrom = bottomTube - ((tubeLength) * (i / (this.maxValue - this.minValue)));
			let xTo = (this.canvas.width / 2) + 20;
			let yTo = yFrom;
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
		}
		context.lineWidth = 1;
		context.strokeStyle = thermometerColorConfig.majorTickColor;
		context.stroke();
		context.closePath();

		// Minor Ticks
		if (this.minorTicks > 0) {
			context.beginPath();
			for (let i = 0; i <= (this.maxValue - this.minValue); i += this.minorTicks) {
				let xFrom = (this.canvas.width / 2) - 15;
				let yFrom = bottomTube - ((tubeLength) * (i / (this.maxValue - this.minValue)));
				let xTo = (this.canvas.width / 2) + 15;
				let yTo = yFrom;
				context.moveTo(xFrom, yFrom);
				context.lineTo(xTo, yTo);
			}
			context.lineWidth = 1;
			context.strokeStyle = thermometerColorConfig.minorTickColor;
			context.stroke();
			context.closePath();
		}

		// Tube
		context.beginPath();
		//context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);
		context.arc(this.canvas.width / 2, this.canvas.height - 10 - radius, radius, 5 * Math.PI / 4, 7 * Math.PI / 4, true);
		context.lineTo((this.canvas.width / 2) + (radius * Math.cos(Math.PI / 4)), topTube); // right side of the tube
		context.arc(this.canvas.width / 2, topTube, (radius / 2), 0, Math.PI, true);
		context.lineWidth = 1;

		if (thermometerColorConfig.withGradient) {
			let grd = context.createLinearGradient(0, 5, 0, radius);
			grd.addColorStop(0, thermometerColorConfig.displayBackgroundGradient.from);// 0  Beginning
			grd.addColorStop(1, thermometerColorConfig.displayBackgroundGradient.to);// 1  End
			context.fillStyle = grd;
		}
		if (thermometerColorConfig.withDisplayShadow) {
			context.shadowBlur = 0;
			context.shadowColor = thermometerColorConfig.shadowColor; // 'black';
		}
		context.lineJoin = "round";
		context.fill();
		context.strokeStyle = 'DarkGrey';
		context.stroke();
		context.closePath();

		// Numbers
		context.beginPath();
		for (let i = this.minValue; i <= this.maxValue; i += this.majorTicks) {
			let xTo = (this.canvas.width / 2) + 20;
			let yTo = bottomTube - ((tubeLength) * ((i - this.minValue) / (this.maxValue - this.minValue)));

			context.font = "bold 10px Arial";
			context.fillStyle = digitColor;
			let str = i.toString();
//		len = context.measureText(str).width;
			context.fillText(str, xTo, yTo + 3); // 5: half font size
		}
		context.closePath();

		// Value
		let text = tempValue.toFixed(thermometerColorConfig.valueNbDecimal);
		context.font = "bold 20px Arial";
		let metrics = context.measureText(text);
		let len = metrics.width;

		context.beginPath();
		context.fillStyle = thermometerColorConfig.valueColor;
		context.fillText(text, (this.canvas.width / 2) - (len / 2), ((radius * .75) + 10));
		context.lineWidth = 1;
		context.strokeStyle = thermometerColorConfig.valueOutlineColor;
		context.strokeText(text, (this.canvas.width / 2) - (len / 2), ((radius * .75) + 10)); // Outlined
		context.closePath();

		// Liquid in the tube
		context.beginPath();
		//context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);
		context.arc(this.canvas.width / 2, this.canvas.height - 10 - (radius * 0.75), (radius * 0.75), 5 * Math.PI / 4, 7 * Math.PI / 4, true);
		let y = bottomTube - ((tubeLength) * ((tempValue - this.minValue) / (this.maxValue - this.minValue)));

		context.lineTo((this.canvas.width / 2) + ((radius * 0.75) * Math.cos(Math.PI / 4)), y); // right side of the tube
		context.lineTo((this.canvas.width / 2) - ((radius * 0.75) * Math.cos(Math.PI / 4)), y); // top of the liquid

		context.lineWidth = 1;

		let _grd = context.createLinearGradient(0, topTube, 0, tubeLength);
		_grd.addColorStop(0, 'red');    // 0  Beginning
		_grd.addColorStop(0.6, 'red');
		_grd.addColorStop(0.8, 'blue');
		_grd.addColorStop(1, 'navy');   // 1  End
		context.fillStyle = _grd;

		context.shadowBlur = 20;
		context.shadowColor = 'black';

		context.lineJoin = "round";
		context.fill();
		context.strokeStyle = 'DarkGrey';
		context.stroke();
		context.closePath();
	}
}

// Associate the tag and the class
window.customElements.define('thermo-meter', Thermometer);
