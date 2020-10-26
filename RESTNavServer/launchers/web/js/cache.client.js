"use strict";

function cacheClient(dataManager, bp) {

	let onMessage = dataManager; // Client function
	let betweenPing = 1000;
	if (bp !== undefined) {
		betweenPing = bp;
	}

	function getNMEAData() {

		let url = '/mux/cache',
				verb = 'GET',
				TIMEOUT = 10000,
        happyCode = 200,
        data = null;

		return new Promise(function (resolve, reject) {
			let xhr = new XMLHttpRequest();

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

// Executed at startup
	(() => {
		// Long poll
		setInterval(() => {
			fetch();
		}, betweenPing);
	})();

	function fetch() {
		let getData = getNMEAData();
		getData.then((value) => {
			//  console.log("Done:", value);
			let json = JSON.parse(value);
			onMessage(json);
		}, (error, errmess) => {
			let message;
			if (errmess !== undefined) {
				try {
					let mess = JSON.parse(errmess);
					if (mess.message !== undefined) {
						message = mess.message;
					}
				} catch (err) {
					//  console.log(errmess);
				}
			}
			console.log("Failed to get nmea data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
		});
	}

}
