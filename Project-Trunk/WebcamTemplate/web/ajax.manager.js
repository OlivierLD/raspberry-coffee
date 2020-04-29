/*
 * @author Olivier Le Diouris
 * Uses ES6 Promises for Ajax.
 */

const DEBUG = false;

function initAjax() {
	let interval = setInterval(() => {
		fetch();
		loadSunData(lastKnownPos);
	}, 1000);
}

const DEFAULT_TIMEOUT = 60000; // 1 minute
/* global events */

/* Uses ES6 Promises */
function getPromise(
		url,                          // full api path
		timeout,                      // After that, fail.
		verb,                         // GET, PUT, DELETE, POST, etc
		headers,                      // Request Headers, or null.
		happyCode,                    // if met, resolve, otherwise fail.
		data = null,                  // payload, when needed (PUT, POST...)
		show = true) {                // Show the traffic [true]|false

	if (show === true) {
		document.body.style.cursor = 'wait';
	}

	if (DEBUG) {
		console.log(">>> Promise", verb, url);
	}

	let promise = new Promise((resolve, reject) => {
		let xhr = new XMLHttpRequest();
		let TIMEOUT = timeout;

		let req = verb + " " + url;
		if (data !== undefined && data !== null) {
			req += ("\n" + JSON.stringify(data, null, 2));
		}

		xhr.open(verb, url, true);
		// xhr.setRequestHeader("Content-type", "application/json");
		if (headers !== undefined && headers !== null) {
			headers.forEach(header => {
				xhr.setRequestHeader(header.name, header.value);
			});
		}
		try {
			if (data === undefined || data === null) {
				xhr.send();
			} else {
				xhr.send(JSON.stringify(data));
			}
		} catch (err) {
			console.log("Send Error ", err);
		}

		let requestTimer = setTimeout(() => {
			xhr.abort();
			let mess = { code: 408, message: 'Timeout' };
			reject(mess);
		}, TIMEOUT);

		xhr.onload = () => {
			clearTimeout(requestTimer);
			if (xhr.status === happyCode) {
				resolve(xhr.response);
			} else {
				reject({ code: xhr.status, message: xhr.response });
			}
		};
	});
	return promise;
}

function getLastSnapshot() {
	return getPromise('/snap/last-snapshot',
			DEFAULT_TIMEOUT,
			'GET',
			[ {name: "Accept", value: "application/json"},
				        {name: "Pragma", value: "no-cache"} ],
			200,
			null,
			false);
}

function fetchPix(callback) {
	let getData = getLastSnapshot();
	getData.then((value) => { // Resolve
//  console.log("Done:", value);
		try {
			let json = JSON.parse(value);
			if (callback !== undefined) {
				callback(json);
			} else {
				console.log(json); // Do something smart here.
			}
		} catch (err) {
			console.log("Error:", err, ("\nfor value [" + value + "]"));
		}
	}, (error) => { // Reject
		console.log("Failed to get the last snapshot..." + (error !== undefined && error.code !== undefined ? error.code : ' - ') + ', ' + (error !== undefined && error.message !== undefined ? error.message : ' - '));
	});
}

