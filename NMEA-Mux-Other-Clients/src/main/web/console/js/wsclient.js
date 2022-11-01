"use strict";

let connection;

(() => {
	let ws = window.WebSocket || window.MozWebSocket;
	if (!ws) {
		displayMessage('Sorry, but your browser does not support WebSockets.');
		return;
	}

	// open ws connection
	let rootUri = "ws://" + (document.location.hostname === "" ? "localhost" : document.location.hostname) + ":" +
			                (document.location.port === "" ? "8080" : document.location.port);
	console.log(`wsuri : ${rootUri}`);
	connection = new WebSocket(rootUri); // 'ws://localhost:9876');

	connection.onopen = () => {
		displayMessage('Connected.')
	};

	connection.onerror = (error) => {
		// just in there were some problems with connection...
		displayMessage('Sorry, but there is some problem with your connection or the server is down.');
	};

	// most important part - incoming messages. Can be overridden (Consumer ?)
	connection.onmessage = (message) => {
        console.log('onmessage:' + message.data);
	};

	/**
	 * This method is optional. If the server wasn't able to respond to the
	 * in 3 seconds then show some error message to notify the user that
	 * something is wrong.
	 */
	setInterval(() => {
		if (connection.readyState !== 1) {
			displayMessage('Unable to communicate with the WebSocket server. Try again.');
		}
	}, 3000); // Ping every 3 seconds

	console.log("ws client connection initialization completed.");
})();

function displayMessage(mess) {
	console.log(mess);
}
