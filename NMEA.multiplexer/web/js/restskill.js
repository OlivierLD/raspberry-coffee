$(document).ready(function() {
    // Nothing.
});

var errManager = {
  display: alert
};

var RESTPayload = {};
var storedHistory = "";
var storedHistoryOut = "";

var getDeferred = function(url, timeout, verb, happyCode, data) {
    var deferred = $.Deferred(),  // a jQuery deferred
        url = url,
        xhr = new XMLHttpRequest(),
        TIMEOUT = timeout;

    var req = verb + " " + url;
    if (data !== undefined) {
        req += ("\n" + JSON.stringify(data, null, 2));
    }
    storedHistoryOut += ((storedHistoryOut.length > 0 ? "\n" : "") + req);
    displayRawDataOut();

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

}

var getSerialPorts = function() {
    return getDeferred('/serial-ports', 10000, 'GET', 200);
};

var getChannels = function() {
    return getDeferred('/channels', 10000, 'GET', 200);
};

var getForwarders = function() {
    return getDeferred('/forwarders', 10000, 'GET', 200);
};

var getComputers = function() {
    return getDeferred('/computers', 10000, 'GET', 200);
};

var addForwarder = function(forwarder) {
    return getDeferred('/forwarders', 10000, 'POST', 200, forwarder);
};

var addChannel = function(channel) {
    return getDeferred('/channels', 10000, 'POST', 200, channel);
};

var addComputer = function(computer) {
    return getDeferred('/computers', 10000, 'POST', 200, computer);
};

var updateChannel = function(channel) {
    return getDeferred('/channels/' + channel.type, 10000, 'PUT', 200, channel);
};

var updateComputer = function(computer) {
    return getDeferred('/computers/' + computer.type, 10000, 'PUT', 200, computer);
};

var updateMuxVerbose = function(value) {
    return getDeferred('/mux-verbose/' + value, 10000, 'PUT', 200);
};

var resetDataCache = function() {
    return getDeferred('/cache', 10000, 'DELETE', 204);
};

var deleteForwarder = function(forwarder) {
    return getDeferred('/forwarders/' + forwarder.type, 10000, 'DELETE', 204, forwarder);
};

var deleteComputer = function(computer) {
    return getDeferred('/computers/' + computer.type, 10000, 'DELETE', 204, computer);
};

var deleteChannel = function(channel) {
    return getDeferred('/channels/' + channel.type, 10000, 'DELETE', 204, channel);
};

var serialPortList = function() {
  var getData = getSerialPorts();
  getData.done(function(value) {
    console.log("Done:", value);
    var json = JSON.parse(value);
    setRESTPayload(json);
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
      errManager.display("Failed to get serial ports list..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
  });
};

var buildList = function(arr) {
    var str = (arr !== undefined) ? arr.toString() : "";
    return str;
};

var setRESTPayload = function(payload) {
  RESTPayload = payload;
  if (showRESTData) {
      displayRawData();
  }
};

var displayRawData = function() {
    var stringified = JSON.stringify(RESTPayload, null, 2);
    storedHistory += ((storedHistory.length > 0 ? "\n" : "") + stringified);
    $("#raw-data").html('<pre>' + storedHistory + '</pre>');
    $("#raw-data").scrollTop($("#raw-data")[0].scrollHeight);
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
    $("#raw-data-out").html("");
};

var channelList = function() {
    var getData = getChannels();
    getData.done(function(value) {
        console.log("Done:", value);
        var json = JSON.parse(value);
        setRESTPayload(json);
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
        errManager.display("Failed to get channels list..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

var forwarderList = function() {
    var getData = getForwarders();
    getData.done(function(value) {
        console.log("Done:", value);
        var json = JSON.parse(value);
        setRESTPayload(json);
        var html = "<h5>Writes to</h5>" +
            "<table>";
        html += "<tr><th>Type</th><th>Parameters</th></th></tr>"
        for (var i=0; i<json.length; i++) {
            var type = json[i].type;
            switch (type) {
                case 'file':
                    html += ("<tr><td><b>file</b></td><td>" + json[i].log + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
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
        errManager.display("Failed to get forwarders list..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

var computerList = function() {
    var getData = getComputers();
    getData.done(function(value) {
        console.log("Done:", value);
        var json = JSON.parse(value);
        setRESTPayload(json);
        var html = "<h5>Computes and writes</h5>" +
            "<table>";
        html += "<tr><th>Type</th><th>Parameters</th><th>verb.</th></tr>"
        for (var i=0; i<json.length; i++) {
            var type = json[i].type;
            switch (type) {
                case 'tw-current':
                    html += ("<tr><td valign='top'><b>tw-current</b></td><td valign='top'>Prefix: " + json[i].prefix + "<br>Timebuffer length: " + json[i].timeBufferLength.toLocaleString() + " ms.</td><td valign='top' align='center'><input type='checkbox' onchange='manageComputerVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose === true ? " checked" : "") + "></td><td valign='top'><button onclick='removeComputer(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                default:
                    html += ("<tr><td valign='top'><b><i>" + type + "</i></b></td><td valign='top'>" + json[i].cls + "</td><td valign='top' align='center'><input type='checkbox' onchange='manageComputerVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose === true ? " checked" : "") + "></td><td valign='top'><button onclick='removeComputer(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
            }
        }
        html += "</table>";
        $("#lists").html(html);
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
        errManager.display("Failed to get nmea.computers list..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

var createChannel = function(channel) {
  var postData = addChannel(channel);
    postData.done(function(value) {
        console.log("Done:", value);
        setRESTPayload(value);
        channelList(); // refetch
    });
    postData.fail(function(error, errmess) {
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
    var postData = addForwarder(forwarder);
    postData.done(function(value) {
        setRESTPayload(value);
        console.log("Done:", value);
        forwarderList(); // refetch
    });
    postData.fail(function(error, errmess) {
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
    var postData = addComputer(computer);
    postData.done(function(value) {
        setRESTPayload(value);
        console.log("Done:", value);
        computerList(); // refetch
    });
    postData.fail(function(error, errmess) {
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
    var deleteData = deleteChannel(channel);
    deleteData.done(function(value) {
        setRESTPayload(value);
        console.log("Done:", value);
        channelList(); // refetch
    });
    deleteData.fail(function(error, errmess) {
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
    var deleteData = deleteForwarder(channel);
    deleteData.done(function(value) {
        setRESTPayload(value);
        console.log("Done:", value);
        forwarderList(); // refetch
    });
    deleteData.fail(function(error, errmess) {
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
    var deleteData = deleteComputer(computer);
    deleteData.done(function(value) {
        setRESTPayload(value);
        console.log("Done:", value);
        computerList(); // refetch
    });
    deleteData.fail(function(error, errmess) {
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
    var putData = updateChannel(channel);
    putData.done(function(value) {
        setRESTPayload(value);
        console.log("Done:", value);
        channelList(); // refetch
    });
    putData.fail(function(error, errmess) {
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
    var putData = updateComputer(computer);
    putData.done(function(value) {
        setRESTPayload(value);
        console.log("Done:", value);
        computerList(); // refetch
    });
    putData.fail(function(error, errmess) {
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
    var updateMux = updateMuxVerbose(cb.checked ? 'on' : 'off');
    updateMux.done(function(value) {
        RESTPayload = value;
        console.log("Done:", value);
    });
    updateMux.fail(function(error, errmess) {
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
    var reset = resetDataCache();
    reset.done(function(value) {
        RESTPayload = value;
        console.log("Done:", value);
    });
    reset.fail(function(error, errmess) {
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
