"use strict";

var cacheClient = function(dataManager, bp) {

    var onMessage = dataManager; // Client function
    var betweenPing = 1000;
    if (bp !== undefined) {
        betweenPing = bp;
    }

    var getNMEAData = function () {
        var deferred = $.Deferred(),  // a jQuery deferred
            url = '/cache',
            xhr = new XMLHttpRequest(),
            TIMEOUT = 10000;

        xhr.open('GET', url, true);
        xhr.setRequestHeader("Content-type", "application/json");
        try {
            xhr.send();
        } catch (err) {
            throw err;
        }

        var requestTimer = setTimeout(function () {
            xhr.abort();
            deferred.reject(408, {message: 'Timeout'});
        }, TIMEOUT);

        xhr.onload = function () {
            clearTimeout(requestTimer);
            if (xhr.status === 200) {
                deferred.resolve(xhr.response);
            } else {
                deferred.reject(xhr.status, xhr.response);
            }
        };
        return deferred.promise();
    };

// Executed at startup
    (function () {
        // Long poll
        setInterval(function () {
            fetch();
        }, betweenPing);
    })();

    var fetch = function () {
        var getData = getNMEAData();
        getData.done(function (value) {
            //  console.log("Done:", value);
            var json = JSON.parse(value);
            onMessage(json);
        });
        getData.fail(function (error, errmess) {
            var message;
            if (errmess !== undefined) {
                try {
                    var mess = JSON.parse(errmess);
                    if (mess.message !== undefined) {
                        message = mess.message;
                    }
                } catch (err) {
                //  console.log(errmess);
                }
            }
            console.log("Failed to get nmea data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
        });
    };

};
