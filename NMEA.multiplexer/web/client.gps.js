"use strict";

var getNMEAData = function() {
    var deferred = $.Deferred(),  // a jQuery deferred
        url = '/cache',
        xhr = new XMLHttpRequest(),
        TIMEOUT = 10000;

    xhr.open('GET', url, true);
    xhr.setRequestHeader("Content-type", "application/json");
    xhr.send();

    var requestTimer = setTimeout(function() {
        xhr.abort();
        deferred.reject(408, { message: 'Timeout'});
    }, TIMEOUT);

    xhr.onload = function() {
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
  setInterval(function() {
    fetch();
  }, 1000);
})();

var fetch = function() {
    var getData = getNMEAData();
    getData.done(function(value) {
        console.log("Done:", value);
        var json = JSON.parse(value);
        onMessage(json);
    });
    getData.fail(function(error, errmess) {
        var message;
        if (errmess !== undefined) {
            var mess = JSON.parse(errmess);
            if (mess.message !== undefined) {
                message = mess.message;
            }
        }
        alert("Failed to get nmea data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

var onMessage = function (json) {
    $("#raw-json").text(JSON.stringify(json, null, 2));

    if (json.Position !== undefined) {
        plotPositionOnChart({lat: json.Position.lat, lng: json.Position.lng});
    }
    document.getElementById("fixdate").innerHTML = json["GPS Date & Time"].date;
    nmeaID.innerHTML = '<b>' + json.NMEA + '</b>';
    if (json["Satellites in view"] !== undefined) {
        generateSatelliteData(json["Satellites in view"]);
    }
    if (json.COG !== undefined) {
        rose.setValue(Math.round(json.COG.angle));
    }
    if (json.SOG !== undefined) {
        displayBSP.setValue(json.SOG.speed);
    }
};

var generateSatelliteData = function(sd) {
    var html = "<table cellspacing='10'>";
    html += "<tr><th>PRN</th><th>Alt.</th><th>Z</th><th>snr</th></tr>";
    if (sd !== undefined) {
        // Send to plotter here.
        if (satellitesPlotter !== undefined) {
            satellitesPlotter.setSatellites(sd);
        }

        for (var sat in sd) {
            html += "<tr>" +
                "<td align='center' bgcolor='black' style='color: " + getSNRColor(sd[sat].snr) + ";'>" + sd[sat].svID +
                "</td><td align='right'>" + sd[sat].elevation +
                "&deg;</td><td align='right'>" + sd[sat].azimuth +
                "&deg;</td><td align='right'>" + sd[sat].snr + "</td></tr>";
        }
    }
    html += "</table>";
    satData.innerHTML = html;
};

var getSNRColor = function(snr) {
    var c = 'lightGray';
    if (snr !== undefined && snr !== null) {
        if (snr > 0) {
            c = 'red';
        }
        if (snr > 10) {
            c = 'orange';
        }
        if (snr > 20) {
            c = 'yellow';
        }
        if (snr > 30) {
            c = 'lightGreen';
        }
        if (snr > 40) {
            c = 'green';
        }
    }
    return c;
};

var displayMessage = function(mess) {
  var messList = statusFld.innerHTML;
  messList = (((messList !== undefined && messList.length) > 0 ? messList + '<br>' : '') + mess);
  statusFld.innerHTML = messList;
  statusFld.scrollTop = statusFld.scrollHeight; // Scroll down
};

var resetStatus = function() {
  statusFld.innerHTML = "";
};

var setConnectionStatus = function(ok) {
  var title = document.getElementById("title");
  if (title !== undefined) {
    title.style.color = (ok === true ? 'green' : 'red');
  }
};
