const skyMapVerbose = false;
const SKY_MAP_TAG_NAME = 'sky-map';

/**
 * Renders a StarFinder (2102-D) or a Sky Map (like Sirius).
 *
 * Quick hints for the Sky Map:
 * - Put it OVER your head (look at it from underneath).
 * - Align date and SOLAR time of the day to see the visible sky.
 *
 * Parameters:
 * - North or South
 * - With stars
 * - With star names
 * - visible sky
 * - Full sphere
 * - Wandering bodies -> requires a REST service, JSON data like
 * [
 {
	 "name": "sun",
	 "decl": -20.04044686148565,
	 "gha": 155.6529121226147
 },
 {
	 "name": "moon",
	 "decl": 23.16309834765886,
	 "gha": 333.80030094991827
 },
 {
	 "name": "aries",
	 "decl": 0,
	 "gha": 32.94240012811581
 },
 {
	 "name": "venus",
	 "decl": -11.050714772347902,
	 "gha": 187.22301328597055
 },
 {
	 "name": "mars",
	 "decl": -23.63545138636733,
	 "gha": 136.0959874339015
 },
 {
	 "name": "jupiter",
	 "decl": -3.8939103945425586,
	 "gha": 38.329413872258606
 },
 {
	 "name": "saturn",
	 "decl": -3.333562139273032,
	 "gha": 199.44442264190673
 }
 ]
 * - Constellations
 * - Constellation names
 * - Star Finder or Sky Map
 * - hemisphere
 *
 * - Latitude (Observer)
 * - LHA Aries
 * - Displayable star names
 */

/* The map data */
import constellations from "./stars/constellations.js";
import * as Utilities from "./utilities/Utilities.js";
// import constellations from "./stars/constellations"; // minifyJs does NOT like the .js extension

const Hemispheres = {
	NORTHERN_HEMISPHERE: 1,
	SOUTHERN_HEMISPHERE: -1
};

const MapType = {
	STARFINDER_TYPE: 'STARFINDER',
	SKYMAP_TYPE: 'SKYMAP'
};

const Month = {
	JANUARY: {
		name: 'January',
		nbDays: 31
	},
	FEBRUARY: {
		name: 'February',
		nbDays: 28
	},
	MARCH: {
		name: 'March',
		nbDays: 31
	},
	APRIL: {
		name: 'April',
		nbDays: 30
	},
	MAY: {
		name: 'May',
		nbDays: 31
	},
	JUNE: {
		name: 'June',
		nbDays: 30
	},
	JULY: {
		name: 'July',
		nbDays: 31
	},
	AUGUST: {
		name: 'August',
		nbDays: 31
	},
	SEPTEMBER: {
		name: 'September',
		nbDays: 30
	},
	OCTOBER: {
		name: 'October',
		nbDays: 31
	},
	NOVEMBER: {
		name: 'November',
		nbDays: 30
	},
	DECEMBER: {
		name: 'December',
		nbDays: 31
	}
};

/* global HTMLElement */
class SkyMap extends HTMLElement {

	static get observedAttributes() {
		return [
			"width",                  // Integer. Canvas width
			"height",                 // Integer. Canvas height
			"hemisphere",             // String. N or S. Default N
			"type",                   // String SKYMAP or STARFINDER (default STARFINDER)
			"star-names",             // boolean. Default true (major stars only)
			"stars",                  // boolean. Default true.
			"constellation-names",    // boolean. Default false
			"constellations",         // boolean. Default true
			"visible-sky",            // boolean. Default true
			"sky-grid",               // boolean. Default true
			"wandering-bodies",       // boolean. Default false
			"latitude",               // Number [0..90], default 45, no sign! -> see hemisphere
			"lha-aries"               // Number, Default 0.

		];
	}

	static dummyDump() {
		console.log('We have %d constellation(s).', constellations.length);
		for (let i=0; i<constellations.length; i++) {
			console.log("- %s: %d star(s)", constellations[i].name, constellations[i].stars.length);
			if (i === 0) {
				console.log(constellations[i]);
			}
		}
	}

	constructor() {
		super();
		this._shadowRoot = this.attachShadow({mode: 'open'}); // 'open' means it is accessible from external JavaScript.
		// create and append a <canvas>
		this.canvas = document.createElement("canvas");
		let fallbackElemt = document.createElement("h1");
		let content = document.createTextNode("This is a SkyMap or StarFinder, on an HTML5 canvas");
		fallbackElemt.appendChild(content);
		this.canvas.appendChild(fallbackElemt);
		this.shadowRoot.appendChild(this.canvas);

		// For tests of the import
//	this.dummyDump();

		// Default values
		this._width       = 500;
		this._height      = 500;

		this.majorTicks = 5; // prm ?
		this.minorTicks = 1; // prm ?

		this.LHAAries = 0;
		this._hemisphere = Hemispheres.NORTHERN_HEMISPHERE;

		this.observerLatitude = 45;

		this._type = MapType.STARFINDER_TYPE; // SKYMAP_TYPE;
		this._starNames = true;
		this._withStars = true;
		this._constellationNames = false;
		this._withConstellations = true;
		this._withVisibleSky = true;
		this._withSkyGrid = true;
		this._withWanderingBodies = false;
		this._wanderingBodiesData = undefined;
	}

	// Called whenever the custom element is inserted into the DOM.
	connectedCallback() {
		if (skyMapVerbose) {
			console.log("connectedCallback invoked");
		}
		this.repaint();
	}

	// Called whenever the custom element is removed from the DOM.
	disconnectedCallback() {
		if (skyMapVerbose) {
			console.log("disconnectedCallback invoked");
		}
	}

	// Called whenever an attribute is added, removed or updated.
	// Only attributes listed in the observedAttributes property are affected.
	attributeChangedCallback(attrName, oldVal, newVal) {
		if (skyMapVerbose) {
			console.log("attributeChangedCallback invoked on " + attrName + " from " + oldVal + " to " + newVal);
		}
		switch (attrName) {
			case "width":
				this._width = parseInt(newVal);
				break;
			case "height":
				this._height = parseInt(newVal);
				break;
			case "hemisphere":
				this._hemisphere = (newVal === 'S' ? Hemispheres.SOUTHERN_HEMISPHERE : Hemispheres.NORTHERN_HEMISPHERE);
				break;
			case "type":
				this._type = newVal;
				break;
			case "stars":
				this._withStars = (newVal === 'true');
				break;
			case "star-names":
				this._starNames = (newVal === 'true');
				break;
			case "constellations":
				this._withConstellations = (newVal === 'true');
				break;
			case "constellation-names":
				this._constellationNames = (newVal === 'true');
				break;
			case "visible-sky":
				this._withVisibleSky = (newVal === 'true');
				break;
			case "sky-grid":
				this._withSkyGrid = (newVal === 'true');
				break;
			case "wandering-bodies":
				this._withWanderingBodies = (newVal === 'true');
				break;
			case "latitude":
				this.observerLatitude = parseFloat(newVal);
				break;
			case "lha-aries":
				this.LHAAries = parseFloat(newVal);
				break;
			default:
				break;
		}
		this.repaint();
	}

	// Called whenever the custom element has been moved into a new document.
	adoptedCallback() {
		if (skyMapVerbose) {
			console.log("adoptedCallback invoked");
		}
	}

	set width(val) {
		this.setAttribute("width", val);
	}
	set height(val) {
		this.setAttribute("height", val);
	}
	set hemisphere(val) {
		this._hemisphere = (val === 'S' ? Hemispheres.SOUTHERN_HEMISPHERE : Hemispheres.NORTHERN_HEMISPHERE);
	}
	set type(val) {
		this._type = val;
	}
	set stars(val) {
		this._withStars = val;
	}
	set starNames(val) {
		this._starNames = val;
	}
	set constellations(val) {
		this._withConstellations = val;
	}
	set constellationNames(val) {
		this._constellationNames = val;
	}
	set visibleSky(val) {
		this._withVisibleSky = val;
	}
	set skyGrid(val) {
		this._withSkyGrid = val;
	}
	set wanderingBodies(val) {
		this._withWanderingBodies = val;
	}
	set wanderingBodiesData(json) {
		this._wanderingBodiesData = json;
	}
	set latitude(val) {
		this.observerLatitude = val;
	}
	set lhaAries(val) {
		this.LHAAries = val;
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
	get hemisphere() {
		return this._hemisphere;
	}
	get type() {
		return this._type;
	}
	get stars() {
		return this._withStars;
	}
	get starNames() {
		return this._starNames;
	}
	get constellations() {
		return this._withConstellations;
	}
	get constellationNames() {
		return this._constellationNames;
	}
	get visibleSky() {
		return this._withVisibleSky;
	}
	get skyGrid() {
		return this._withSkyGrid;
	}
	get wanderingBodies() {
		return this._withWanderingBodies;
	}
	get wanderingBodiesData() {
		return this._wanderingBodiesData;
	}
	get latitude() {
		return this.observerLatitude;
	}
	get lhaAries() {
		return this.LHAAries;
	}

	get shadowRoot() {
		return this._shadowRoot;
	}

	/*
	 * Component methods
	 */
	repaint() {
		this.drawSkyMap();
	}

	drawSkyMap() {
		let context = this.canvas.getContext('2d');
		context.clearRect(0, 0, this.width, this.height);

		let radius = Math.min(this.width, this.height) * 0.99 / 2;

		// Set the canvas size from its container.
		this.canvas.width = this.width;
		this.canvas.height = this.height;

		context.beginPath();
		// White BG
		context.fillStyle = 'white';
		context.arc(this.canvas.width / 2, this.canvas.height / 2, radius, 0, 2 * Math.PI, false);
		context.fill();
		context.closePath();
		// 2 circles for LHA/Dates
		context.beginPath();
		context.lineWidth = 1;
		context.strokeStyle = 'gray';
		context.arc(this.canvas.width / 2, this.canvas.height / 2, radius * 0.98, 0, 2 * Math.PI, false);
		context.stroke();
		context.closePath();

		context.beginPath();
		context.arc(this.canvas.width / 2, this.canvas.height / 2, radius * 0.92, 0, 2 * Math.PI, false); // This one is the "horizon" (pole abaisse)
		context.stroke();
		context.closePath();


		if (this._type === MapType.STARFINDER_TYPE) { // OPTION StarFinder
			// Major ticks
			context.beginPath();
			for (let i = 0; i < 360; i++) {
				if (i % this.majorTicks === 0) {
					let currentAngle = - Utilities.toRadians(i - (this._hemisphere * this.LHAAries));
					let xFrom = (this.canvas.width / 2) - ((radius * 0.98) * Math.cos(currentAngle));
					let yFrom = (this.canvas.height / 2) - ((radius * 0.98) * Math.sin(currentAngle));
					let xTo = (this.canvas.width / 2) - ((radius * 0.92) * Math.cos(currentAngle));
					let yTo = (this.canvas.height / 2) - ((radius * 0.92) * Math.sin(currentAngle));
					context.moveTo(xFrom, yFrom);
					context.lineTo(xTo, yTo);
				}
			}
			context.lineWidth = 1;
			context.strokeStyle = 'gray';
			context.stroke();
			context.closePath();

			// Minor ticks
			if (this.minorTicks > 0) {
				context.beginPath();
				for (let i = 0; i < 360; i += this.minorTicks) {
					let _currentAngle = - Utilities.toRadians(i - (this._hemisphere * this.LHAAries));

					let xFrom = (this.canvas.width / 2) - ((radius * 0.98) * Math.cos(_currentAngle));
					let yFrom = (this.canvas.height / 2) - ((radius * 0.98) * Math.sin(_currentAngle));
					let xTo = (this.canvas.width / 2) - ((radius * 0.95) * Math.cos(_currentAngle));
					let yTo = (this.canvas.height / 2) - ((radius * 0.95) * Math.sin(_currentAngle));
					context.moveTo(xFrom, yFrom);
					context.lineTo(xTo, yTo);
				}
				context.lineWidth = 1;
				context.strokeStyle = 'gray';
				context.stroke();
				context.closePath();
			}

			// LHA values
			context.beginPath();
			for (let i = 0; i < 360; i++) {
				if (i % this.majorTicks === 0) {
					context.save();
					context.translate(this.canvas.width / 2, (this.canvas.height / 2)); // canvas.height);
					let __currentAngle = - Utilities.toRadians(i - (this._hemisphere * this.LHAAries));
					context.rotate(__currentAngle - Math.PI);
					context.font = "bold " + Math.round(10) + "px Arial"; // Like "bold 15px Arial"
					context.fillStyle = 'black';
					let lha = (this._hemisphere === Hemispheres.NORTHERN_HEMISPHERE || i === 0 ? i : (360 - i));
					let str = lha.toString() + '°';
					let len = context.measureText(str).width;
					context.fillText(str, -len / 2, (-(radius * 0.98) + 10));
					// context.lineWidth = 1;
					// context.strokeStyle = 'black';
					// context.strokeText(str, -len / 2, (-(radius * .8) + 10)); // Outlined
					context.restore();
				}
			}
			context.closePath();
		} else if (this._type === MapType.SKYMAP_TYPE) {
			context.beginPath();
			// 0 is 21 Sept.
			for (let day=1; day<=365; day++) { // No leap year
				let now = SkyMap.findCorrespondingDay(day);
				let angleOnDisk = 360 * ((day - 1) / 365); // The angle in the circle
//			console.log("Day ", day, " => now", JSON.stringify(now), " angle:", angleOnDisk);
				let rad = Utilities.toRadians((angleOnDisk - this.LHAAries) * this._hemisphere);
				let xFrom = (this.canvas.width / 2) - ((radius * 0.98) * Math.cos(rad - (Math.PI / 2)));
				let yFrom = (this.canvas.height / 2) - ((radius * 0.98) * Math.sin(rad - (Math.PI / 2)));
				let xTo = (this.canvas.width / 2) - ((radius * ((now.dayOfMonth === 1 || now.dayOfMonth % 5 === 0) ? 0.92 : 0.95)) * Math.cos(rad - (Math.PI / 2)));
				let yTo = (this.canvas.height / 2) - ((radius * ((now.dayOfMonth === 1 || now.dayOfMonth % 5 === 0) ? 0.92 : 0.95)) * Math.sin(rad - (Math.PI / 2)));
				context.moveTo(xFrom, yFrom);
				context.lineTo(xTo, yTo);
//			console.log("Day ", day, " => now", JSON.stringify(now), " angle:", angleOnDisk, "Rad", rad, "LHA Aries", this.LHAAries, "Hem.", this._hemisphere);
				if (now.dayOfMonth === 1 || now.dayOfMonth % 5 === 0) { // Print the day #
					context.save();
					context.translate(this.canvas.width / 2, (this.canvas.height / 2));
					let __currentAngle = rad;
					context.rotate(__currentAngle - Math.PI);
					context.font = "bold " + Math.round(10) + "px Arial"; // Like "bold 15px Arial"
					context.fillStyle = 'black';
					let str = now.dayOfMonth.toString();
					let len = context.measureText(str).width;
					context.fillText(str, -len / 2, (-(radius * 0.98) + 10));
					context.restore();
				}
				if (now.dayOfMonth === Math.round(now.month.nbDays / 2)) { // Print the month name
					context.save();
					context.translate(this.canvas.width / 2, (this.canvas.height / 2));
					let __currentAngle = rad;
					context.rotate(__currentAngle - Math.PI);
					context.font = "bold " + Math.round(10) + "px Arial"; // Like "bold 15px Arial"
					context.fillStyle = 'red';
					let str = now.month.name;
					let len = context.measureText(str).width;
					context.fillText(str, -len / 2, (-(radius * 1.01) + 10));
					context.restore();
				}
			}
			context.lineWidth = 1;
			context.strokeStyle = 'gray';
			context.stroke();
			context.closePath();
		}

		// Full Sphere
		// Gray BG
		context.beginPath();
		context.fillStyle = 'lightGray';
		context.arc(this.canvas.width / 2, this.canvas.height / 2, radius * 0.92, 0, 2 * Math.PI, false); // This one is the "horizon" (pole abaisse)
		context.fill();
		context.closePath();

		// quarters of hours
		context.beginPath();
		for (let i = 0; i < 96; i++) {
			let currentAngle = Utilities.toRadians(i * (15 / 4));
			let xFrom = (this.canvas.width / 2) - ((radius * 0.92) * Math.cos(currentAngle));
			let yFrom = (this.canvas.height / 2) - ((radius * 0.92) * Math.sin(currentAngle));
			let xTo = (this.canvas.width / 2) - ((radius * 0.90) * Math.cos(currentAngle));
			let yTo = (this.canvas.height / 2) - ((radius * 0.90) * Math.sin(currentAngle));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
		}
		context.lineWidth = 1;
		context.strokeStyle = 'blue';
		context.stroke();
		context.closePath();

		// Hours
		context.beginPath();
		for (let i = 0; i < 24; i++) {
			let currentAngle = Utilities.toRadians(i * 15);
			let xFrom = (this.canvas.width / 2) - ((radius * 0.92) * Math.cos(currentAngle));
			let yFrom = (this.canvas.height / 2) - ((radius * 0.92) * Math.sin(currentAngle));
			let xTo = (this.canvas.width / 2) - ((radius * 0.88) * Math.cos(currentAngle));
			let yTo = (this.canvas.height / 2) - ((radius * 0.88) * Math.sin(currentAngle));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
		}
		context.lineWidth = 2;
		context.strokeStyle = 'blue';
		context.stroke();
		context.closePath();

		// Hour Values
		context.beginPath();
		for (let i = 0; i < 24; i++) {
			context.save();
			context.translate(this.canvas.width / 2, (this.canvas.height / 2)); // canvas.height);
			let __currentAngle = - Utilities.toRadians(i * 15);
			context.rotate(__currentAngle - Math.PI);
			context.font = "bold " + Math.round(10) + "px Arial"; // Like "bold 15px Arial"
			context.fillStyle = 'blue';
			let hour = (this._hemisphere === Hemispheres.NORTHERN_HEMISPHERE  || i === 0 ? i : (24 - i));
			let str = Utilities.lpad(hour.toString(), 2, '0');
			let len = context.measureText(str).width;
			context.fillText(str, -len / 2, (-(radius * .88) + 10));
			// context.lineWidth = 1;
			// context.strokeStyle = 'black';
			// context.strokeText(str, -len / 2, (-(radius * .8) + 10)); // Outlined
			context.restore();
		}
		context.closePath();

		// Visible Sky
		if (this._withVisibleSky) {
			this.drawVisibleSky(context, radius * 0.92);
		}

		// Full Sphere Celestial equator
		context.beginPath();
		context.lineWidth = 2;
		context.strokeStyle = 'gray';
		context.arc(this.canvas.width / 2, this.canvas.height / 2, radius * 0.92 / 2, 0, 2 * Math.PI, false);
		context.stroke();
		context.closePath();

		// Declinations
		context.save();
		context.beginPath();
		context.setLineDash([5]);
		context.lineWidth = 0.5;
		context.strokeStyle = 'black';
		for (let i=-80; i<90; i+=10) {
			if (i === 0) {
				continue;
			}
			context.beginPath();
			let r = Math.round((radius * 0.92 / 2) * (90 - i) / 90);
			context.arc(this.canvas.width / 2, this.canvas.height / 2, r, 0, 2 * Math.PI, false);
			context.stroke();
			context.closePath();
		}
		context.restore();

		if (this._withStars || this._withConstellations) {
			this.drawStars(context, radius * 0.92);
		}

		if (this._withWanderingBodies) {
			this.drawWanderingBodies(context, radius * 0.92);
		}

		// Display LHA Aries as text
		let strAries = Utilities.decToSex(this.LHAAries);
		context.fillStyle = 'silver'; // this.worldmapColorConfig.displayPositionColor; TODO Get color from CSS
		context.font = "bold 16px Arial"; // "bold 40px Arial"
		context.fillText('LHA Aries: ' + strAries, 10, 18);
	}

	static nextMonth(month) {
		let nextMonth = Month.JANUARY;
		let bool = false;
		for (let k in Month) {
			if (bool) {
				nextMonth = Month[k];
				break;
			}
			bool = (Month[k] === month);
		}
		return nextMonth;
	}

	static findCorrespondingDay(d) {
		// Day 1 is September 21st.
		let currMonth = Month.SEPTEMBER;
		let currDay = 21;
		for (let i = 1; i < d; i++) {
			currDay++;
			if (currDay > currMonth.nbDays) {
				currDay = 1;
				currMonth = SkyMap.nextMonth(currMonth);
			}
		}
//	console.log("Day", d, "becomes", currMonth, currDay);
		return { month: currMonth, dayOfMonth: currDay};
	}

	drawVisibleSky(context, radius) {
		// White BG
		context.beginPath();
		context.fillStyle = 'white';
		for (let z=0; z<=360; z+= 0.25) {
			let deadReck = Utilities.deadReckoning({lat: this.observerLatitude, lng: 0}, 90 * 60, -z);
			let point = this.plotCoordinates(deadReck.lat, deadReck.lng, radius);
			if (z === 0) {
				context.moveTo((this.canvas.width / 2) - point.x, (this.canvas.height / 2) + point.y);
			} else {
				context.lineTo((this.canvas.width / 2) - point.x, (this.canvas.height / 2) + point.y);
			}
		}
		context.closePath();
		context.fill();
		context.closePath();

		// Skyline
		context.beginPath();
		context.strokeStyle = 'blue';
		context.lineWidth = 2;
		for (let z=0; z<=360; z+= 0.25) {
			let deadReck = Utilities.deadReckoning({lat: this.observerLatitude, lng: 0}, 90 * 60, -z);
			let point = this.plotCoordinates(deadReck.lat, deadReck.lng, radius);
			if (z === 0) {
				context.moveTo((this.canvas.width / 2) - point.x, (this.canvas.height / 2) + point.y);
			} else {
				context.lineTo((this.canvas.width / 2) - point.x, (this.canvas.height / 2) + point.y);
			}
			if (z % 90 === 0) { // Cardinal points
				context.save();
				context.font = "bold 12px Arial"; // Like "bold 15px Arial"
				context.fillStyle = 'red';
				let str = "";
				let len = 0;
				switch (z) {
					case 0:
						str = (this._hemisphere === Hemispheres.NORTHERN_HEMISPHERE ? "N" : "S");
						len = context.measureText(str).width;
						context.fillText(str, (this.canvas.width / 2) - point.x - (len / 2), (this.canvas.height / 2) + point.y + (this._type === MapType.STARFINDER_TYPE ? -2 : 12));
						break;
					case 90:
						str = "E";
						len = context.measureText(str).width;
						context.fillText(str, (this.canvas.width / 2) - point.x - (len / 2) + (this._hemisphere === Hemispheres.NORTHERN_HEMISPHERE ? 8 : -12), (this.canvas.height / 2) + point.y + 6);
						break;
					case 180:
						str = (this._hemisphere === Hemispheres.NORTHERN_HEMISPHERE ? "S" : "N");
						len = context.measureText(str).width;
						context.fillText(str, (this.canvas.width / 2) - point.x - (len / 2), (this.canvas.height / 2) + point.y + (this._type === MapType.STARFINDER_TYPE ? 12 : -2));
						break;
					case 270:
						str = "W";
						len = context.measureText(str).width;
						context.fillText(str, (this.canvas.width / 2) - point.x - (len / 2) + (this._hemisphere === Hemispheres.NORTHERN_HEMISPHERE ? -12 : 8), (this.canvas.height / 2) + point.y + 6);
						break;
					default:
						break;
				}
				context.restore();
			}
		}
		context.closePath();
		context.stroke();
		context.closePath();

		// Zenith
		context.beginPath();
		let zenith = Math.round(((radius / 2)) * ((90 - this.observerLatitude) / 90));
		if (this._type === MapType.SKYMAP_TYPE) {
			zenith *= -1;
		}
		context.fillStyle = 'blue';
		const zenithRadius = 2;
		context.arc((this.canvas.width / 2), (this.canvas.height / 2) + zenith, zenithRadius, 0, 2 * Math.PI, false);
		context.fill();
		context.closePath();

		if (this._withSkyGrid) {
			// Altitudes
			context.strokeStyle = 'blue';
			context.lineWidth = 0.5;
			for (let dz = 10; dz <= 90; dz += 10) {
				context.beginPath();
				for (let z = 0; z <= 360; z += 0.25) {
					let deadReck = Utilities.deadReckoning({lat: this.observerLatitude, lng: 0}, dz * 60, -z);
					let point = this.plotCoordinates(deadReck.lat, deadReck.lng, radius);
					if (z === 0) {
						context.moveTo((this.canvas.width / 2) - point.x, (this.canvas.height / 2) + point.y);
					} else {
						context.lineTo((this.canvas.width / 2) - point.x, (this.canvas.height / 2) + point.y);
					}
				}
				context.closePath();
				context.stroke();
			}
		}
		// Azimuths in visible sky
		for (let z=0; z<360; z+=(this._withSkyGrid ? 5 : 90)) {
			if (z % 90 === 0) {
				context.lineWidth = 2;
			} else {
				context.lineWidth = 0.5;
			}
			context.beginPath();
			for (let dz = (this._withSkyGrid ? 10 : 0); dz <= 90; dz++) {
				let deadReck = Utilities.deadReckoning({lat: this.observerLatitude, lng: 0}, dz * 60, z);
				let point = this.plotCoordinates(deadReck.lat, deadReck.lng, radius);
				if (dz === (this._withSkyGrid ? 10 : 0)) {
					context.moveTo((this.canvas.width / 2) - point.x, (this.canvas.height / 2) + point.y);
				} else {
					context.lineTo((this.canvas.width / 2) - point.x, (this.canvas.height / 2) + point.y);
				}
			}
			context.stroke();
			context.closePath();
		}
	}

	static findStar(starArray, starName) {
		let star = {};
		for (let i=0; i<starArray.length; i++) {
			if (starArray[i].name === starName) {
				return starArray[i];
			}
		}
		return star;
	}

	drawStars(context, radius) {
		for (let i=0; i<constellations.length; i++) {
			// Constellation?
			if (this._withConstellations) {
				let constellation = constellations[i].lines;
				for (let l = 0; l < constellation.length; l++) {
					let starFrom = SkyMap.findStar(constellations[i].stars, constellations[i].lines[l].from);
					let starTo = SkyMap.findStar(constellations[i].stars, constellations[i].lines[l].to);
					if (starFrom !== {} && starTo !== {}) {
						context.beginPath();
						let dec = starFrom.d * this._hemisphere;
						let ra = starFrom.ra;
						let lng = (360 - (ra * 360 / 24));
						lng += (/*this._hemisphere * */this.LHAAries);
						if (lng > 180) {
							lng -= 360;
						}
						let p1 = this.plotCoordinates(dec, lng, radius);
						dec = starTo.d * this._hemisphere;
						ra = starTo.ra;
						lng = (360 - (ra * 360 / 24));
						lng += (/*this._hemisphere * */this.LHAAries);
						if (lng > 180) {
							lng -= 360;
						}
						let p2 = this.plotCoordinates(dec, lng, radius);
						context.strokeStyle = 'black';
						context.lineWidth = 0.5;
						// (this._type === MapType.STARFINDER_TYPE ? 1 : -1 )
						context.moveTo((this.canvas.width / 2) - p1.x, (this.canvas.height / 2) + p1.y);
						context.lineTo((this.canvas.width / 2) - p2.x, (this.canvas.height / 2) + p2.y);

						context.stroke();
						context.closePath();
					}
				}
				if (this._constellationNames) {
					// Calculate the center of the constellation
					let minD = undefined, maxD = undefined, minRA = undefined, maxRA = undefined;
					for (let s = 0; s < constellations[i].stars.length; s++) {
						if (s === 0) {
							minD = constellations[i].stars[s].d;
							maxD = constellations[i].stars[s].d;
							minRA = constellations[i].stars[s].ra;
							maxRA = constellations[i].stars[s].ra;
						} else {
							minD = Math.min(constellations[i].stars[s].d, minD);
							maxD = Math.max(constellations[i].stars[s].d, maxD);
							minRA = Math.min(constellations[i].stars[s].ra, minRA);
							maxRA = Math.max(constellations[i].stars[s].ra, maxRA);
						}
					}
					let centerDec = this._hemisphere * (maxD + minD) / 2;
					let centerRA = (maxRA + minRA) / 2;
					let lng = (360 - (centerRA * 360 / 24));
					lng += (/*this._hemisphere * */this.LHAAries);
					if (lng > 180) {
						lng -= 360;
					}
					let p = this.plotCoordinates(centerDec, lng, radius);
					context.font = "bold " + Math.round(10) + "px Arial"; // Like "bold 15px Arial"
					context.fillStyle = 'blue';
					let str = constellations[i].name;
					let len = context.measureText(str).width;
					context.fillText(str, (this.canvas.width / 2) - p.x - (len / 2), (this.canvas.height / 2) + p.y - 2);
				}
			}

			// Stars
			if (this._withStars) {
				for (let s = 0; s < constellations[i].stars.length; s++) {
					let dec = constellations[i].stars[s].d * this._hemisphere;
					let ra = constellations[i].stars[s].ra;
					let lng = (360 - (ra * 360 / 24));
					lng += (/*this._hemisphere * */this.LHAAries);
					if (lng > 180) {
						lng -= 360;
					}
					let p = this.plotCoordinates(dec, lng, radius);
					context.beginPath();
					context.fillStyle = 'gold';
					const starRadius = 2;
					context.arc((this.canvas.width / 2) - p.x, (this.canvas.height / 2) + p.y, starRadius, 0, 2 * Math.PI, false);
					context.fill();
					context.strokeStyle = 'black';
					context.lineWidth = 0.5;
					context.stroke();

					if (constellations[i].stars[s].name.charAt(0) === constellations[i].stars[s].name.charAt(0).toUpperCase() && this._starNames) { // Star name, starts with uppercase
						context.font = "bold " + Math.round(10) + "px Arial"; // Like "bold 15px Arial"
						context.fillStyle = 'blue';
						let str = constellations[i].stars[s].name;
						let len = context.measureText(str).width;
						context.fillText(str, (this.canvas.width / 2) - p.x - (len / 2), (this.canvas.height / 2) + p.y - 2);
					}
					context.closePath();
				}
			}
		}
	}

	static findGHAAries(wBodies) {
		let ghaA = undefined;
		for (let i=0; i<wBodies.length; i++) {
			if (wBodies[i].name === "aries") {
				return wBodies[i].gha;
			}
		}
		return ghaA;
	}

	/*
	 * Sun     \u2609
	 * Moon    \u263D, \u263E
	 * Venus   \u2640
	 * Mars    \u2642
	 * Jupiter \u2643
	 * Saturn  \u2644
	 *
	 * Aries (Gamma) \u03b3
	 */
	static findSymbol(bodyName) {
		switch (bodyName.toUpperCase()) {
			case 'ARIES':
				return '\u03b3';
			case 'SUN':
				return '\u2609';
			case 'MOON':
				return '\u263D';
			case 'VENUS':
				return '\u2640';
			case 'MARS':
				return '\u2642';
			case 'JUPITER':
				return '\u2643';
			case 'SATURN':
				return '\u2644';
			default:
				return bodyName;
		}
	}

	drawWanderingBodies(context, radius) {
		if (this._wanderingBodiesData !== undefined) {
			let self = this;
			let ghaAries = SkyMap.findGHAAries(this._wanderingBodiesData);
			this._wanderingBodiesData.forEach((body) => {
				let dec = body.decl * self._hemisphere;
				let lng = body.gha - ghaAries;
				lng += (/*this._hemisphere * */self.LHAAries);
				if (lng > 180) {
					lng -= 360;
				}
				let p = self.plotCoordinates(dec, lng, radius);
				context.beginPath();
				context.fillStyle = 'cyan';
				const bodyRadius = 4;
				context.arc((self.canvas.width / 2) - p.x, (self.canvas.height / 2) + p.y, bodyRadius, 0, 2 * Math.PI, false);
				context.fill();
				context.strokeStyle = 'black';
				context.lineWidth = 0.5;
				context.stroke();

				context.font = "bold " + Math.round(24) + "px Arial"; // Like "bold 15px Arial"
				context.fillStyle = 'red';
				let str = SkyMap.findSymbol(body.name);
				let len = context.measureText(str).width;
				context.fillText(str, (self.canvas.width / 2) - p.x - (len / 2), (self.canvas.height / 2) + p.y - 4);

				context.closePath();
			});
		} else {
			console.log("No wandering bodies data available");
		}
	}

	plotCoordinates(lat, lng, radius) {
		let r = (((90 - lat) / 180) * radius);
		let xOffset = Math.round(r * Math.sin(Utilities.toRadians(lng))) * this._hemisphere;
		let yOffset = Math.round(r * Math.cos(Utilities.toRadians(lng)));
		if (this._type === MapType.SKYMAP_TYPE) {
			yOffset *= -1;
		}
		return {x: xOffset, y: yOffset};
	}
}

// Associate the tag and the class
window.customElements.define(SKY_MAP_TAG_NAME, SkyMap);
