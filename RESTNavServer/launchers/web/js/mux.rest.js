var errManager = {
  display: alert
};
var RESTPayload = {};
var storedHistory = "";
var storedHistoryOut = "";
var storedElapsed = "";

let DEBUG = false

/* Uses ES6 Promises */
function getPromise(
		url,                          // full api path
		timeout,                      // After that, fail.
		verb,                         // GET, PUT, DELETE, POST, etc
		happyCode,                    // if met, resolve, otherwise fail.
		data = null,                  // payload, when needed (PUT, POST...)
		show = false) {               // Show the traffic [true]|false

	if (show === true) {
		document.body.style.cursor = 'wait';
	}

	if (DEBUG) {
		console.log(">>> Promise", verb, url);
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
			let mess = {code: 408, message: 'Timeout'};
			reject(mess);
		}, TIMEOUT);

		xhr.onload = function () {
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

var DEFAULT_TIMEOUT = 10000;

var protocolTestFunc = function() {
    var url = document.location.origin.replace('http', 'mux') + '/this-is-a-test';
    return getPromise(url, DEFAULT_TIMEOUT, 'POST', 200, null, false);
};

var terminate = function() {
    return getPromise('/mux/terminate', DEFAULT_TIMEOUT, 'POST', 200, null, false);
};

var enableLogging = function(b) {
    return getPromise('/mux/mux-process/' + (b === true ? 'on' : 'off'), DEFAULT_TIMEOUT, 'PUT', 200, null, false);
};

var getForwarderStatus = function() {
    return getPromise('/mux/mux-process', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

var getLogFiles = function() {
	return getPromise('/mux/log-files', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

// Should be useless..., invoke it directly (no promise required) to download.
var getLogFile = function(fileName) {
	return getPromise('/mux/log-files/' + fileName, DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

var deleteLogFile = function(logFile) {
	return getPromise('/mux/log-files/' + logFile, DEFAULT_TIMEOUT, 'DELETE', 200, null, false);
};

var getSystemTime = function() {
	return getPromise('/mux/system-time?fmt=date', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

var getVolume = function() {
    return getPromise('/mux/nmea-volume', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

var getRunData = function() {
    return getPromise('/mux/run-data', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

var getLastSentence = function() {
    return getPromise('/mux/last-sentence', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

var getSOGCOG = function() {
    return getPromise('/mux/sog-cog', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

var getDistance = function() {
    return getPromise('/mux/distance', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

var getDeltaAlt = function() {
    return getPromise('/mux/delta-alt', DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

var getSerialPorts = function() {
    return getPromise('/mux/serial-ports', DEFAULT_TIMEOUT, 'GET', 200);
};

var getChannels = function() {
    return getPromise('/mux/channels', DEFAULT_TIMEOUT, 'GET', 200);
};

var getForwarders = function() {
    return getPromise('/mux/forwarders', DEFAULT_TIMEOUT, 'GET', 200);
};

var getComputers = function() {
    return getPromise('/mux/computers', DEFAULT_TIMEOUT, 'GET', 200);
};

var addForwarder = function(forwarder) {
    return getPromise('/mux/forwarders', DEFAULT_TIMEOUT, 'POST', 200, forwarder);
};

var addChannel = function(channel) {
    return getPromise('/mux/channels', DEFAULT_TIMEOUT, 'POST', 200, channel);
};

var addComputer = function(computer) {
    return getPromise('/mux/computers', DEFAULT_TIMEOUT, 'POST', 200, computer);
};

var updateChannel = function(channel) {
    return getPromise('/mux/channels/' + channel.type, DEFAULT_TIMEOUT, 'PUT', 200, channel);
};

var updateComputer = function(computer) {
    return getPromise('/mux/computers/' + computer.type, DEFAULT_TIMEOUT, 'PUT', 200, computer);
};

var updateMuxVerbose = function(value) {
    return getPromise('/mux/mux-verbose/' + value, DEFAULT_TIMEOUT, 'PUT', 200);
};

var resetDataCache = function() {
    return getPromise('/mux/cache', DEFAULT_TIMEOUT, 'DELETE', 204);
};

var deleteForwarder = function(forwarder) {
    return getPromise('/mux/forwarders/' + forwarder.type, DEFAULT_TIMEOUT, 'DELETE', 204, forwarder);
};

var deleteComputer = function(computer) {
    return getPromise('/mux/computers/' + computer.type, DEFAULT_TIMEOUT, 'DELETE', 204, computer);
};

var deleteChannel = function(channel) {
    return getPromise('/mux/channels/' + channel.type, DEFAULT_TIMEOUT, 'DELETE', 204, channel);
};

var setSpeedUnit = function(speedUnit) {
    return getPromise('/mux/events/change-speed-unit', DEFAULT_TIMEOUT, 'POST', 200, { "speed-unit": speedUnit }, false);
};

var pushData = function(flow) {
    if (false && flowData.length < (INIT_SIZE - 1)) {
        flowData.splice(0, 1);
        flowData.push(new Tuple(flowData.length, flow));
    } else {
        flowData.push(new Tuple(flowData.length, flow));
    }
    $("#flow").text(flow + " bytes/sec.");
    if (GRAPH_MAX_LEN !== undefined && flowData.length > GRAPH_MAX_LEN) {
        while (flowData.length > GRAPH_MAX_LEN) {
            flowData.splice(0, 1);
        }
    }
};

var protocolTest = function() {
    var postData = protocolTestFunc();
    postData.then(function(value) {
        console.log(value);
    }, function(error, errmess) {
        var message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager.display("Failed to get protocol test status..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

var forwarderStatus = function() {
  // No REST traffic for this one.
    var getData = getForwarderStatus();
    getData.then(function(value) {
        var json = JSON.parse(value); // Like {"processing":false,"started":1501082121336}
        var status = json.processing;
        $("#forwarders-status").text(status === true ? 'ON' :'Paused');
    }, function(error, errmess) {
        var message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager.display("Failed to get the forwarders status..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
        $("#forwarders-status").text('-');
    });
};

var dataVolume = function() {
  // No REST traffic for this one.
    $('#flow').css('cursor', 'progress');
    var getData = getVolume();
    getData.then(function(value) {
        var json = JSON.parse(value); // Like { "nmea-bytes": 13469, "started": 1489004962194 }
        var currentTime = new Date().getTime();
        var elapsed = currentTime - json.started;
        var volume = json["nmea-bytes"];
        var flow = Math.round(volume / (elapsed / 1000));
        pushData(flow);
        $('#flow').css('cursor', 'auto');
    }, function(error, errmess) {
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

var storedNMEA = "";
var stackNMEAData = function(sentenceToAdd) {
    storedNMEA += ((storedNMEA.length > 0 ? "\n" : "") + sentenceToAdd); // TODO Limit the size...
    var content = '<pre>' + storedNMEA + '</pre>';
    $("#inbound-data-div").html(content);
    $("#inbound-data-div").scrollTop($("#inbound-data-div")[0].scrollHeight);
};

var lastTimeStamp = 0;
var getLastNMEASentence = function() {
    // No REST traffic for this one.
    var getData = getLastSentence();
    getData.then(function(value) {
        var json = JSON.parse(value); // Like { "nmea-bytes": 13469, "started": 1489004962194 }
        var lastString = json["last-data"];
        var timestamp = json["timestamp"];
        if (timestamp > lastTimeStamp) {
            stackNMEAData(lastString);
            lastTimeStamp = timestamp;
//          console.log(lastString)
        }
    }, function(error, errmess) {
        var message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager.display("Failed to get the last NMEA Data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

var serialPortList = function() {
  var before = new Date().getTime();
  var getData = getSerialPorts();
  getData.then(function(value) {
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
  }, function(error, errmess) {
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
  if ($("#raw-data-out") !== undefined) {
    $("#raw-data-out").html('<pre>' + storedHistoryOut + '</pre>');
    if ($("#raw-data-out")[0] !== undefined) {
      $("#raw-data-out").scrollTop($("#raw-data-out")[0].scrollHeight);
    }
  }
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
    getData.then(function(value) {
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
                html += ("<tr><td valign='top'><b>file</b></td><td valign='top'>Name: " + json[i].file + "<br>Between reads: " + json[i].pause + " ms" + "<br>Loop: " + json[i].loop + "</td><td valign='top'>" + buildList(json[i].deviceFilters) + "</td><td valign='top'>" + buildList(json[i].sentenceFilters) + "</td><td align='center' valign='top'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td valign='top'><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
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
              case 'lsm303':
                  html += ("<tr><td><b>lsm303</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td>");
                  if (json[i].headingOffset !== undefined) {
                      html += ("<td>Heading Offset: " + json[i].headingOffset + "</td>");
                  }
		              if (json[i].readFrequency !== undefined) {
			              html += ("<td>Read Frequency: " + json[i].readFrequency + " ms</td>");
		              }
		              if (json[i].dampingSize !== undefined) {
			              html += ("<td>Damping Size: " + json[i].dampingSize + " elmts</td>");
		              }
                  html += "</tr>";
                  break;
              case 'zda':
                  html += ("<tr><td><b>zda</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") + "</td><td>" + buildList(json[i].deviceFilters) + "</td><td>" + buildList(json[i].sentenceFilters) + "</td><td align='center'><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
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
    }, function(error, errmess) {
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
    getData.then(function(value) {
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
    }, function(error, errmess) {
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
    getData.then(function(value) {
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
    }, function(error, errmess) {
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
        "<tr><th width='45%'>Pulled in from</th><th width='10%'></th><th width='45%'>Pushed out to</th></tr>" +
        "<tr><td valign='middle' align='center' rowspan='2' title='Channels'>" + channels + "</td>" +
//      "<td valign='middle' align='center' rowspan='2'><b><i>MUX</i></b></td>" +
		    "<td valign='middle' align='center' rowspan='2'><img src='images/antenna.png' width='32' height='32' alt='MUX' title='MUX'></td>" +
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
    getChannelPromise.then(function(value) {
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
                    html += ("<tr><td valign='top'><b>file</b></td><td valign='top'>File: " + json[i].file + "<br>Between reads: " + json[i].pause + " ms" + "<br>Loop: " + json[i].loop +
                    "</td><td valign='top'>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                    "</td><td valign='top'>" + valueOrText(buildList(json[i].sentenceFilters), 'No Sentence Filter') +
                    "</td></tr>");
                    break;
                case 'serial':
                    html += ("<tr><td><b>serial</b></td><td>" + json[i].port + ":" + json[i].br +
                    "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                    "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                    "</td></tr>");
                    break;
                case 'tcp':
                    html += ("<tr><td><b>tcp</b></td><td>" + json[i].hostname + ":" + json[i].port +
                    "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                    "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                    "</td></tr>");
                    break;
                case 'ws':
                    html += ("<tr><td><b>ws</b></td><td> " + json[i].wsUri +
                    "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                    "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                    "</td></tr>");
                    break;
                case 'rnd':
                    html += ("<tr><td><b>rnd</b></td><td></td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                    "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                    "</td></tr>");
                    break;
                case 'bmp180':
                    html += ("<tr><td><b>bmp180</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") +
                    "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                    "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                    "</td></tr>");
                    break;
                case 'bme280':
                    html += ("<tr><td><b>bme280</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") +
                    "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                    "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                    "</td></tr>");
                    break;
                case 'lsm303':
                    html += ("<tr><td><b>lsm303</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") +
                    "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                    "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
		                    ((json[i].headingOffset !== undefined && json[i].headingOffset !== 0) ? ("<td>Heading Offset: " + json[i].headingOffset + "</td>") : "" ) +
		                    ((json[i].readFrequency !== undefined && json[i].readFrequency !== 0) ? ("<td>Read Frequency: " + json[i].readFrequency + " ms</td>") : "" ) +
		                    ((json[i].dampingSize !== undefined && json[i].dampingSize !== 0) ? ("<td>Damping Size: " + json[i].dampingSize + " elmts</td>") : "" ) +
                    "</td></tr>");
                    break;
                case 'zda':
                    html += ("<tr><td><b>zda</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") +
                    "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                    "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                    "</td></tr>");
                    break;
                case 'htu21df':
                    html += ("<tr><td><b>htu21df</b></td><td>" + (json[i].devicePrefix !== undefined ? json[i].devicePrefix : "") +
                    "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                    "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                    "</td></tr>");
                    break;
                default:
                    html += ("<tr><td><b><i>" + type + "</i></b></td><td>" + json[i].cls +
                    "</td><td>" + valueOrText(buildList(json[i].deviceFilters), 'No Device Filter') +
                    "</td><td>" + valueOrText(buildList(json[i].sentenceFilters), 'No Device Filter') +
                    "</td></tr>");
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
    }, function(error, errmess) {
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
    getForwarderPromise.then(function(value) {
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
                    html += ("<tr><td><b>file</b></td><td>" + json[i].log + ", " + (json[i].append === true ? 'append' : 'reset') + " mode.</td></tr>");
                    break;
                case 'serial':
                    html += ("<tr><td><b>serial</b></td><td>" + json[i].port + ":" + json[i].br + "</td></tr>");
                    break;
                case 'tcp':
                    html += ("<tr><td><b>tcp</b></td><td>Port " + json[i].port + "</td><td><small>" + json[i].nbClients + " Client(s)</small></td></tr>");
                    break;
                case 'gpsd':
                    html += ("<tr><td><b>gpsd</b></td><td>Port " + json[i].port + "</td><td><small>" + json[i].nbClients + " Client(s)</small></td></tr>");
                    break;
                case 'ws':
                    html += ("<tr><td><b>ws</b></td><td>" + json[i].wsUri + "</td></tr>");
                    break;
                case 'rmi':
                    html += ("<tr><td valign='top'><b>rmi</b></td><td valign='top'>" +
                    "Port: " + json[i].port + "<br>" +
                    "Name: " + json[i].bindingName + "<br>" +
                    "Address: " + json[i].serverAddress +
                    "</td></tr>");
                    break;
                case 'console':
                    html += ("<tr><td><b>console</b></td><td>" + valueOrText('', 'No parameter') + "</td></tr>");
                    break;
                default:
                    html += ("<tr><td><b><i>" + type + "</i></b></td><td>" + json[i].cls + "</td></tr>");
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
    }, function(error, errmess) {
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
    getComputerPromise.then(function(value) {
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
                    html += ("<tr><td valign='top'><b>tw-current</b></td><td valign='top'>Prefix: " + json[i].prefix + "<br>Timebuffer length: " + json[i].timeBufferLength.toLocaleString() + " ms.</td></tr>");
                    break;
                default:
                    html += ("<tr><td valign='top'><b><i>" + type + "</i></b></td><td valign='top'>" + json[i].cls + "</td></tr>");
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
    }, function(error, errmess) {
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
    postData.then(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        channelList(); // refetch
    }, function(error, errmess) {
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
    postData.then(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        forwarderList(); // refetch
    }, function(error, errmess) {
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
    postData.then(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        computerList(); // refetch
    }, function(error, errmess) {
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
    deleteData.then(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        channelList(); // refetch
    }, function(error, errmess) {
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
    deleteData.then(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        forwarderList(); // refetch
    }, function(error, errmess) {
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
    deleteData.then(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        computerList(); // refetch
    }, function(error, errmess) {
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
    putData.then(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        channelList(); // refetch
    }, function(error, errmess) {
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
    putData.then(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
        computerList(); // refetch
    }, function(error, errmess) {
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
    updateMux.then(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        RESTPayload = value;
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
    }, function(error, errmess) {
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
    reset.then(function(value) {
        var after = new Date().getTime();
        document.body.style.cursor = 'default';
        RESTPayload = value;
        console.log("Done in " + (after - before) + " ms :", value);
        setRESTPayload(value, (after - before));
    }, function(error, errmess) {
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
//   $("#add-channel").css('display', (channels === true ? 'inline' : 'none'));
    if (channels === true) {
      $("#add-channel").show(1000);
    } else {
      $("#add-channel").hide(1000);
    }
//  $("#add-forwarder").css('display', (forwarders === true ? 'inline' : 'none'));
    if (forwarders === true) {
      $("#add-forwarder").show(1000);
    } else {
      $("#add-forwarder").hide(1000);
    }
//  $("#add-computer").css('display', (computers === true ? 'inline' : 'none'));
    if (computers === true) {
      $("#add-computer").show(1000);
    } else {
      $("#add-computer").hide(1000);
    }
};

var decToSex = function (val, ns_ew, withDeg) {
	var absVal = Math.abs(val);
	var intValue = Math.floor(absVal);
	var dec = absVal - intValue;
	var i = intValue;
	dec *= 60;
//    var s = i + "°" + dec.toFixed(2) + "'";
//    var s = i + String.fromCharCode(176) + dec.toFixed(2) + "'";
	var s = "";
	if (val < 0) {
		s += (ns_ew === 'NS' ? 'S' : 'W');
	} else {
		s += (ns_ew === 'NS' ? 'N' : 'E');
	}
	s += " ";
	var sep = " ";
	if (withDeg === true) {
		sep = "°";
	}
//    s += i + "\"" + dec.toFixed(2) + "'";
	s += i + sep + dec.toFixed(2) + "'";
	return s;
};
