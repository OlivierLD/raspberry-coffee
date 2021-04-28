/*
 * @author Olivier LeDiouris
 * Uses ES6 Promises for Ajax.
 */

const DEBUG = false;

const DEFAULT_TIMEOUT = 60000; // 1 minute

/* Uses ES6 Promises */
function getPromise(
		url,                          // full api path
		timeout,                      // After that, fail.
		verb,                         // GET, PUT, DELETE, POST, etc
		headers,                      // Request Headers, or null. Array of { 'name': 'header-name', 'value: 'Value' }
		happyCode,                    // if met, resolve, otherwise fail.
		data = null,                  // json payload, when needed (PUT, POST...)
		show = false) {               // Show the traffic true|[false]

	if (show === true) {
		document.body.style.cursor = 'wait';
	}

	if (DEBUG) {
		console.log(">>> Promise", verb, url);
	}

	return new Promise((resolve, reject) => {
		let xhr = new XMLHttpRequest();
		let TIMEOUT = timeout;

		if (DEBUG) {
			let req = verb + " " + url;
			if (data !== undefined && data !== null) {
				req += ("\n" + JSON.stringify(data, null, 2));
			}
			console.log(req);
		}

		xhr.open(verb, url, true);
		// xhr.setRequestHeader("Content-type", "application/json");
		if (headers !== undefined && headers !== null) {
			headers.forEach(header => {
				xhr.setRequestHeader(header.name, header.value);
			});
		}
		try {
			if (/*data === undefined || */ data === null) {
				xhr.send();
			} else {
				xhr.send(JSON.stringify(data));
			}
		} catch (err) {
			console.log("Send Error ", err);
		}

		let requestTimer = setTimeout(() => {
			xhr.abort();
			let mess = {code: 408, message: 'Timeout'};
			reject(mess);
		}, TIMEOUT);

		xhr.onload = () => {
			clearTimeout(requestTimer);
			if (xhr.status === happyCode) {
				resolve(xhr.response);
			} else {
				reject({code: xhr.status, message: xhr.response});
			}
		};
	});
}

function getLastSnapshot(prms) {
	let qs = '';
	if (prms !== undefined && prms.length > 0) {
		prms.forEach(prm => { // Compose Query String
			qs += `${qs.length > 0 ? '&' : ''}${prm}`;
		});
	}
	return getPromise(`/snap/last-snapshot${qs.length > 0 ? `?${qs}` : ""}`,
		DEFAULT_TIMEOUT,
		'GET',
		[{name: "Accept", value: "application/json"},
			{name: "Pragma", value: "no-cache"}],
		200,
		null,
		false);
}

function fetchPix(prms, callback) {
	let getData = getLastSnapshot(prms);
	getData.then(value => { // Resolve
//  console.log("Done:", value);
		try {
			let json = JSON.parse(value);
			if (callback !== undefined) {
				callback(json);
			} else {
				console.log(json); // Do something smarter here?
			}
		} catch (err) {
			console.log(`Error:${err} \nfor value [${value}]`);
		}
	}, error => { // Reject
		console.log("Failed to get the last snapshot..." +
			(error !== undefined && error.code !== undefined ? error.code : ' - ') + ', ' +
			(error !== undefined && error.message !== undefined ? error.message : ' - '));
	});
}
