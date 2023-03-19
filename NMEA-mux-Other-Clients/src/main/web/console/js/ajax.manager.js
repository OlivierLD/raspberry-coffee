/*
 * @author Olivier Le Diouris
 * Uses ES6 Promises for Ajax.
 */

const DEBUG = false;

function initAjax() {

    // Example:
    // ISS Position http://api.open-notify.org/iss-now.json
    // ISS Passage time http://api.open-notify.org/iss-pass.json?lat=37.7&lon=-122.5 [ &alt=20&n=5 ]
    // ISS Crew members: http://api.open-notify.org/astros.json
    if (false) { // 
        let issInterval = setInterval(() => {
            let issPromise = getISSData();
            issPromise.then(issData => {
                console.log('ISSData:', issData);
            }, (error, message) => {
                console.debug('ISSData error', error, message);
            });
        }, 5000);
    }
}

const DEFAULT_TIMEOUT = 60000; // 1 minute
/* global events */

/* Uses ES6 Promises */
function getPromise(
    url,                          // full api path
    timeout,                      // After that, fail.
    verb,                         // GET, PUT, DELETE, POST, etc
    happyCode,                    // if met, resolve, otherwise fail.
    data = null,             // payload, when needed (PUT, POST...)
    show = true,          // Show the traffic [true]|false
    headers = null) {        // Array of { name: '', value: '' }

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
        if (headers === null) {
            xhr.setRequestHeader("Content-type", "application/json");
        } else {
            headers.forEach(header => xhr.setRequestHeader(header.name, header.value));
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
    return promise;
}

function getISSData() {
    return getPromise('http://api.open-notify.org/iss-now.json',
        DEFAULT_TIMEOUT,
        'GET',
        200,
        null,
        false,
        [{
            name: 'Access-Control-Allow-Origin',
            value: '*'
        }, {
            name: 'Access-Control-Allow-Methods',
            value: 'GET, POST, PUT, OPTIONS, HEAD'
        }, {
            name: 'Access-Control-Allow-Headers',
            value: 'Content-Type'
        }]);
}
