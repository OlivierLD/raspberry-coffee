class Polygon {

	constructor(height, width) {
		this._height = height;
		this._width = width;

		console.log("New.Target:", new.target);

		console.log("Target/constructor:", (new.target === Polygon), (new.target === this));

		// Emulate abstract class
		var abstract = false;
		if (new.target === Polygon && abstract) {
			throw new Error("Cannot instantiate that.");
		}
	};

	static blah() {
		console.log("Interesting...");
	};

	test() { // No need to declare it as 'function'
		console.log("Height is " ,this. _height);
		console.log("Width is " , this._width);
	};

	set width(value) { // Setter
		this._width = value;
	};
	get width() {      // Getter
		return this._width;
	};
};

var polygon = new Polygon(10, 20);
polygon.test();

Polygon.blah();

console.log("Is polygon a Polygon? ", (polygon instanceof Polygon));
polygon.width = 30;
polygon.test();
console.log("Width:" + polygon.width);
