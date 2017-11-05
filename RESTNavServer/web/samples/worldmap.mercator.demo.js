/*
 * @author Olivier Le Diouris
 *
 * Shows how to use the WorldMap object
 * and the REST APIs of the Astro service.
 * Displays a GLOBE
 */
"use strict";

var worldMap;
var currentDate;

var errManager = function(mess) {
	console.log(mess);
};

var init = function () {
	worldMap = new WorldMap('mapCanvas', 'MERCATOR');
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
