/**
 * @author Olivier LeDiouris
 */
const DEFAULT_TIMEOUT = 60000; // 1 minute

function postSmooth(payload) {
	return getPromise('/math/smooth', DEFAULT_TIMEOUT, 'POST', 201, payload, false);
}

/* Uses ES6 Promises */
/**
 *
 * @param url full api path
 * @param timeout After that, fail.
 * @param verb GET, PUT, DELETE, POST, etc
 * @param happyCode if met, resolve, otherwise fail.
 * @param data payload, when needed (PUT, POST...)
 * @param show Show the traffic [true]|false
 * @returns {Promise<any>}
 */
function getPromise(url, timeout, verb, happyCode, data, show) {
	if (show === undefined) {
		show = true;
	}
	if (show === true) {
		document.body.style.cursor = 'wait';
	}

	let promise = new Promise(function (resolve, reject) {
		let xhr = new XMLHttpRequest();
		let TIMEOUT = timeout;

		let req = verb + " " + url;
		if (data !== undefined && data !== null) {
			req += ("\n" + JSON.stringify(data, null, 2));
		}

		xhr.open(verb, url, true);
		xhr.setRequestHeader("Content-type", "application/json");
		try {
			if (data === undefined || data === null) {
				xhr.send();
			} else {
				xhr.send(JSON.stringify(data));
			}
		} catch (err) {
			console.log("Send Error ", err);
		}

		let requestTimer = setTimeout(function () {
			xhr.abort();
			let mess = { code: 408, message: 'Timeout' };
			reject(mess);
		}, TIMEOUT);

		xhr.onload = function () {
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

function smooth(payload, cb) {
	let requestSmoothing = postSmooth(payload);
	requestSmoothing.then(function (value) { // Resolve
//  console.log("Done:", value);
		try {
			let json = JSON.parse(value);
			if (cb !== undefined) {
				cb(json);
			} else {
				console.log("Smoothing result:", json);
			}
		} catch (err) {
			console.log("Error:", err, ("\nfor value [" + value + "]"));
		}
	}, function (error) { // Reject
		console.log("Failed to get smoothed data..." + (error !== undefined && error.code !== undefined ? error.code : ' - ') + ', ' + (error !== undefined && error.message !== undefined ? error.message : ' - '));
	});
}

function postGuessSmooth(payload) {
	return getPromise('/math/intelligent-smooth', 10 * DEFAULT_TIMEOUT, 'POST', 201, payload, true); // give it some time...
}

function guessSmooth(payload, cb) {
	let requestGuessSmoothing = postGuessSmooth(payload);
	requestGuessSmoothing.then(function (value) { // Resolve
//  console.log("Done:", value);
		try {
			let json = JSON.parse(value);
			if (cb !== undefined) {
				cb(json);
				document.body.style.cursor = 'default';
			} else {
				console.log("Smoothing result:", json);
				document.body.style.cursor = 'default';
			}
		} catch (err) {
			console.log("Error:", err, ("\nfor value [" + value + "]"));
			document.body.style.cursor = 'default';
		}
	}, function (error) { // Reject
		console.log("Failed to get smoothed data..." + (error !== undefined && error.code !== undefined ? error.code : ' - ') + ', ' + (error !== undefined && error.message !== undefined ? error.message : ' - '));
		document.body.style.cursor = 'default';
	});
}

function getQueryParameterByName(name, url) {
	if (!url) url = window.location.href;
	name = name.replace(/[\[\]]/g, "\\$&");
	let regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
			results = regex.exec(url);
	if (!results) {
		return null;
	}
	if (!results[2]) {
		return '';
	}
	return decodeURIComponent(results[2].replace(/\+/g, " "));
}
