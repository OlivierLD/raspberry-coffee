$(document).ready(function() {
    // Nothing.
});

var errManager = {
  display: alert
};

var getDeferred = function(
    url,                          // full api path
    timeout,                      // After that, fail.
    verb,                         // GET, PUT, DELETE, POST, etc
    happyCode,                    // if met, resolve, otherwise fail.
    data) {                       // payload, when needed (PUT, POST...)

    var deferred = $.Deferred(),  // a jQuery deferred
        url = url,
        xhr = new XMLHttpRequest(),
        TIMEOUT = timeout;

    var req = verb + " " + url;
    if (data !== undefined && data !== null) {
        req += ("\n" + JSON.stringify(data, null, 2));
    }
    xhr.open(verb, url, true);
    xhr.setRequestHeader("Content-type", "application/json");
    if (data === undefined) {
        xhr.send();
    } else {
        xhr.send(JSON.stringify(data));
    }

    var requestTimer = setTimeout(function() {
        xhr.abort();
        var mess = { message: 'Timeout' };
        deferred.reject(408, mess);
    }, TIMEOUT);

    xhr.onload = function() {
        clearTimeout(requestTimer);
        if (xhr.status === happyCode) {
            deferred.resolve(xhr.response);
        } else {
            deferred.reject(xhr.status, xhr.response);
        }
    };
    return deferred.promise();
};

var DEFAULT_TIMEOUT = 10000;

/*
 * Required functions:
 * - Relay Status: getter, setter
 * - Sensor Data: getter, setter
 *
 * TODO
 * Last watering time
 * Status (watering, waiting...)
 */
var getRelayStatus = function() {
    return getDeferred('/pws/relay-state', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

var getSensorData = function() {
    return getDeferred('/pws/sth10-data', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

var setRelayStatus = function(status) {
    return getDeferred('/pws/relay-state', DEFAULT_TIMEOUT, 'PUT', 200, status); // 200 or 201?
};

var setSensorData = function(data) {
    return getDeferred('/pws/sth10-data', DEFAULT_TIMEOUT, 'POST', 200, data);
};

var relayStatus = function() {
    var getData = getRelayStatus();
    getData.done(function(value) {
        var json = JSON.parse(value); // Like {"processing":false,"started":1501082121336}
        var status = json.processing;
        $("#forwarders-status").text(status === true ? 'ON' :'Paused');
    });
    getData.fail(function(error, errmess) {
        var message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager.display("Failed to get the relay status..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
        $("#forwarders-status").text('-');
    });
};

var sensorData = function() {
    var getData = getSensorData();
    getData.done(function(value) {
      var json = JSON.parse(value);
	    $("#temp").text(json.temperature.toFixed(2));
	    $("#hum").text(json.humidity.toFixed(2));
    });
    getData.fail(function(error, errmess) {
        var message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager.display("Failed to get the flow status..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
        pushData(0);
        $('#flow').css('cursor', 'auto');
    });
};

var setStatus = function(state) {
    var putData = setRelayStatus(state);
    putData.done(function(value) {
        // Yo!
    });
    putData.fail(function(error, errmess) {
        document.body.style.cursor = 'default';
        var message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager.display("Failed to set valve status..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

var setData = function(data) {
    var postData = setSensorData(data);
    postData.done(function(value) {
        // Yo!
    });
    postData.fail(function(error, errmess) {
        document.body.style.cursor = 'default';
        var message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager.display("Failed to set sensor data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};
