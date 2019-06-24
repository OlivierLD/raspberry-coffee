/**
 * @author Olivier Le Diouris
 * ES6
 */
"use strict";

function initWS() {
	let connection;
	// if user is running mozilla then use it's built-in WebSocket
	let ws = window.WebSocket || window.MozWebSocket;  // TODO otherwise, fall back
	// if browser doesn't support WebSocket, just show some notification and exit
	//  if (!window.WebSocket)
	if (!ws) {
		alert('Sorry, but your browser does not support WebSockets.'); // TODO? Fallback
		return;
	}
	// open connection
	let rootUri = "ws://" + (document.location.hostname === "" ? "localhost" : document.location.hostname) + ":" +
			(document.location.port === "" ? "9876" : document.location.port);
	console.log(rootUri);
	try {
		connection = new WebSocket(rootUri); // 'ws://localhost:9876');
		connection.onopen = function() {
			console.log('Connected.')
		};
		connection.onerror = function(error) {
			// just in there were some problems with connection...
			alert('Sorry, but there is some problem with your connection or the server is down.');
		};
		connection.onmessage = function(message) {
			//  console.log('onmessage:' + JSON.stringify(message.data));
			let data = JSON.parse(message.data);
			console.log("Received", data);
		};
	} catch (err) {
		console.log(">>> Connection:" + err);
	}
}


function lpad(str, pad, len) {
	while (str.length < len) {
		str = pad + str;
	}
	return str;
}

(() => {
	console.log("Initializing WS Connection");
	initWS();
})();
