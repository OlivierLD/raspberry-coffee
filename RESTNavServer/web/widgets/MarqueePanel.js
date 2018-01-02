const NB_LINES = 32;
const NB_COLS = 128;

function MarqueePanel(cName, nbColumns, nbLines, bgColor, fgColor) {

	const FONT_SIZE = 10; // Cannot be changed.
	var characters = [];

	this.bg = (bgColor || 'black');
	this.fg = (fgColor || 'red');

	this.w = (nbColumns !== undefined ? nbColumns : NB_COLS); // Actual values, defaulted to SSD1306
	this.h = (nbLines !== undefined ? nbLines : NB_LINES);

	// This represents the led array (128x32). 'X' means on, ' ' means off.
	// The dumpScreen method displays this one.
	var screenMatrix = []; // 2D array of characters

	var canvasName = cName;
	var canvas = document.getElementById(canvasName);
	var context = canvas.getContext('2d');

	const ledRadius = canvas.width / (2 * this.w);

	var findCharacter = function(char) {
		var matrix;
		for (var c=0; c<characters.length; c++) {
			if (characters[c].key === char) { //} || characters[c].key === char.toLowerCase()) {
				matrix = characters[c].matrix;
				break;
			}
		}
		return matrix;
	};

	this.init = function () {
		for (var y=0; y<this.h; y++) {
			var line = [];
			for (var x=0; x<this.w; x++) {
				line.push(' ');
			}
			screenMatrix.push(line);
		}

		characters.push({
			key: " ",
			matrix: ["   ",
				"   ",
				"   ",
				"   ",
				"   ",
				"   ",
				"   ",
				"   ",
				"   ",
				"   "]
		});

		characters.push({
			key: ".", matrix: ["    ",
				"    ",
				"    ",
				"    ",
				"    ",
				"    ",
				"    ",
				" XX ",
				"    ",
				"    "]
		});

		characters.push({
			key: "'", matrix: ["     ",
				"  XX ",
				"  X  ",
				" X   ",
				"     ",
				"     ",
				"     ",
				"     ",
				"     ",
				"     "]
		});

		characters.push({
			key: "\"", matrix: ["     ",
				"     ",
				" X X ",
				" X X ",
				" X X ",
				"     ",
				"     ",
				"     ",
				"     ",
				"     "]
		});

		characters.push({
			key: "!", matrix: ["     ",
				"     ",
				" XX  ",
				" XX  ",
				" XX  ",
				" XX  ",
				"     ",
				" XX  ",
				"     ",
				"     "]
		});

		characters.push({
			key: "?", matrix: ["       ",
				"       ",
				"  XXX  ",
				" X  XX ",
				"   XX  ",
				"  XX   ",
				"       ",
				"  XX   ",
				"       ",
				"       "]
		});

		characters.push({
			key: "\u00b0", matrix: ["     ",
				" XXX ",
				" X X ",
				" XXX ",
				"     ",
				"     ",
				"     ",
				"     ",
				"     ",
				"     "]
		});

		characters.push({
			key: "-", matrix: ["      ",
				"      ",
				"      ",
				"      ",
				" XXXXX",
				"      ",
				"      ",
				"      ",
				"      "]
		});

		characters.push({
			key: "+", matrix: ["        ",
				"        ",
				"        ",
				"    X   ",
				"    X   ",
				"  XXXXX ",
				"    X   ",
				"    X   ",
				"        ",
				"        "]
		});

		characters.push({
			key: "=", matrix: ["      ",
				"      ",
				"      ",
				"      ",
				" XXXX ",
				"      ",
				" XXXX ",
				"      ",
				"      ",
				"      "]
		});

		characters.push({
			key: ":", matrix: ["    ",
				"    ",
				"    ",
				"    ",
				" XX ",
				"    ",
				" XX ",
				"    ",
				"    ",
				"    "]
		});

		characters.push({
			key: "@", matrix: ["       ",
				"       ",
				"  XXX  ",
				" XX  X ",
				" X  XX ",
				" X X X ",
				" X X X ",
				" XX XXX",
				"  XXX  ",
				"       "]
		});

		characters.push({
			key: "#", matrix: ["      ",
				"      ",
				" X X  ",
				" X X  ",
				"XXXXX ",
				" X X  ",
				"XXXXX ",
				" X X  ",
				" X X  ",
				"      "]
		});

		characters.push({
			key: "$", matrix: ["  X   ",
				" XXXX ",
				"XX  X ",
				"XXXX  ",
				" XXXX ",
				"   XX ",
				"XX XX ",
				" XXXX ",
				"  X   ",
				"      "]
		});

		characters.push({
			key: "%", matrix: ["      ",
				"XXX   ",
				"X X X ",
				"XXXX  ",
				"  X   ",
				" XXXX ",
				"X X X ",
				"  XXX ",
				"      ",
				"      "]
		});

		characters.push({
			key: "^", matrix: ["     ",
				"  X  ",
				" XXX ",
				"XX XX",
				"     ",
				"     ",
				"     ",
				"     ",
				"     ",
				"     "]
		});

		characters.push({
			key: "&", matrix: ["       ",
				"       ",
				"  XXX  ",
				" XX    ",
				"  XX   ",
				" XXXXX ",
				"XX XX  ",
				" XXXXX ",
				"       ",
				"       "]
		});

		characters.push({
			key: "*", matrix: ["     ",
				"  X  ",
				"XXXX ",
				" XX  ",
				"X  X ",
				"     ",
				"     ",
				"     ",
				"     ",
				"     "]
		});

		characters.push({
			key: "(", matrix: ["     ",
				"   X ",
				"  X  ",
				" XX  ",
				" XX  ",
				" XX  ",
				" XX  ",
				"  X  ",
				"   X ",
				"     "]
		});

		characters.push({
			key: ")", matrix: ["      ",
				"  X   ",
				"   X  ",
				"   XX ",
				"   XX ",
				"   XX ",
				"   XX ",
				"   X  ",
				"  X   ",
				"      "]
		});

		characters.push({
			key: "_", matrix: ["       ",
				"       ",
				"       ",
				"       ",
				"       ",
				"       ",
				"       ",
				"       ",
				"       ",
				"XXXXXX "]
		});

		characters.push({
			key: "<", matrix: ["      ",
				"      ",
				"   XX ",
				"  XX  ",
				" XX   ",
				"  XX  ",
				"   XX ",
				"      ",
				"      ",
				"      "]
		});

		characters.push({
			key: ">", matrix: ["      ",
				"      ",
				" XX   ",
				"  XX  ",
				"   XX ",
				"  XX  ",
				" XX   ",
				"      ",
				"      ",
				"      "]
		});

		characters.push({
			key: "{", matrix: ["      ",
				"   XX ",
				"  XX  ",
				"  XX  ",
				" XX   ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				"   XX ",
				"      "]
		});

		characters.push({
			key: "}", matrix: ["      ",
				" XX   ",
				"  XX  ",
				"  XX  ",
				"   XX ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				" XX   ",
				"      "]
		});

		characters.push({
			key: "|", matrix: ["     ",
				"     ",
				"  X  ",
				"  X  ",
				"  X  ",
				"  X  ",
				"  X  ",
				"  X  ",
				"  X  ",
				"     "]
		});

		characters.push({
			key: "[", matrix: ["      ",
				"  XXX ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				"  XXX ",
				"      "]
		});

		characters.push({
			key: "]", matrix: ["     ",
				" XXX ",
				"  XX ",
				"  XX ",
				"  XX ",
				"  XX ",
				"  XX ",
				"  XX ",
				" XXX ",
				"     "]
		});

		characters.push({
			key: "\\", matrix: ["      ",
				" X    ",
				" X    ",
				"  X   ",
				"  X   ",
				"   X  ",
				"   X  ",
				"    X ",
				"    X ",
				"      "]
		});

		characters.push({
			key: "/", matrix: ["      ",
				"    X ",
				"    X ",
				"   X  ",
				"   X  ",
				"  X   ",
				"  X   ",
				" X    ",
				" X    ",
				"      "]
		});

		characters.push({
			key: ",", matrix: ["    ",
				"    ",
				"    ",
				"    ",
				"    ",
				"    ",
				"    ",
				" XX ",
				" X  ",
				"X   "]
		});

		characters.push({
			key: ";", matrix: ["    ",
				"    ",
				"    ",
				"    ",
				" XX ",
				"    ",
				"    ",
				" XX ",
				" X  ",
				"X   "]
		});

/// TODO Add up to 10 lines for all below
		characters.push({
			key: "0", matrix: ["      ",
				" XXX  ",
				"XX XX ",
				"XX XX ",
				"XX XX ",
				"XX XX ",
				"XX XX ",
				" XXX  ",
				"      ",
				"      "]
		});

		characters.push({
			key: "1", matrix: ["      ",
				"  XX  ",
				"XXXX  ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				"XXXXXX",
				"      ",
				"      "]
		});

		characters.push({
			key: "2", matrix: ["      ",
				" XXX  ",
				"XX XX ",
				"   XX ",
				"  XX  ",
				" XX   ",
				"XX XX ",
				"XXXXX ",
				"      ",
				"      "]
		});

		characters.push({
			key: "3", matrix: ["      ",
				" XXX  ",
				"XX XX ",
				"   XX ",
				" XXX  ",
				"   XX ",
				"XX XX ",
				" XXX  ",
				"      ",
				"      "]
		});

		characters.push({
			key: "4", matrix: ["      ",
				"   XX ",
				"  XXX ",
				" X XX ",
				"XX XX ",
				"XXXXXX",
				"   XX ",
				"   XX ",
				"      ",
				"      "]
		});

		characters.push({
			key: "5", matrix: ["      ",
				"XXXXX ",
				"XX    ",
				"XXXX  ",
				"XX XX ",
				"   XX ",
				"X  XX ",
				"XXXX  "]
		});

		characters.push({
			key: "6", matrix: ["      ",
				" XXX  ",
				"XX XX ",
				"XX    ",
				"XXXX  ",
				"XX XX ",
				"XX XX ",
				" XXX  ",
				"      ",
				"      "]
		});

		characters.push({
			key: "7", matrix: ["      ",
				"XXXXX ",
				"XX XX ",
				"   XX ",
				"  XX  ",
				"  XX  ",
				" XX   ",
				"XX    "]
		});

		characters.push({
			key: "8", matrix: ["      ",
				" XXX  ",
				"XX XX ",
				"XX XX ",
				" XXX  ",
				"XX XX ",
				"XX XX ",
				" XXX  ",
				"      ",
				"      "]
		});

		characters.push({
			key: "9", matrix: ["      ",
				" XXX  ",
				"XX XX ",
				"XX XX ",
				" XXXX ",
				"   XX ",
				"XX XX ",
				" XXX  ",
				"      ",
				"      "]
		});

		characters.push({
			key: "a", matrix: ["      ",
				"      ",
				"      ",
				" XXX  ",
				"XX XX ",
				" XXXX ",
				"XX XX ",
				"XXXXXX",
				"      ",
				"      "]
		});

		characters.push({
			key: "b", matrix: ["       ",
				"XXX    ",
				" XX    ",
				" XXXX  ",
				" XX XX ",
				" XX XX ",
				" XX XX ",
				"XXXXX  ",
				"       ",
				"       "]
		});

		characters.push({
			key: "c", matrix: ["      ",
				"      ",
				"      ",
				" XXX  ",
				"XX XX ",
				"XX    ",
				"XX XX ",
				" XXX  ",
				"      ",
				"      "]
		});

		characters.push({
			key: "d", matrix: ["      ",
				"  XXX ",
				"   XX ",
				" XXXX ",
				"XX XX ",
				"XX XX ",
				"XX XX ",
				" XXXXX",
				"      ",
				"      "]
		});

		characters.push({
			key: "e", matrix: ["      ",
				"      ",
				"      ",
				" XXX  ",
				"XX XX ",
				"XXXXX ",
				"XX    ",
				" XXXX ",
				"      ",
				"      "]
		});

		characters.push({
			key: "f", matrix: ["     ",
				"  XXX",
				" XX  ",
				"XXXXX",
				" XX  ",
				" XX  ",
				" XX  ",
				"XXXX ",
				"     ",
				"     "]
		});

		characters.push({
			key: "g", matrix: ["     ",
				"     ",
				"     ",
				" XX X",
				"XX XX",
				"XX XX",
				"XX XX",
				" XXXX",
				"   XX",
				"XXXX "]
		});

		characters.push({
			key: "h", matrix: ["       ",
				"XXX    ",
				" XX    ",
				" XXXX  ",
				" XX XX ",
				" XX XX ",
				" XX XX ",
				" XX XX ",
				"       ",
				"       "]
		});

		characters.push({
			key: "i", matrix: ["      ",
				"  XX  ",
				"      ",
				"XXXX  ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				"XXXXXX",
				"      ",
				"      "]
		});

		characters.push({
			key: "j", matrix: ["     ",
				"  XX ",
				"     ",
				"XXXX ",
				"  XX ",
				"  XX ",
				"  XX ",
				"  XX ",
				"  XX ",
				"XXX  "]
		});

		characters.push({
			key: "k", matrix: ["      ",
				"XXX   ",
				" XX   ",
				" XX XX",
				" XXXX ",
				" XXX  ",
				" XXXX ",
				" XX XX",
				"      ",
				"      "]
		});

		characters.push({
			key: "l", matrix: ["     ",
				"XXXX ",
				"  XX ",
				"  XX ",
				"  XX ",
				"  XX ",
				"  XX ",
				"XXXXX",
				"     ",
				"     "]
		});

		characters.push({
			key: "m", matrix: ["      ",
				"      ",
				"      ",
				"XXXXX ",
				" XXXXX",
				" X X X",
				" X X X",
				" X X X",
				"      ",
				"      "]
		});

		characters.push({
			key: "n", matrix: ["       ",
				"       ",
				"       ",
				"XX XX  ",
				" XX XX ",
				" XX XX ",
				" XX XX ",
				" XX XX ",
				"       ",
				"       "]
		});

		characters.push({
			key: "o", matrix: ["     ",
				"     ",
				"     ",
				" XXX ",
				"XX XX",
				"XX XX",
				"XX XX",
				" XXX ",
				"     ",
				"     "]
		});

		characters.push({
			key: "p", matrix: ["       ",
				"       ",
				"       ",
				"XXXXX  ",
				" XX XX ",
				" XX XX ",
				" XX XX ",
				" XXXX  ",
				" XX    ",
				"XXXX   "]
		});

		characters.push({
			key: "q", matrix: ["      ",
				"      ",
				"      ",
				" XX X ",
				"XX XX ",
				"XX XX ",
				"XX XX ",
				" XXXX ",
				"   XX ",
				"  XXXX"]
		});

		characters.push({
			key: "r", matrix: ["      ",
				"      ",
				"      ",
				"XX XXX",
				" XXX X",
				" XX   ",
				" XX   ",
				"XXXX  ",
				"      ",
				"      "]
		});

		characters.push({
			key: "s", matrix: ["      ",
				"      ",
				"      ",
				" XXXX ",
				"XXX   ",
				" XXXX ",
				"   XXX",
				"XXXXX ",
				"      ",
				"      "]
		});

		characters.push({
			key: "t", matrix: ["     ",
				" XX  ",
				" XX  ",
				"XXXXX",
				" XX  ",
				" XX  ",
				" XX X",
				"  XXX",
				"     ",
				"     "]
		});

		characters.push({
			key: "u", matrix: ["      ",
				"      ",
				"      ",
				"XXX XX",
				" XX XX",
				" XX XX",
				" XX XX",
				"  XXXX",
				"      ",
				"      "]
		});

		characters.push({
			key: "v", matrix: ["       ",
				"       ",
				"       ",
				"XXX XXX",
				" XX XX ",
				"  XXX  ",
				"  XXX  ",
				"   X   ",
				"       ",
				"       "]
		});

		characters.push({
			key: "w", matrix: ["      ",
				"      ",
				"      ",
				"X X XX",
				"X X X ",
				"XXXXX ",
				" XXXX ",
				"  X X ",
				"      ",
				"      "]
		});

		characters.push({
			key: "x", matrix: ["      ",
				"      ",
				"      ",
				"XXX XX",
				" XXXX ",
				"  XX  ",
				" XXXXX",
				"XX  XX",
				"      ",
				"      "]
		});

		characters.push({
			key: "y", matrix: ["       ",
				"       ",
				"       ",
				"XXX XXX",
				" XX XX ",
				" XX XX ",
				"  X X  ",
				"  XXX  ",
				"  XX   ",
				"XXX    "]
		});

		characters.push({
			key: "z", matrix: ["     ",
				"     ",
				"     ",
				"XXXXX",
				"X XX ",
				" XX  ",
				"XX XX",
				"XXXXX",
				"     ",
				"     "]
		});

		characters.push({
			key: "A", matrix: ["      ",
				"      ",
				" XXXX ",
				"  XXX ",
				"  X X ",
				" XXXXX",
				" XX XX",
				"XXX XX",
				"       ",
				"       "]
		});

		characters.push({
			key: "B", matrix: ["       ",
				"       ",
				"XXXXX  ",
				" XX XX ",
				" XXXX  ",
				" XX XX ",
				" XX XX ",
				"XXXXX  ",
				"       ",
				"       "]
		});

		characters.push({
			key: "C", matrix: ["     ",
				"     ",
				" XXXX",
				"XX XX",
				"XX   ",
				"XX   ",
				"XX XX",
				" XXX ",
				"     ",
				"     "]
		});

		characters.push({
			key: "D", matrix: ["      ",
				"      ",
				"XXXXX ",
				" XX XX",
				" XX XX",
				" XX XX",
				" XX XX",
				"XXXXX ",
				"      ",
				"      "]
		});

		characters.push({
			key: "E", matrix: ["      ",
				"      ",
				"XXXXXX",
				" XX   ",
				" XXXX ",
				" XX   ",
				" XX XX",
				"XXXXXX",
				"      ",
				"      "]
		});

		characters.push({
			key: "F", matrix: ["      ",
				"      ",
				"XXXXXX",
				" XX   ",
				" XXXX ",
				" XX   ",
				" XX   ",
				"XXXX  ",
				"      ",
				"      "]
		});

		characters.push({
			key: "G", matrix: ["     ",
				"     ",
				" XXX ",
				"XX XX",
				"XX   ",
				"XXXXX",
				"XX XX",
				" XXXX",
				"     ",
				"     "]
		});

		characters.push({
			key: "H", matrix: ["      ",
				"      ",
				"XXX XX",
				" XX XX",
				" XXXXX",
				" XX XX",
				" XX XX",
				"XXX XX",
				"      ",
				"      "]
		});

		characters.push({
			key: "I", matrix: ["      ",
				"      ",
				"XXXXX ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				"XXXXX ",
				"      ",
				"      "]
		});

		characters.push({
			key: "J", matrix: ["     ",
				"     ",
				" XXXX",
				"  XX ",
				"  XX ",
				"X XX ",
				"X XX ",
				"XXX  ",
				"     ",
				"     "]
		});

		characters.push({
			key: "K", matrix: ["      ",
				"      ",
				"XXX XX",
				" XX X ",
				" XXX  ",
				" XXXX ",
				" XX XX",
				"XXXX X",
				"      ",
				"      "]
		});

		characters.push({
			key: "L", matrix: ["      ",
				"      ",
				"XXXX  ",
				" XX   ",
				" XX   ",
				" XX   ",
				" XX XX",
				"XXXXXX",
				"      ",
				"      "]
		});

		characters.push({
			key: "M", matrix: ["       ",
				"       ",
				"XX   XX",
				" XX XX ",
				" XX XX ",
				" XXXXX ",
				" X X X ",
				"XX X XX",
				"       ",
				"       "]
		});

		characters.push({
			key: "N", matrix: ["      ",
				"      ",
				"XX XXX",
				"XXX X ",
				"XXX X ",
				"XX XX ",
				"XX XX ",
				"XX  X ",
				"      ",
				"      "]
		});

		characters.push({
			key: "O", matrix: ["      ",
				"      ",
				" XXX  ",
				"XX XX ",
				"XX XX ",
				"XX XX ",
				"XX XX ",
				" XXX  ",
				"      ",
				"      "]
		});

		characters.push({
			key: "P", matrix: ["       ",
				"       ",
				"XXXXX  ",
				" XX XX ",
				" XX XX ",
				" XXXX  ",
				" XX    ",
				"XXXX   ",
				"       ",
				"       "]
		});

		characters.push({
			key: "Q", matrix: ["     ",
				"     ",
				" XXX ",
				"XX XX",
				"XX XX",
				"XX XX",
				"XX XX",
				" XXX ",
				"   XX",
				"     "]
		});

		characters.push({
			key: "R", matrix: ["       ",
				"       ",
				"XXXXX  ",
				" XX XX ",
				" XX XX ",
				" XXXX  ",
				" XX XX ",
				"XXXX XX",
				"       ",
				"       "]
		});

		characters.push({
			key: "S", matrix: ["     ",
				"     ",
				" XXXX",
				"XX  X",
				"XXXX  ",
				"  XXX",
				"X  XX",
				"XXXX ",
				"     ",
				"     "]
		});

		characters.push({
			key: "T", matrix: ["      ",
				"      ",
				"XXXXXX",
				"X XX X",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				" XXXX ",
				"      ",
				"      "]
		});

		characters.push({
			key: "U", matrix: ["       ",
				"       ",
				"XXX XXX",
				" XX XX ",
				" XX XX ",
				" XX XX ",
				" XX XX ",
				"  XXX  ",
				"       ",
				"       "]
		});

		characters.push({
			key: "V", matrix: ["       ",
				"       ",
				"XXX XXX",
				" XX XX ",
				" XX XX ",
				"  XXX  ",
				"  XXX  ",
				"   X   ",
				"       ",
				"       "]
		});

		characters.push({
			key: "W", matrix: ["       ",
				"       ",
				"XX X XX",
				" X X X ",
				" X X X ",
				" XXXXX ",
				"  XXX  ",
				"  X X  ",
				"       ",
				"       "]
		});

		characters.push({
			key: "X", matrix: ["      ",
				"      ",
				"XX  XX",
				" XXXX ",
				"  XX  ",
				"  XX  ",
				" XXXX ",
				"XX  X ",
				"      ",
				"      "]
		});

		characters.push({
			key: "Y", matrix: ["      ",
				"      ",
				"XX  XX",
				"XX  XX",
				" XXXX ",
				"  XX  ",
				"  XX  ",
				" XXXX ",
				"      ",
				"      "]
		});

		characters.push({
			key: "Z", matrix: ["     ",
				"     ",
				"XXXXX",
				"XX XX",
				"  XX ",
				" XX  ",
				"XX XX",
				"XXXXX",
				"     ",
				"     "]
		});

	};

	this.init();
//console.log("Loaded " + characters.length + " characters");

	var Mode = {
		WHITE_ON_BLACK: 1,
		BLACK_ON_WHITE: 2
	};

	this.clear = function(mode) {
		mode = mode || Mode.WHITE_ON_BLACK;

		for (var i = 0; i < this.h; i++) {
			for (var j = 0; j < this.w; j++) {
				screenMatrix[i][j] = (mode === Mode.WHITE_ON_BLACK ? ' ' : 'X');
			}
		}
	};


	var fillCircle = function(context, pt, radius, color) {
		var grd = context.createRadialGradient(pt.x - (radius / 3), pt.y - (radius / 3), radius / 3, pt.x, pt.y, radius);
		grd.addColorStop(0, "red");
		grd.addColorStop(1, "orange");

		context.beginPath();
		context.fillStyle = grd; // color;
		context.arc(pt.x, pt.y, radius, 0, radius * Math.PI);
		context.fill();
		context.closePath();
	};

	this.repaint = function() {

		context.beginPath();
		context.fillStyle = this.bg;
		context.fillRect(0, 0, canvas.width, canvas.height);


		var xStep = Math.round(canvas.width / this.w);
		var yStep = Math.round(canvas.height / this.h);
		for (var x=0; x<this.w; x++) {
			for (var y=0; y<this.h; y++) {
//			context.beginPath();
//			context.fillStyle = (screenMatrix[y][x] === ' ' ? this.bg : this.fg);
//			context.fillRect(x * xStep, y * yStep, xStep, yStep); // TODO: Better. BG + Round led
				if (screenMatrix[y][x] !== ' ') {
					fillCircle(context, {x: (x * xStep) + (xStep / 2), y: (y * yStep) + (yStep / 2)}, ledRadius, this.fg);
				}
//			context.closePath();
			}
		}
	};

	var invert = function(c) {
		return (c === ' ' ? 'X' : ' ');
	};

	this.text = function(txt, xPx, yPx, fontFact, mode, rotate) {

		fontFact = fontFact || 1;
		mode = mode || Mode.WHITE_ON_BLACK;
		rotate = rotate || false;

		var screenColumn = xPx;
		for (var i = 0; i < txt.length; i++) {         // For each character of the string to display
			var c = txt.charAt(i);
			var matrix = findCharacter(c);

			if (matrix !== undefined) {
				// Assume all pixel lines have the same length
				for (var x = 0; x < matrix[0].length; x++) { // Each COLUMN of the character matrix
					for (var factX = 0; factX < fontFact; factX++) {
						var verticalBitmap = [];
						var vmY = 0;
						for (var my = 0; my < matrix.length; my++) {  // Each LINE of the character matrix
							for (var factY = 0; factY < fontFact; factY++) {
								verticalBitmap.push(matrix[my].charAt(x));
							}
						}
						// Write the character in the screen matrix
						// screenMatrix[line][col]
						for (var y = 0; y < (fontFact * FONT_SIZE); y++) { // One-character vertical bitmap
							var screenLine = (y + yPx - (FONT_SIZE - 1));
							if (!rotate) {
								if (screenLine >= 0 && screenLine < this.h && screenColumn >= 0 && screenColumn < this.w) {
									screenMatrix[screenLine][screenColumn] = (mode === Mode.WHITE_ON_BLACK ? verticalBitmap[y] : invert(verticalBitmap[y]));
								}
							} else { // 90 deg counter-clockwise
								if (screenLine >= 0 && screenLine < this.w && screenColumn >= 0 && screenColumn < this.h) {
									screenMatrix[this.h - screenColumn][screenLine] = (mode === Mode.WHITE_ON_BLACK ? verticalBitmap[y] : invert(verticalBitmap[y]));
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
	};

};
