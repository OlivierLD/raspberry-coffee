/*
 * @author Olivier Le Diouris
 *
 */
"use strict";

var worldMap;
var currentDate;

var errManager = function(mess) {
	console.log(mess);
};

var DEFAULT_TIMEOUT = 60000;

/*
 *  Demo features
 */

var position = {
	lat: 37.7489,
	lng: -122.5070
};

const MINUTE = 60000; // in ms.

var getCurrentUTCDate = function() {
	var date = new Date();
	var offset = date.getTimezoneOffset() * MINUTE; // in millisecs

	return new Date().getTime() + offset; // - (6 * 3600 * 1000);
};

var init = function () {
	worldMap = new WorldMap('mapCanvas', 'MERCATOR');

	worldMap.setNorth(66.5);
	worldMap.setSouth(-48.5);
	worldMap.setWest(127.5);
	worldMap.setEast(-102.0); // Recalculated, anyway.

	worldMap.setMouseMoveCallback(mouseMoveCallback);

	worldMap.setUserPosition({ latitude: position.lat, longitude: position.lng });

};

var mouseMoveCallback = function(obj) {

	var canvas = document.getElementById('mapCanvas');
	var context = canvas.getContext('2d');

	worldMap.drawWorldMap();
	var str = [];
	try {
		str.push("X:" + obj.x + ", Y:" + obj.y);
		str.push("L: " + decToSex(obj.lat, "NS"));
		str.push("G: " + decToSex(obj.lng, "EW"));
	} catch (err) {
		console.log(err);
	}
	var tooltipW = 100, nblines = str.length;
	context.fillStyle = 'rgba(255, 255, 0, 0.5)'; // transparent yellow
	var fontSize = 10;
	var x_offset = 10, y_offset = 10;

	if (obj.x > (context.canvas.clientWidth / 2)) {
		x_offset = -(tooltipW + 10);
	}
	if (obj.y > (context.canvas.clientHeight / 2)) {
		y_offset = -(10 + 6 + (nblines * fontSize));
	}
	context.fillRect(obj.x + x_offset, obj.y + y_offset, tooltipW, 6 + (nblines * fontSize)); // Background
	context.fillStyle = 'black'; // graphColorConfig.tooltipTextColor;
	context.font = /*'bold ' +*/ fontSize + 'px verdana';
	for (var i=0; i<str.length; i++) {
		context.fillText(str[i], obj.x + x_offset + 5, obj.y + y_offset + (3 + (fontSize * (i + 1)))); //, 60);
	}
};

