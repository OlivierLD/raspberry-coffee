"use strict";

class Shape {
	constructor(a) {
		this.area = a;
	};
};

class Circle extends Shape {
	disp() {
		console.log("Circle Area is " + this.area);
	};
};

var circle = new Circle(234);
circle.disp();

