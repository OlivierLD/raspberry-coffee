$(document).ready(function() {
    // Nothing.
});

var errManager = {
  display: alert
};

var RESTPayload = {};
var storedHistory = "";
var storedHistoryOut = "";
var storedElapsed = "";

var getDeferred = function(
    url,                          // full api path
    timeout,                      // After that, fail.
    verb,                         // GET, PUT, DELETE, POST, etc
    happyCode,                    // if met, resolve, otherwise fail.
    data,                         // payload, when needed (PUT, POST...)
    show) {                       // Show the traffic [true]|false
    if (show === undefined) {
        show = true;
    }
    if (show === true) {
        document.body.style.cursor = 'wait';
    }
    var deferred = $.Deferred(),  // a jQuery deferred
        url = url,
        xhr = new XMLHttpRequest(),
        TIMEOUT = timeout;

    var req = verb + " " + url;
    if (data !== undefined && data !== null) {
        req += ("\n" + JSON.stringify(data, null, 2));
    }
    if (show === true) {
        storedHistoryOut += ((storedHistoryOut.length > 0 ? "\n" : "") + req);
        displayRawDataOut();
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

var getVolume = function() {
    return getDeferred('/nmea-volume', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

var getSerialPorts = function() {
    return getDeferred('/serial-ports', DEFAULT_TIMEOUT, 'GET', 200);
};

var getChannels = function() {
    return getDeferred('/channels', DEFAULT_TIMEOUT, 'GET', 200);
};

var getForwarders = function() {
    return getDeferred('/forwarders', DEFAULT_TIMEOUT, 'GET', 200);
};

var getComputers = function() {
    return getDeferred('/computers', DEFAULT_TIMEOUT, 'GET', 200);
};

var addForwarder = function(forwarder) {
    return getDeferred('/forwarders', DEFAULT_TIMEOUT, 'POST', 200, forwarder);
};

var addChannel = function(channel) {
    return getDeferred('/channels', DEFAULT_TIMEOUT, 'POST', 200, channel);
};

var addComputer = function(computer) {
    return getDeferred('/computers', DEFAULT_TIMEOUT, 'POST', 200, computer);
};

var updateChannel = function(channel) {
    return getDeferred('/channels/' + channel.type, DEFAULT_TIMEOUT, 'PUT', 200, channel);
};

var updateComputer = function(computer) {
    return getDeferred('/computers/' + computer.type, DEFAULT_TIMEOUT, 'PUT', 200, computer);
};

var updateMuxVerbose = function(value) {
    return getDeferred('/mux-verbose/' + value, DEFAULT_TIMEOUT, 'PUT', 200);
};

var resetDataCache = function() {
    return getDeferred('/cache', DEFAULT_TIMEOUT, 'DELETE', 204);
};

var deleteForwarder = function(forwarder) {
    return getDeferred('/forwarders/' + forwarder.type, DEFAULT_TIMEOUT, 'DELETE', 204, forwarder);
};

var deleteComputer = function(computer) {
    return getDeferred('/computers/' + computer.type, DEFAULT_TIMEOUT, 'DELETE', 204, computer);
};

var deleteChannel = function(channel) {
    return getDeferred('/channels/' + channel.type, DEFAULT_TIMEOUT, 'DELETE', 204, channel);
};

var dataVolume = function() {
  // No REST traffic for this one.
    var getData = getVolume();
    getData.done(function(value) {
        var json = JSON.parse(value); // Like { "nmea-bytes": 13469, "started": 1489004962194 }
        var currentTime = new Date().getTime();
        var elapsed = currentTime - json.started;
        var volume = json["nmea-bytes"];
        var flow = Math.round(volume / (elapsed / 1000));
        $("#flow").text(flow + " bytes/sec.");
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
    });

};

var serialPortList = function() {
  var before = new Date().getTime();
  var getData = getSerialPorts();
  getData.done(function(value) {
    var after = new Date().getTime();
    document.body.style.cursor = 'default';
    console.log("Done in " + (after - before) + " ms :", value);
    var json = JSON.parse(value);
    setRESTPayload(json, (after - before));
    var html = "<h5>Available Serial Ports</h5>";
    if (json.length > 0) {
        html += "<table>";
        json.forEach(function(line, idx) {
            html += ("<tr><td>" + line + "</td></tr>");
        });
        html += "</table>";
    } else {
        html += "<i>No Serial Port available</i>";
    }
    $("#lists").html(html);
    $("#diagram").css('display', 'none');
    $("#lists").css('display', 'block');
  });
  getData.fail(function(error, errmess) {
      document.body.style.cursor = 'default';
      var message;
      if (errmess !== undefined) {
          if (errmess.message !== undefined) {
              message = errmess.message;
          } else {
              message = errmess;
          }
      }
      errManager.display("Failed to get serial ports list..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
  });
};

var buildList = function(arr) {
    var str = (arr !== undefined) ? arr.toString() : "";
    return str;
};

var setRESTPayload = function(payload, elapsed) {
  if (typeof payload === 'string') {
      if (payload.length > 0) {
          RESTPayload = JSON.parse(payload);
      } else {
          RESTPayload = {};
      }
  } else {
      RESTPayload = payload;
  }
  if (true || showRESTData) { // Show anyways
      displayRawData(elapsed);
  }
};

var displayRawData = function(elapsed) {
    var stringified = JSON.stringify(RESTPayload, null, 2);
    storedHistory += ((storedHistory.length > 0 ? "\n" : "") + stringified);
    var content = '<pre>' + storedHistory + '</pre>';
    var elapsedContent = "\n";
    if (elapsed !== undefined) {
        elapsedContent = ('in ' + elapsed + " ms.\n");
    }
    $("#raw-data").html(content);
    $("#raw-data").scrollTop($("#raw-data")[0].scrollHeight);

    storedElapsed += elapsedContent;
    $("#rest-elapsed").html('<pre>' + storedElapsed + "</pre>");
    $("#rest-elapsed").scrollTop($("#rest-elapsed")[0].scrollHeight);
};

var displayRawDataOut = function() {
    $("#raw-data-out").html('<pre>' + storedHistoryOut + '</pre>');
    $("#raw-data-out").scrollTop($("#raw-data-out")[0].scrollHeight);
};

var clearRESTData = function() {
    RESTPayload = {};
    storedHistory  = "";
    $("#raw-data").html("");
};

var clearRESTOutData = function() {
    storedHistoryOut  = "";
    storedElapsed = "";
    $("#raw-data-out").html("");
    $("#rest-elapsed").html("");
};

var channelList = function() {
    var before = new Date().getTime();
    var getData = getChannels();
    getData.done(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        var json = JSON.parse(value);
        setRESTPayload(json, (after - before));
        var html = "<h5>Reads from</h5>" +
            "<table>";
        html += "<tr><th>Type</th><th>Parameters</th><th>Device filters</th><th>Sentence filters</th><th>verb.</th></tr>"
        for (var i=0; i<json.length; i++) {
          var type = json[i].type;
          switch (type) {
              case 'file':
                html += ("<tr><td><b>file</b></td><td>" + json[i].file + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                break;
              case 'serial':
                  html += ("<tr><td><b>serial</b></td><td>" + json[i].port + ":" + json[i].br + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                  break;
              case 'tcp':
                  html += ("<tr><td><b>tcp</b></td><td>" + json[i].hostname + ":" + json[i].port + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                  break;
              case 'ws':
                  html += ("<tr><td><b>ws</b></td><td> " + json[i].wsUri + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                  break;
              case 'rnd':
                  html += ("<tr><td><b>rnd</b></td><td></td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                  break;
              case 'bmp180':
                  html += ("<tr><td><b>bmp180</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                  break;
              case 'bme280':
                  html += ("<tr><td><b>bme280</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                  break;
              case 'htu21df':
                  html += ("<tr><td><b>htu21df</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                  break;
              default:
                html += ("<tr><td><b><i>" + type + "</i></b></td><td>" + json[i].cls + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                break;
          }
        }
        html += "</table>";
        $("#lists").html(html);
        $("#diagram").css('display', 'none');
        $("#lists").css('display', 'block');
    });
    getData.fail(function(error, errmess) {
        document.body.style.cursor = 'default';
        var message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager.display("Failed to get channels list..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

var forwarderList = function() {
    var before = new Date().getTime();
    var getData = getForwarders();
    getData.done(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        var json = JSON.parse(value);
        setRESTPayload(json, (after - before));
        var html = "<h5>Writes to</h5>" +
            "<table>";
        html += "<tr><th>Type</th><th>Parameters</th></th></tr>";
        for (var i=0; i<json.length; i++) {
            var type = json[i].type;
            switch (type) {
                case 'file':
                    html += ("<tr><td><b>file</b></td><td>" + json[i].log + ", " + (json[i].append === true ? 'append' : 'reset') + " mode.</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'serial':
                    html += ("<tr><td><b>serial</b></td><td>" + json[i].port + ":" + json[i].br + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'tcp':
                    html += ("<tr><td><b>tcp</b></td><td>Port " + json[i].port + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td><td><small>" + json[i].nbClients + " Client(s)</small></td></tr>");
                    break;
                case 'gpsd':
                    html += ("<tr><td><b>gpsd</b></td><td>Port " + json[i].port + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td><td><small>" + json[i].nbClients + " Client(s)</small></td></tr>");
                    break;
                case 'ws':
                    html += ("<tr><td><b>ws</b></td><td>" + json[i].wsUri + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'rmi':
                    html += ("<tr><td valign='top'><b>rmi</b></td><td valign='top'>" +
                    "Port: " + json[i].port + "<br>" +
                    "Name: " + json[i].bindingName + "<br>" +
                    "Address: " + json[i].serverAddress +
                    "</td><td valign='top'><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'console':
                    html += ("<tr><td><b>console</b></td><td></td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                default:
                    html += ("<tr><td><b><i>" + type + "</i></b></td><td>" + json[i].cls + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
            }
        }
        html += "</table>";
        $("#lists").html(html);
        $("#diagram").css('display', 'none');
        $("#lists").css('display', 'block');
    });
    getData.fail(function(error, errmess) {
        document.body.style.cursor = 'default';
        var message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager.display("Failed to get forwarders list..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

var computerList = function() {
    var before = new Date().getTime();
    var getData = getComputers();
    getData.done(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        var json = JSON.parse(value);
        setRESTPayload(json, (after - before));
        var html = "<h5>Computes and writes</h5>" +
            "<table>";
        html += "<tr><th>Type</th><th>Parameters</th><th>verb.</th></tr>";
        for (var i=0; i<json.length; i++) {
            var type = json[i].type;
            switch (type) {
                case 'tw-current':
                    html += ("<tr><td valign='top'><b>tw-current</b></td><td valign='top'>Prefix: " + json[i].prefix + "<br>Timebuffer length: " + json[i].timeBufferLength.toLocaleString() + " ms.</td><td valign='top' align='center'><input type='checkbox' title='verbose' onchange='manageComputerVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose === true ? " checked" : "") + "></td><td valign='top'><button onclick='removeComputer(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                default:
                    html += ("<tr><td valign='top'><b><i>" + type + "</i></b></td><td valign='top'>" + json[i].cls + "</td><td valign='top' align='center'><input type='checkbox' title='verbose' onchange='manageComputerVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose === true ? " checked" : "") + "></td><td valign='top'><button onclick='removeComputer(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
            }
        }
        html += "</table>";
        $("#lists").html(html);
        $("#diagram").css('display', 'none');
        $("#lists").css('display', 'block');
    });
    getData.fail(function(error, errmess) {
        document.body.style.cursor = 'default';
        var message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager.display("Failed to get nmea.computers list..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};


var buildTable = function (channels, forwarders, computers) {
    var html = "<table width='100%'>" +
        "<tr><th width='45%'>Pulled in</th><th width='10%'></th><th width='45%'>Pushed out</th></tr>" +
        "<tr><td valign='middle' align='center' rowspan='2' title='Channels'>" + channels + "</td>" +
        "<td valign='middle' align='center' rowspan='2'><b><i>MUX</i></b></td>" +
        "<td valign='middle' align='center' title='Forwarders'>" + forwarders + "</td></tr>" +
        "<tr><td valign='middle' align='center' title='Computers'>" + computers + "</td></tr>" +
        "</table>";
    return html;
};

var valueOrText = function(value, ifEmpty) {
    if (value === undefined || value === null || value.trim().length === 0) {
        return "<span style='color: lightgrey;'>" + ifEmpty + "</span>";
    } else {
        return value;
    }
};

var generateDiagram = function () {

    var nbPromises = 0;
    var channelTable = "";
    var forwarderTable = "";
    var computerTable = "";

    var getChannelPromise = getChannels();
    getChannelPromise.done(function(value) {
        var before = new Date().getTime();
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        var json = JSON.parse(value);
        setRESTPayload(json, (after - before));
        var html = "<table>";
        for (var i=0; i<json.length; i++) {
            var type = json[i].type;
            switch (type) {
                case 'file':
                    html += ("<tr><td><b>file</b></td><td>" + json[i].file +
                    "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                    "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Sentence Filter') +
                    "</td><td align='center'><input type='checkbox' title='verbose' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'serial':
                    html += ("<tr><td><b>serial</b></td><td>" + json[i].port + ":" + json[i].br +
                    "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                    "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                    "</td><td align='center'><input type='checkbox' title='verbose' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'tcp':
                    html += ("<tr><td><b>tcp</b></td><td>" + json[i].hostname + ":" + json[i].port +
                    "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                    "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                    "</td><td align='center'><input type='checkbox' title='verbose' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'ws':
                    html += ("<tr><td><b>ws</b></td><td> " + json[i].wsUri +
                    "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                    "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                    "</td><td align='center'><input type='checkbox' title='verbose' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'rnd':
                    html += ("<tr><td><b>rnd</b></td><td></td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                    "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                    "</td><td align='center'><input type='checkbox' title='verbose' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'bmp180':
                    html += ("<tr><td><b>bmp180</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") +
                    "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                    "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                    "</td><td align='center'><input type='checkbox' title='verbose' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'bme280':
                    html += ("<tr><td><b>bme280</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") +
                    "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                    "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                    "</td><td align='center'><input type='checkbox' title='verbose' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'htu21df':
                    html += ("<tr><td><b>htu21df</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") +
                    "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                    "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                    "</td><td align='center'><input type='checkbox' title='verbose' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                default:
                    html += ("<tr><td><b><i>" + type + "</i></b></td><td>" + json[i].cls +
                    "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                    "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                    "</td><td align='center'><input type='checkbox' title='verbose' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
            }
        }
        html += "</table>";
        channelTable = html;
        nbPromises += 1;
        if (nbPromises === 3) {
            $("#diagram").html(buildTable(channelTable, forwarderTable, computerTable));
            $("#diagram").css('display', 'block');
            $("#lists").css('display', 'none');
        }
    });
    getChannelPromise.fail(function(error, errmess) {
        document.body.style.cursor = 'default';
        var message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        } else {
            message = 'Failed to get the channels';
        }
        channelTable = "<span style='color: red;'>" + message + "</span>";
        nbPromises += 1;
        if (nbPromises === 3) {
            $("#diagram").html(buildTable(channelTable, forwarderTable, computerTable));
            $("#diagram").css('display', 'block');
            $("#lists").css('display', 'none');
        }
        errManager.display("Failed to get channels list..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });

    var getForwarderPromise = getForwarders();
    getForwarderPromise.done(function(value) {
        var before = new Date().getTime();
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        var json = JSON.parse(value);
        setRESTPayload(json, (after - before));
        var html = "<table>";
        for (var i=0; i<json.length; i++) {
            var type = json[i].type;
            switch (type) {
                case 'file':
                    html += ("<tr><td><b>file</b></td><td>" + json[i].log + ", " + (json[i].append === true ? 'append' : 'reset') + " mode.</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'serial':
                    html += ("<tr><td><b>serial</b></td><td>" + json[i].port + ":" + json[i].br + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'tcp':
                    html += ("<tr><td><b>tcp</b></td><td>Port " + json[i].port + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td><td><small>" + json[i].nbClients + " Client(s)</small></td></tr>");
                    break;
                case 'gpsd':
                    html += ("<tr><td><b>gpsd</b></td><td>Port " + json[i].port + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td><td><small>" + json[i].nbClients + " Client(s)</small></td></tr>");
                    break;
                case 'ws':
                    html += ("<tr><td><b>ws</b></td><td>" + json[i].wsUri + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'rmi':
                    html += ("<tr><td valign='top'><b>rmi</b></td><td valign='top'>" +
                    "Port: " + json[i].port + "<br>" +
                    "Name: " + json[i].bindingName + "<br>" +
                    "Address: " + json[i].serverAddress +
                    "</td><td valign='top'><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'console':
                    html += ("<tr><td><b>console</b></td><td>" + valueOrText('', 'No parameter') + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                default:
                    html += ("<tr><td><b><i>" + type + "</i></b></td><td>" + json[i].cls + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
            }
        }
        html += "</table>";
        forwarderTable = html;
        nbPromises += 1;
        if (nbPromises === 3) {
            $("#diagram").html(buildTable(channelTable, forwarderTable, computerTable));
            $("#diagram").css('display', 'block');
            $("#lists").css('display', 'none');
        }
    });
    getForwarderPromise.fail(function(error, errmess) {
        document.body.style.cursor = 'default';
        var message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        } else {
            message = 'Failed to get the Forwarders';
        }
        forwarderTable = "<span style='color: red;'>" + message + "</span>";
        nbPromises += 1;
        if (nbPromises === 3) {
            $("#diagram").html(buildTable(channelTable, forwarderTable, computerTable));
            $("#diagram").css('display', 'block');
            $("#lists").css('display', 'none');
        }
        errManager.display("Failed to get forwarders list..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });

    var getComputerPromise = getComputers();
    getComputerPromise.done(function(value) {
        var before = new Date().getTime();
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        var json = JSON.parse(value);
        setRESTPayload(json, (after - before));
        var html = "<table>";
        for (var i=0; i<json.length; i++) {
            var type = json[i].type;
            switch (type) {
                case 'tw-current':
                    html += ("<tr><td valign='top'><b>tw-current</b></td><td valign='top'>Prefix: " + json[i].prefix + "<br>Timebuffer length: " + json[i].timeBufferLength.toLocaleString() + " ms.</td><td valign='top' align='center'><input type='checkbox' title='verbose' onchange='manageComputerVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose === true ? " checked" : "") + "></td><td valign='top'><button onclick='removeComputer(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                default:
                    html += ("<tr><td valign='top'><b><i>" + type + "</i></b></td><td valign='top'>" + json[i].cls + "</td><td valign='top' align='center'><input type='checkbox' title='verbose' onchange='manageComputerVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose === true ? " checked" : "") + "></td><td valign='top'><button onclick='removeComputer(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
            }
        }
        html += "</table>";
        computerTable = html;
        nbPromises += 1;
        if (nbPromises === 3) {
            $("#diagram").html(buildTable(channelTable, forwarderTable, computerTable));
            $("#diagram").css('display', 'block');
            $("#lists").css('display', 'none');
        }
    });
    getComputerPromise.fail(function(error, errmess) {
        document.body.style.cursor = 'default';
        var message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        computerTable = "<span style='color: red;'>" + message + "</span>";
        nbPromises += 1;
        if (nbPromises === 3) {
            $("#diagram").html(buildTable(channelTable, forwarderTable, computerTable));
            $("#diagram").css('display', 'block');
            $("#lists").css('display', 'none');
        }
        errManager.display("Failed to get nmea.computers list..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

var createChannel = function(channel) {
    var before = new Date().getTime();
    var postData = addChannel(channel);
    postData.done(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        channelList(); // refetch
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
        errManager.display("Failed to create channel..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

var createForwarder = function(forwarder) {
    var before = new Date().getTime();
    var postData = addForwarder(forwarder);
    postData.done(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        forwarderList(); // refetch
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
        errManager.display("Failed to create forwarder..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

var createComputer = function(computer) {
    var before = new Date().getTime();
    var postData = addComputer(computer);
    postData.done(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        computerList(); // refetch
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
        errManager.display("Failed to create computer..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

var removeChannel = function(channel) {
    var before = new Date().getTime();
    var deleteData = deleteChannel(channel);
    deleteData.done(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        channelList(); // refetch
    });
    deleteData.fail(function(error, errmess) {
        document.body.style.cursor = 'default';
        var message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager.display("Failed to delete channel..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

var removeForwarder = function(channel) {
    var before = new Date().getTime();
    var deleteData = deleteForwarder(channel);
    deleteData.done(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        forwarderList(); // refetch
    });
    deleteData.fail(function(error, errmess) {
        document.body.style.cursor = 'default';
        var message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager.display("Failed to delete forwarder..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

var removeComputer = function(computer) {
    var before = new Date().getTime();
    var deleteData = deleteComputer(computer);
    deleteData.done(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        computerList(); // refetch
    });
    deleteData.fail(function(error, errmess) {
        document.body.style.cursor = 'default';
        var message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager.display("Failed to delete computer..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

var changeChannel = function(channel) {
    var before = new Date().getTime();
    var putData = updateChannel(channel);
    putData.done(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        channelList(); // refetch
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
        errManager.display("Failed to update channel..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

var changeComputer = function(computer) {
    var before = new Date().getTime();
    var putData = updateComputer(computer);
    putData.done(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        computerList(); // refetch
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
        errManager.display("Failed to update computer..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

var manageChannelVerbose = function(cb, channel) {
    console.log('Clicked checkbox on', channel, ' checked:', cb.checked);
    // PUT on the channel.
    channel.verbose = cb.checked;
    changeChannel(channel);
};

var manageComputerVerbose = function(cb, computer) {
    console.log('Clicked checkbox on', computer, ' checked:', cb.checked);
    // PUT on the channel.
    computer.verbose = cb.checked;
    changeComputer(computer);
};

var manageMuxVerbose = function(cb) {
    var before = new Date().getTime();
    var updateMux = updateMuxVerbose(cb.checked ? 'on' : 'off');
    updateMux.done(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        RESTPayload = value;
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
    });
    updateMux.fail(function(error, errmess) {
        document.body.style.cursor = 'default';
        var message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager.display("Failed to update multiplexer..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

var resetCache = function() {
    var before = new Date().getTime();
    var reset = resetDataCache();
    reset.done(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        RESTPayload = value;
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
    });
    reset.fail(function(error, errmess) {
        document.body.style.cursor = 'default';
        var message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager.display("Failed to reset data cache..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

var addChannelVisible = false;
var addForwarderVisible = false;
var addComputerVisible = false;

var showAddChannel = function() {
    addChannelVisible = !addChannelVisible;
    addForwarderVisible = false;
    addComputerVisible = false;
    showDivs(addChannelVisible, addForwarderVisible, addComputerVisible);
};

var showAddForwarder = function() {
    addChannelVisible = false;
    addForwarderVisible = !addForwarderVisible;
    addComputerVisible = false;
    showDivs(addChannelVisible, addForwarderVisible, addComputerVisible);
};

var showAddComputer = function() {
    addChannelVisible = false;
    addForwarderVisible = false;
    addComputerVisible = !addComputerVisible;
    showDivs(addChannelVisible, addForwarderVisible, addComputerVisible);
};

var showDivs = function(channels, forwarders, computers) {
//  console.log("Displaying divs: channels " + (channels === true ? 'X' : 'O') + " forwarders " + (forwarders === true ? 'X' : 'O') + " computers " + (computers === true ? 'X' : 'O'));
    $("#add-channel").css('display', (channels === true ? 'inline' : 'none'));
    $("#add-forwarder").css('display', (forwarders === true ? 'inline' : 'none'));
    $("#add-computer").css('display', (computers === true ? 'inline' : 'none'));
};
