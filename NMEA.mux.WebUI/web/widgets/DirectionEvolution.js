/*
 * @author Olivier Le Diouris
 */
function DirectionEvolution(cName, label)     // Canvas name
{
	var instance = this;
	var cWidth, cHeight;

	var SEC = 1000;
	var MIN = 60 * SEC;
	var HRS = 60 * MIN;
	var DAY = 24 * HRS;

	var canvas;
	var context;

	var directionBuffer = [];
	var lastDirection = {};

	if (label === undefined) {
		label = "Dir.";
	}

	var maxBuffLength;

	this.setMaxBuffLength = function(len) {
		maxBuffLength = len;
	}

	this.addDirection = function (d) { // { "angle": angle, "time": time }
		directionBuffer.push(d);
		if (maxBuffLength !== undefined) {
			if (directionBuffer.length > maxBuffLength) {
				directionBuffer.splice(0, (directionBuffer.length - maxBuffLength)); // Drop the head, keep the tail
			}
		}
		lastDirection = d;
		instance.drawGraph();
	};

	var dumpBuffer = function () {
		console.log(JSON.stringify(directionBuffer));
	};

	this.resetDirection = function () {
		dumpBuffer();
		directionBuffer = [];
		instance.drawGraph();
	};

	// Drop oldest half
	this.reset2Direction = function () {
		directionBuffer = directionBuffer.slice(Math.round(directionBuffer.length / 2), directionBuffer.length);
		instance.drawGraph();
	};

	this.getLifeSpan = function () {
		var lifeSpan = 0;
		if (directionBuffer[0] !== undefined) {
			var from = directionBuffer[0].time;
			var to = directionBuffer[directionBuffer.length - 1].time;
			lifeSpan = to - from;
		}
		return lifeSpan;
	};

	this.getLifeSpanFormatted = function () {
		var ms = this.getLifeSpan();
		var fmt = "0";
		if (ms !== 0) {
			var sec = Math.floor(ms / 1000);
			var min = Math.floor(sec / 60);
			var hrs = Math.floor(min / 60);
			var day = Math.floor(hrs / 24);

			fmt = (day > 0 ? day.toString() + "d " : "") +
					((hrs > 0 || day > 0) ? (hrs - (24 * day)).toString() + "h " : "") +
					((min > 0 || hrs > 0 || day > 0) ? (min - (60 * hrs) - (24 * day)).toString() + "m " : "") +
					(sec - (min * 60) - (60 * hrs) - (24 * day)).toString() + "s.";
		}
		return fmt;
	};

	this.getFromBoundary = function () {
		return directionBuffer[0].time;
	};

	this.getToBoundary = function () {
		return directionBuffer[directionBuffer.length - 1].time;
	};

	this.getBufferLength = function () {
		return directionBuffer.length;
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
		// Data here
		// Calculate average
		if (directionBuffer.length > 0) {
			var sum = 0;
			var sumCos = 0, sumSin = 0;
			for (var i = 0; i < directionBuffer.length; i++) {
//      sum += directionBuffer[i];
				sumCos += Math.cos(toRadians(directionBuffer[i].angle));
				sumSin += Math.sin(toRadians(directionBuffer[i].angle));
			}
//    var avg = sum / directionBuffer.length;
			var avgCos = sumCos / directionBuffer.length;
			var avgSin = sumSin / directionBuffer.length;

			var aCos = toDegrees(Math.acos(avgCos));
			var aSin = toDegrees(Math.asin(avgSin));

			if (avgCos > 0) {
				if (avgSin > 0) {
					avg = aCos;
				} else {
					avg = 360 - aCos;
				}
			}
			else {
				if (avgSin > 0) {
					avg = 180 - aCos;
				} else {
					avg = 180 + aCos;
				}
			}
//    console.log("(" + label + ") Avg TWD:" + Math.round(avg));
		}

		var orig = lastDirection.angle - 180;
		for (var i = 0; i < 360; i++) {
			if (Math.round(i + orig) % 30 === 0) {
				var x = i * (canvas.width / 360);

				context.beginPath();
				context.lineWidth = (Math.round(i + orig) % 90 === 0) ? 3 : 1;
				context.strokeStyle = (Math.round(i + orig) % 90 === 0) ? 'LightGreen' : 'white';
				context.moveTo(x, 0);
				context.lineTo(x, canvas.height);
				context.closePath();
				context.stroke();
				var dir = Math.round(i + orig);
				while (dir < 0) dir += 360;
				while (dir > 360) dir -= 360;
				if (dir % 90 === 0) {
					var txt = "";
					switch (dir % 360) {
						case 0:
						case 360:
							txt = "N";
							break;
						case 90:
							txt = "E";
							break;
						case 180:
							txt = "S";
							break;
						case 270:
							txt = "W";
							break;
						default:
							break;
					}
					context.font = "bold 16px Arial"; // "bold 16px Arial"
					var metrics = context.measureText(txt);
					len = metrics.width;
					context.fillStyle = 'white';
					context.fillText(txt, x - (len / 2), canvas.height - 5);
				}
			}
		}
		context.lineWidth = 3;

		var yScale = canvas.height / (directionBuffer.length - 1);
		var xScale = canvas.width / 360;
		context.strokeStyle = 'red';
		context.beginPath();
		var prevDir;
		for (var i = 0; i < directionBuffer.length; i++) {
			var dir = directionBuffer[i].angle;
			if (prevDir !== undefined) {
//			if (Math.abs(prevDir - dir) > 180) {
				if (Math.abs(lastDirection.angle - dir) > 180) {
					if (dir > 180) {
						dir -= 360;
					} else {
						dir += 360;
					}
				}
			}
			var xPt = (canvas.width / 2) + ((dir - lastDirection.angle) * xScale);
			var yPt = canvas.height - (i * yScale);
//    console.log("i:" + i + ", " + xPt + "/" + yPt);
			if (i === 0) {
				context.moveTo(xPt, yPt);
			} else {
				context.lineTo(xPt, yPt);
			}
			prevDir = dir;
		}
//  context.closePath();
		context.stroke();

		// Horizontal grid
		context.lineWidth = 1;
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
		for (var i = 0; i < directionBuffer.length; i++) {
			if (prevTime === undefined || (prevTime !== undefined &&
							prevTime !== Math.floor(directionBuffer[i].time / SEC) &&
							Math.floor(directionBuffer[i].time / SEC) % (timeStep / SEC) == 0)) {
				prevTime = Math.floor(directionBuffer[i].time / SEC);
				var y = canvas.height - (i * (canvas.height / directionBuffer.length));
				context.beginPath();
				context.moveTo(0, y);
				context.lineTo(canvas.width, y);
				context.closePath();
				context.stroke();

				var txt = new Date(directionBuffer[i].time).format("H:i:s");

				//  console.log(">>> DEBUG >>> For date:" + directionBuffer[i].time + ", displaying " + txt);

				context.font = "bold 12px Arial"; // "bold 12px Arial"
				var metrics = context.measureText(txt);
				len = metrics.width;
				context.fillText(txt, canvas.width - 5 - len, y - 5);
			}
		}

		// Display values
		context.fillStyle = 'green';
		context.font = "bold 16px Courier New";
		var txtY = 20;
		var space = 18;
		var col1 = 10, col2 = 90;
		context.fillText(label, col1, txtY);
		if (lastDirection.angle !== undefined) {
			context.fillText(lastDirection.angle.toFixed(0) + "", col2, txtY);
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

	var toDegrees = function (rad) {
		return rad * (180 / Math.PI);
	};

	(function () {
		canvas = document.getElementById(cName);
		canvas.addEventListener('mousemove', function (evt) // Tooltip
		{
			var x = evt.pageX - canvas.offsetLeft;
			var y = evt.pageY - canvas.offsetTop;

			var coords = relativeMouseCoords(evt, canvas);
			x = coords.x;
			y = coords.y;
			var yInBuffer = Math.floor(directionBuffer.length * ((canvas.height - y) / canvas.height));

			var mouseDirectiopn = lastDirection.angle + (360 * (x - (canvas.width / 2)) / canvas.width);
			while (mouseDirectiopn > 360) mouseDirectiopn -= 360;
			while (mouseDirectiopn < 0) mouseDirectiopn += 360;

			var str1 = label + " " + Math.round(mouseDirectiopn) + "";
			var str2 = ((directionBuffer[yInBuffer] !== undefined) ? new Date(directionBuffer[yInBuffer].time).format("H:i:s") : "");
			instance.drawGraph();
			context.fillStyle = "rgba(250, 250, 210, .6)";
//      context.fillStyle = 'yellow';
			context.fillRect(x + 10, y + 10, 70, 40); // Background
			context.fillStyle = 'black';
			context.font = 'bold 12px verdana';
			context.fillText(str1, x + 15, y + 25, 60);
			context.fillText(str2, x + 15, y + 25 + 14, 60);
		}, 0);
		instance.drawGraph();
	})(); // Invoked automatically when new is invoked.
};
