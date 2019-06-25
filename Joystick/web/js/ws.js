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
			if (data.up === false && data.down === false && data.left === false && data.right === false) {
				document.getElementById('center').classList.add('selected-dot')
				document.getElementById('top').classList.remove('selected-dot')
				document.getElementById('bottom').classList.remove('selected-dot')
				document.getElementById('left').classList.remove('selected-dot')
				document.getElementById('right').classList.remove('selected-dot')
			} else {
				document.getElementById('center').classList.remove('selected-dot')
				if (data.up === true) {
					document.getElementById('top').classList.add('selected-dot')
				} else {
					document.getElementById('top').classList.remove('selected-dot')
				}
				if (data.down === true) {
					document.getElementById('bottom').classList.add('selected-dot')
				} else {
					document.getElementById('bottom').classList.remove('selected-dot')
				}
				if (data.right === true) {
					document.getElementById('right').classList.add('selected-dot')
				} else {
					document.getElementById('right').classList.remove('selected-dot')
				}
				if (data.left === true) {
					document.getElementById('left').classList.add('selected-dot')
				} else {
					document.getElementById('left').classList.remove('selected-dot')
				}
			}
		};
	} catch (err) {
		console.log(">>> Connection:" + err);
	}
}

(() => {
	console.log("Initializing WS Connection");
	initWS();
})();
