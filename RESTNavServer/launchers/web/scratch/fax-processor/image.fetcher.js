/*
 * @author Olivier Le Diouris
 * Uses ES6 Promises for Ajax.
 */

const DEBUG = false;
const DEFAULT_TIMEOUT = 60000; // 1 minute

/* Uses ES6 Promises */
function getPromise(
    url,                          // full resource path
    timeout,                      // After that, fail.
    verb,                         // GET, PUT, DELETE, POST, etc
    happyCode,                    // if met, resolve, otherwise fail.
    data = null,                  // payload, when needed (PUT, POST...)
    show = true,                  // Show the traffic [true]|false
    headers = null) {             // Array of { name: 'Header-Name', value: 'Header-Value' }

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
        xhr.responseType = "blob";
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

function getData(url, headers) {
    return getPromise(url, DEFAULT_TIMEOUT, 'GET', 200, null, false, headers);
}

function fetchImage(url, headers) {
    let responseData = getData(url, headers);
    responseData.then((imageBlob) => { // Resolve
        try {
           let urlCreator = window.URL || window.webkitURL;
           let imageUrl = urlCreator.createObjectURL(imageBlob);
           document.querySelector("#image").src = imageUrl;
           // onMessage(json); ...
        } catch (err) {
            console.log("Error:", err);
        }
    }, (error) => {                   // Reject
        console.log("Failed to get data..." +
                    (error !== undefined && error.code !== undefined ? error.code : ' - ') + ', ' +
                    (error !== undefined && error.message !== undefined ? error.message : ' - '));
    });
}
