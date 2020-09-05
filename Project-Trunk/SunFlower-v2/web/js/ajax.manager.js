function getSFData() {

    let url = '/sf/status',
        xhr = new XMLHttpRequest(),
        verb = 'GET',
        data = null,
        happyCode = 200,
        TIMEOUT = 10000;

    let promise = new Promise(function (resolve, reject) {
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

        let requestTimer = setTimeout( () => {
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

function fetchData(callback) {
    let getData = getSFData();
    getData.then((value) => {
        // console.log("Done:", value);
        let json = JSON.parse(value);
        if (callback !== undefined) {
            callback(json);
        } else {
            console.log('Data', json);
        }
    }, (error, errmess) => {
        let message;
        if (errmess !== undefined) {
            let mess = JSON.parse(errmess);
            if (mess.message !== undefined) {
                message = mess.message;
            }
        }
        console.debug("Failed to get nmea data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
}