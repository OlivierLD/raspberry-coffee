"use strict";

class PrinterClass {
	doPrint() {
		console.log("Printing from parent");
	};
};

class StringPrinter extends PrinterClass {
	doPrint() {
		super.doPrint();
		console.log("Printing from subClass");
	};
};

var obj = new StringPrinter();
obj.doPrint();
