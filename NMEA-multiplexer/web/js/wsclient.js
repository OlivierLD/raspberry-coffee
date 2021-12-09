"use strict";

let connection;

(() => {
	let ws = window.WebSocket || window.MozWebSocket;
	if (!ws) {
		displayMessage('Sorry, but your browser does not support WebSockets.');
		return;
	}

	// open connection
	let rootUri = "ws://" + (document.location.hostname === "" ? "localhost" : document.location.hostname) + ":" +
			(document.location.port === "" ? "8080" : document.location.port);
	console.log(rootUri);
	connection = new WebSocket(rootUri); // 'ws://localhost:9876');

	connection.onopen = () => {
		displayMessage('Connected.')
	};

	connection.onerror = (error) => {
		// just in there were some problems with connection...
		displayMessage('Sorry, but there is some problem with your connection or the server is down.');
	};

	// most important part - incoming messages
	connection.onmessage = (message) => {
//      console.log('onmessage:' + message);
		if (filters.value.length > 0) {
			var pattern = new RegExp(filters.value, "gm");
			if (message.data.match(pattern)) {
				displayMessage(message.data);
			}
		} else {
			displayMessage('Unfiltered: ' + message.data);
		}
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
	}, 3000); // Ping
})();

function displayMessage(mess) {
	let messList = statusFld.innerHTML;
	if (messList !== undefined) {
		let lines = messList.split('<br>');
		while (lines.length > 10) { // Limit number of messages to 10.
			lines.shift();
		}
		messList = '';
		for (let i = 0; i < lines.length; i++) {
			messList += (lines[i] + '<br>');
		}
	}
	messList = (((messList !== undefined && messList.length) > 0 ? messList + (messList.endsWith('<br>') ? '' : '<br>') : '') + mess);
	statusFld.innerHTML = messList;
	statusFld.scrollTop = statusFld.scrollHeight; // Scroll down
}

function resetStatus() {
	statusFld.innerHTML = "";
}
