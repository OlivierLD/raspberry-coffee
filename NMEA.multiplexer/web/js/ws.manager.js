/*
 * @author Olivier Le Diouris
 */
function initWS() {
	let connection;

	// if user is running mozilla then use it's built-in WebSocket
	//  window.WebSocket = window.WebSocket || window.MozWebSocket;  // TODO otherwise, fall back
	let ws = window.WebSocket || window.MozWebSocket;  // TODO otherwise, fall back

	// if browser doesn't support WebSocket, just show some notification and exit
	//  if (!window.WebSocket)

	if (!ws) {
		alert('Sorry, but your browser does not support WebSockets.'); // TODO Fallback
		return;
	}

	// open connection
	let rootUri = "ws://" + (document.location.hostname === "" ? "localhost" : document.location.hostname) + ":" +
			(document.location.port === "" ? "9876" : document.location.port);
	console.log(rootUri);
	connection = new WebSocket(rootUri); // 'ws://localhost:9876');

	connection.onopen = function () {
		console.log('Connected.')
	};

	connection.onerror = function (error) {
		// just in there were some problems with connection...
		alert('Sorry, but there is some problem with your connection or the server is down.');
	};

	connection.onmessage = function (message) {
//  console.log('onmessage:' + JSON.stringify(message.data));
		var data = JSON.parse(message.data);
		setValues(data);
	};
}

function setValues(doc) {
	try {
		var errMess = "";

		var json = doc;

		// TODO Implement event publication like below...

		/*
		var displayWT = (json.displayWT !== undefined ? json.displayWT : true);

		events.publish('show-hide-wt', displayWT);

		try {
				var latitude = parseFloat(json.lat);
				var longitude = parseFloat(json.lng);
				events.publish('pos', {
						'lat': latitude,
						'lng': longitude
				});
		}
		catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Problem with ") + "position");
		}

		// Displays
		try {
				var bsp = parseFloat(json.bsp);
				events.publish('bsp', bsp);
		}
		catch (err) {
				errMess += ((errMess.length > 0 ? ", " : "Problem with ") + "boat speed");
		}
		*/

		if (errMess !== undefined)
			displayErr(errMess);
	} catch (err) {
		displayErr(err);
	}
}
