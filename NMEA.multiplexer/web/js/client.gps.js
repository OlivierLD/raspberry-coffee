"use strict";

if (Math.toDegrees === undefined) {
    Math.toDegrees = (rad) => {
        return rad * (180 / Math.PI);
    }
}

if (Math.toRadians === undefined) {
    Math.toRadians = (deg) => {
        return deg * (Math.PI / 180);
    }
}

var onMessage = function (json) {
    $("#raw-json").text(JSON.stringify(json, null, 2));

    if (json.Position !== undefined) {
        try {
            clear("mapCanvas");
            drawWorldMap("mapCanvas");
        } catch (absorb) {

        }
        plotPositionOnChart({lat: json.Position.lat, lng: json.Position.lng});
    }
    try {
        document.getElementById("fixdate").innerHTML = json["GPS Date & Time"].date;
    } catch (err) {
        console.log("Err", err);
    }
    try {
        nmeaID.innerHTML = '<b>' + json.NMEA + '</b>';
    } catch (err) {
        console.log("Err", err);
    }
    if (json["Satellites in view"] !== undefined) {
        generateSatelliteData(json["Satellites in view"]);
        // Satellites on the chart
        if (json.Position !== undefined) {
            plotSatellitesOnChart({lat: json.Position.lat, lng: json.Position.lng}, json["Satellites in view"]);
        }
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

var deadReckoning = function(from, dist, route) {
  var deltaL = Math.toRadians(dist / 60) * Math.cos(Math.toRadians(route));
  var l2 = from.lat + Math.toDegrees(deltaL);
  var deltaG = Math.toRadians(dist / (60 * Math.cos(Math.toRadians(from.lat + l2) / 2))) * Math.sin(Math.toRadians(route)); // 2009-mar-10
  var g2 = from.lng + Math.toDegrees(deltaG);
  while (g2 > 180) {
    g2 = 360 - g2;
  }
  while (g2 < -180) {
    g2 += 360;
  }
  return { lat: l2, lng: g2 };
};

var plotSatellitesOnChart = function(pos, sd) {
    if (sd !== undefined) {
        for (var sat in sd) {
            var satellite = sd[sat];
            var satellitePosition = deadReckoning(pos, (90 - satellite.elevation) * 60, satellite.azimuth);
        //  console.log("Plotting sat " + satellite.svID + " at " + JSON.stringify(satellitePosition));
            plotSatelliteOnChart(satellitePosition, satellite.svID, getSNRColor(satellite.snr));
        }
    }
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

var decToSex = function (val, ns_ew) {
    var absVal = Math.abs(val);
    var intValue = Math.floor(absVal);
    var dec = absVal - intValue;
    var i = intValue;
    dec *= 60;
//  var s = i + "°" + dec.toFixed(2) + "'";
    var s = i + String.fromCharCode(176) + dec.toFixed(2) + "'";

    if (val < 0) {
        s += (ns_ew === 'NS' ? 'S' : 'W');
    } else {
        s += (ns_ew === 'NS' ? 'N' : 'E');
    }
    return s;
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

var cacheClient = new cacheClient(onMessage);
