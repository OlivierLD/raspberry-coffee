/*
 * @author Olivier Le Diouris
 */
function SpeedEvolution(cName, maxSpeed, withBeaufortScale, label, scaleInc)  // Canvas name
{
	var instance = this;
	var cWidth, cHeight;

	if (maxSpeed === undefined) {
		maxSpeed = 60;
	}
	if (withBeaufortScale === undefined) {
		withBeaufortScale = true;
	}
	if (label === undefined) {
		label = "Speed";
	}
	if (scaleInc === undefined) {
		scaleInc = 5;
	}

	var SEC = 1000;
	var MIN = 60 * SEC;
	var HRS = 60 * MIN;
	var DAY = 24 * HRS;

	var canvas;
	var context;

	var speedBuffer = [];
	var lastSpeed = {};

	var maxBuffLength;

	this.setMaxBuffLength = function(len) {
		maxBuffLength = len;
	}

	this.addSpeed = function (d) { // { "speed": speed, "time": time }
		speedBuffer.push(d);
		if (maxBuffLength !== undefined) {
			if (speedBuffer.length > maxBuffLength) {
				speedBuffer.splice(0, (speedBuffer.length - maxBuffLength)); // Drop the head, keep the tail
			}
		}
		lastSpeed = d;
		instance.drawGraph();
	};

	var dumpBuffer = function () {
		console.log(JSON.stringify(speedBuffer));
	};

	this.resetSpeed = function () {
		dumpBuffer();
		speedBuffer = [];
		instance.drawGraph();
	};

	// Drop oldest half
	this.reset2Speed = function () {
		speedBuffer = speedBuffer.slice(Math.round(speedBuffer.length / 2), speedBuffer.length);
		instance.drawGraph();
	};

	this.getLifeSpan = function () {
		var lifeSpan = 0;
		if (speedBuffer[0] !== undefined) {
			var from = speedBuffer[0].time;
			var to = speedBuffer[speedBuffer.length - 1].time;
			lifeSpan = to - from;
		}
		return lifeSpan;
	};

	this.getLifeSpanFormatted = function () {
		var ms = this.getLifeSpan();
		return formattedTime(ms);
	};

	this.getFromBoundary = function () {
		return speedBuffer[0].time;
	};

	this.getToBoundary = function () {
		return speedBuffer[speedBuffer.length - 1].time;
	};

	this.getBufferLength = function () {
		return speedBuffer.length;
	};

	this.drawGraph = function () {
		cWidth = document.getElementById(cName).width;
		cHeight = document.getElementById(cName).height;

		context = canvas.getContext('2d');

		// context.fillStyle = "LightGray";
		var grd = context.createLinearGradient(0, 5, 0, document.getElementById(cName).height);
		grd.addColorStop(0, 'LightGray'); // 'gray');    // 0  Beginning
		grd.addColorStop(1, 'black'); // 'LightGray');    // 1  End
		context.fillStyle = grd;

		context.fillRect(0, 0, canvas.width, canvas.height);

//  context.beginPath();
//  context.lineWidth = 1;
//  context.strokeStyle = 'black';
//  context.strokeText("Overview", 10, 20); // Outlined  
//  context.closePath();
		// Grid
		context.strokeStyle = 'LightGreen';
		for (var i = 0; i < maxSpeed; i += scaleInc) {
			var x = i * (canvas.width / maxSpeed);
			context.beginPath();
			context.lineWidth = (i % 10 === 0) ? 3 : 1;
			context.moveTo(x, 0);
			context.lineTo(x, canvas.height);
			context.closePath();
			context.stroke();
		}
		context.lineWidth = 1;
		// Horizontal grid
		var lifeSpan = this.getLifeSpan();
		var timeStep = 15 * SEC; // Default, 15s
		if (lifeSpan > DAY) {
			timeStep = 3 * HRS;
		} else if (lifeSpan > 3 * HRS) {
			timeStep = 30 * MIN;
		} else if (lifeSpan > HRS) {
			timeStep = 10 * MIN;
		} else if (lifeSpan > 30 * MIN) {
			timeStep = 5 * MIN;
		} else if (lifeSpan > 10 * MIN) {
			timeStep = 1 * MIN;
		} else if (lifeSpan > MIN) {
			timeStep = 30 * SEC;
		}

		context.strokeStyle = 'white';
		context.fillStyle = 'white';
		var prevTime;
		for (var i = 0; i < speedBuffer.length; i++) {
			if (prevTime === undefined || (prevTime !== undefined &&
							prevTime !== Math.floor(speedBuffer[i].time / SEC) &&
							Math.floor(speedBuffer[i].time / SEC) % (timeStep / SEC) == 0)) {
				prevTime = Math.floor(speedBuffer[i].time / SEC);
				var y = canvas.height - (i * (canvas.height / speedBuffer.length));
				context.beginPath();
				context.moveTo(0, y);
				context.lineTo(canvas.width, y);
				context.closePath();
				context.stroke();

				var txt = new Date(speedBuffer[i].time).format("H:i:s");
				context.font = "bold 12px Arial"; // "bold 12px Arial"
				var metrics = context.measureText(txt);
				len = metrics.width;
				context.fillText(txt, canvas.width - 5 - len, y - 5);
			}
		}

		if (withBeaufortScale) {
			// Beaufort scale
			var beaufort = [1, 4, 7, 11, 16, 22, 28, 34, 41, 48, 56, 64];
			context.strokeStyle = 'rgba(255, 0, 0, 0.7)'; // 'red';
			for (var i = 0; i < beaufort.length; i++) {
				var x = beaufort[i] * (canvas.width / maxSpeed);
				context.beginPath();
				context.moveTo(x, 0);
				context.lineTo(x, canvas.height);
				context.closePath();
				context.stroke();
				var txt = (i + 1).toString();
				context.font = "bold 16px Arial"; // "bold 16px Arial"
				var metrics = context.measureText(txt);
				len = metrics.width;
				context.fillStyle = 'white';
				context.fillText(txt, x - (len / 2), canvas.height - 5);
			}
		}
		context.lineWidth = 3;

		// Data here
		// Calculate average
		if (false && speedBuffer.length > 0) {
			var sum = 0;
			for (var i = 0; i < speedBuffer.length; i++) {
				sum += speedBuffer[i].speed;
			}
			var avg = sum / speedBuffer.length;
		}

		var yScale = canvas.height / (speedBuffer.length - 1);
		var xScale = canvas.width / maxSpeed;
		context.strokeStyle = 'cyan';
		context.beginPath();
		for (var i = 0; i < speedBuffer.length; i++) {
			var xPt = speedBuffer[i].speed * xScale;
			var yPt = canvas.height - (i * yScale);
//    console.log("i:" + i + ", " + xPt + "/" + yPt);
			if (i === 0) {
				context.moveTo(xPt, yPt);
			} else {
				context.lineTo(xPt, yPt);
			}
		}
//  context.closePath();
		context.stroke();

		// Display values
		context.fillStyle = 'green';
		context.font = "bold 16px Courier New";
		var txtY = 20;
		var space = 18;
		var col1 = 10, col2 = 90;
		context.fillText(label, col1, txtY);
		if (lastSpeed.speed !== undefined) {
			context.fillText(lastSpeed.speed.toFixed(2) + " kts", col2, txtY);
		}
		txtY += space;
	};

	var relativeMouseCoords = function (event, element) {
		var totalOffsetX = 0;
		var totalOffsetY = 0;
		var canvasX = 0;
		var canvasY = 0;
		var currentElement = element;

		do {
			totalOffsetX += currentElement.offsetLeft - currentElement.scrollLeft;
			totalOffsetY += currentElement.offsetTop - currentElement.scrollTop;
		} while (currentElement = currentElement.offsetParent)

		canvasX = event.pageX - totalOffsetX;
		canvasY = event.pageY - totalOffsetY;

		return {x: canvasX, y: canvasY};
	};

	var toRadians = function (deg) {
		return deg * (Math.PI / 180);
	};

	function toDegrees(rad) {
		return rad * (180 / Math.PI);
	};

	(function () {
		canvas = document.getElementById(cName);
		canvas.addEventListener('mousemove', function (evt) {
			var x = evt.pageX - canvas.offsetLeft;
			var y = evt.pageY - canvas.offsetTop;

			var coords = relativeMouseCoords(evt, canvas);
			x = coords.x;
			y = coords.y;
			var yInBuffer = Math.floor(speedBuffer.length * ((canvas.height - y) / canvas.height));

			var str1 = label + " " + Math.round(maxSpeed * x / canvas.width) + "kts";
			var str2 = ((speedBuffer[yInBuffer] !== undefined) ? new Date(speedBuffer[yInBuffer].time).format("H:i:s") : "");
			instance.drawGraph();
			context.fillStyle = "rgba(250, 250, 210, .6)";
//    context.fillStyle = 'yellow';
			context.fillRect(x + 10, y + 10, 70, 40); // Background
			context.fillStyle = 'black';
			context.font = 'bold 12px verdana';
			context.fillText(str1, x + 15, y + 25, 60);
			context.fillText(str2, x + 15, y + 25 + 14, 60);
		}, 0);
		instance.drawGraph();
	})(); // Invoked automatically when new is invoked.
};

var formattedTime = function (ms) {
	var fmt = "0";
	if (ms !== 0) {
		var sec = Math.floor(ms / 1000);
		var min = Math.floor(sec / 60);
		var hrs = Math.floor(min / 60);
		var day = Math.floor(hrs / 24);

		fmt = (day > 0 ? day.toString() + "d " : "") +
				((hrs > 0 || day > 0) ? (hrs - (24 * day)).toString() + "h " : "") +
				((min > 0 || hrs > 0 || day > 0) ? (min - (60 * hrs)).toString() + "m " : "") +
				(sec - (min * 60)).toString() + "s";
	}
	return fmt;
};

//var ms = 6457205; // 1h 47m 37s.
//console.log(formattedTime(ms));
   
