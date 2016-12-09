$(document).ready(function() {


});

var getSerialPorts = function() {
  var deferred = $.Deferred(),  // a jQuery deferred
      url = '/serial-ports',
      xhr = new XMLHttpRequest(),
      TIMEOUT = 10000;

  xhr.open('GET', url, true);
  xhr.setRequestHeader("Content-type", "application/json");
  xhr.send();

  var requestTimer = setTimeout(function() {
    xhr.abort();
    deferred.reject();
  }, TIMEOUT);

  xhr.onload = function() {
    clearTimeout(requestTimer);
    if (xhr.status === 200) {
      deferred.resolve(xhr.response);
    } else {
      deferred.reject();
    }
  };
  return deferred.promise();
};

var getChannels = function() {
    var deferred = $.Deferred(),  // a jQuery deferred
        url = '/channels',
        xhr = new XMLHttpRequest(),
        TIMEOUT = 10000;

    xhr.open('GET', url, true);
    xhr.setRequestHeader("Content-type", "application/json");
    xhr.send();

    var requestTimer = setTimeout(function() {
        xhr.abort();
        deferred.reject();
    }, TIMEOUT);

    xhr.onload = function() {
        clearTimeout(requestTimer);
        if (xhr.status === 200) {
            deferred.resolve(xhr.response);
        } else {
            deferred.reject();
        }
    };
    return deferred.promise();
};

var getForwarders = function() {
    var deferred = $.Deferred(),  // a jQuery deferred
        url = '/forwarders',
        xhr = new XMLHttpRequest(),
        TIMEOUT = 10000;

    xhr.open('GET', url, true);
    xhr.setRequestHeader("Content-type", "application/json");
    xhr.send();

    var requestTimer = setTimeout(function() {
        xhr.abort();
        deferred.reject();
    }, TIMEOUT);

    xhr.onload = function() {
        clearTimeout(requestTimer);
        if (xhr.status === 200) {
            deferred.resolve(xhr.response);
        } else {
            deferred.reject();
        }
    };
    return deferred.promise();
};

var addForwarder = function(channel) {
    var deferred = $.Deferred(),  // a jQuery deferred
        url = '/forwarders',
        xhr = new XMLHttpRequest(),
        TIMEOUT = 10000;

    xhr.open('POST', url, true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    xhr.send(JSON.stringify(channel));

    var requestTimer = setTimeout(function() {
        xhr.abort();
        deferred.reject();
    }, TIMEOUT);

    xhr.onload = function() {
        clearTimeout(requestTimer);
        if (xhr.status === 200) {
            deferred.resolve(xhr.response);
        } else {
            deferred.reject();
        }
    };
    return deferred.promise();
};

var addChannel = function(channel) {
  var deferred = $.Deferred(),  // a jQuery deferred
      url = '/channels',
      xhr = new XMLHttpRequest(),
      TIMEOUT = 10000;

  xhr.open('POST', url, true);
  xhr.setRequestHeader('Content-Type', 'application/json');

  xhr.send(JSON.stringify(channel));

  var requestTimer = setTimeout(function() {
    xhr.abort();
    deferred.reject();
  }, TIMEOUT);

  xhr.onload = function() {
    clearTimeout(requestTimer);
    if (xhr.status === 200) {
      deferred.resolve(xhr.response);
    } else {
      deferred.reject();
    }
  };
  return deferred.promise();
};

var updateChannel = function(channel) {
    var deferred = $.Deferred(),  // a jQuery deferred
        url = '/channels',
        xhr = new XMLHttpRequest(),
        TIMEOUT = 10000;

    xhr.open('PUT', url, true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    xhr.send(JSON.stringify(channel));

    var requestTimer = setTimeout(function() {
        xhr.abort();
        deferred.reject();
    }, TIMEOUT);

    xhr.onload = function() {
        clearTimeout(requestTimer);
        if (xhr.status === 200) {
            deferred.resolve(xhr.response);
        } else {
            deferred.reject();
        }
    };
    return deferred.promise();
};

var updateMuxVerbose = function(value) {
    var deferred = $.Deferred(),  // a jQuery deferred
        url = '/mux/verbose/' + value,
        xhr = new XMLHttpRequest(),
        TIMEOUT = 10000;

    xhr.open('PUT', url, true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    xhr.send(); // No payload

    var requestTimer = setTimeout(function() {
        xhr.abort();
        deferred.reject();
    }, TIMEOUT);

    xhr.onload = function() {
        clearTimeout(requestTimer);
        if (xhr.status === 200) {
            deferred.resolve(xhr.response);
        } else {
            deferred.reject();
        }
    };
    return deferred.promise();
};

var deleteForwarder = function(channel) {
    var deferred = $.Deferred(),  // a jQuery deferred
        url = '/forwarders/' + channel.type,
        xhr = new XMLHttpRequest(),
        TIMEOUT = 10000;

    xhr.open('DELETE', url, true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    xhr.send(JSON.stringify(channel));

    var requestTimer = setTimeout(function() {
        xhr.abort();
        deferred.reject();
    }, TIMEOUT);

    xhr.onload = function() {
        clearTimeout(requestTimer);
        if (xhr.status === 204) {
            deferred.resolve(xhr.response);
        } else {
            deferred.reject();
        }
    };
    return deferred.promise();
};

var deleteChannel = function(channel) {
    var deferred = $.Deferred(),  // a jQuery deferred
        url = '/channels/' + channel.type,
        xhr = new XMLHttpRequest(),
        TIMEOUT = 10000;

    xhr.open('DELETE', url, true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    xhr.send(JSON.stringify(channel));

    var requestTimer = setTimeout(function() {
        xhr.abort();
        deferred.reject();
    }, TIMEOUT);

    xhr.onload = function() {
        clearTimeout(requestTimer);
        if (xhr.status === 204) {
            deferred.resolve(xhr.response);
        } else {
            deferred.reject();
        }
    };
    return deferred.promise();
};

var serialPortList = function() {
  var getData = getSerialPorts();
  getData.done(function(value) {
    console.log("Done:", value);
    var json = JSON.parse(value);
    var html = "<ul>";
    for (var i=0; i<json.length; i++) {
      html += ("<li>" + json[i] + "</li>");
    }
    html += "</ul>";
    $("#lists").html(html);
  });
  getData.fail(function(error) {
    alert("Failed to set the value..." + (error !== undefined ? error : ''));
  });
};

var channelList = function() {
    var getData = getChannels();
    getData.done(function(value) {
        console.log("Done:", value);
        var json = JSON.parse(value);
        var html = "<h5>Reads from</h5>" +
            "<table>";
        html += "<tr><th>Type</th><th>Parameters</th><th>verb.</th></tr>"
        for (var i=0; i<json.length; i++) {
          var type = json[i].type;
          switch (type) {
              case 'file':
                html += ("<tr><td><b>file</b></td><td>" + json[i].file + "</td><td><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                break;
              case 'serial':
                  html += ("<tr><td><b>serial</b></td><td>" + json[i].port + ":" + json[i].br + "</td><td><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                  break;
              case 'tcp':
                  html += ("<tr><td><b>tcp</b></td><td>" + json[i].hostname + ":" + json[i].port + "</td><td><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                  break;
              case 'ws':
                  html += ("<tr><td><b>ws</b></td><td> " + json[i].wsUri + "</td><td><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                  break;
              case 'rnd':
                  html += ("<tr><td><b>rnd</b></td><td></td><td><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                  break;
              case 'bme280':
                  html += ("<tr><td><b>bme280</b></td><td></td><td><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                  break;
              case 'htu21df':
                  html += ("<tr><td><b>htu21df</b></td><td></td><td><input type='checkbox' onchange='manageChannelVerbose(this, " + JSON.stringify(json[i]) + ");'" + (json[i].verbose ? " checked" : "") + "></td><td><button onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                  break;
              default:
                break;
          }
        }
        html += "</table>";
        $("#lists").html(html);
    });
    getData.fail(function(error) {
        alert("Failed to set the value..." + (error !== undefined ? error : ''));
    });
};

var forwarderList = function() {
    var getData = getForwarders();
    getData.done(function(value) {
        console.log("Done:", value);
        var json = JSON.parse(value);
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
                case 'ws':
                    html += ("<tr><td><b>ws</b></td><td>" + json[i].wsUri + "</td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                case 'console':
                    html += ("<tr><td><b>console</b></td><td></td><td><button onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>remove</button></td></tr>");
                    break;
                default:
                    break;
            }
        }
        html += "</table>";
        $("#lists").html(html);
    });
    getData.fail(function(error) {
        alert("Failed to set the value..." + (error !== undefined ? error : ''));
    });
};

var createChannel = function(channel) {
  var postData = addChannel(channel);
    postData.done(function(value) {
        console.log("Done:", value);
        channelList(); // refetch
    });
    postData.fail(function(error) {
        alert("Failed to create channel..." + (error !== undefined ? error : ''));
    });
};

var createForwarder = function(channel) {
    var postData = addForwarder(channel);
    postData.done(function(value) {
        console.log("Done:", value);
        forwarderList(); // refetch
    });
    postData.fail(function(error) {
        alert("Failed to create channel..." + (error !== undefined ? error : ''));
    });
};

var removeChannel = function(channel) {
    var deleteData = deleteChannel(channel);
    deleteData.done(function(value) {
        console.log("Done:", value);
        channelList(); // refetch
    });
    deleteData.fail(function(error) {
        alert("Failed to delete channel..." + (error !== undefined ? error : ''));
    });
};

var removeForwarder = function(channel) {
    var deleteData = deleteForwarder(channel);
    deleteData.done(function(value) {
        console.log("Done:", value);
        forwarderList(); // refetch
    });
    deleteData.fail(function(error) {
        alert("Failed to delete channel..." + (error !== undefined ? error : ''));
    });
};

var changeChannel = function(channel) {
    var putData = updateChannel(channel);
    putData.done(function(value) {
        console.log("Done:", value);
        channelList(); // refetch
    });
    putData.fail(function(error) {
        alert("Failed to update channel..." + (error !== undefined ? error : ''));
    });
};

var manageChannelVerbose = function(cb, channel) {
    console.log('Clicked checkbox on', channel, ' checked:', cb.checked);
    // PUT on the channel.
    channel.verbose = cb.checked;
    changeChannel(channel);
};

var manageMuxVerbose = function(cb) {
    var updateMux = updateMuxVerbose(cb.checked ? 'on' : 'off');
    updateMux.done(function(value) {
        console.log("Done:", value);
    });
    updateMux.fail(function(error) {
        alert("Failed to update multiplexer..." + (error !== undefined ? error : ''));
    });
};

var showAddChannel = function() {
    $("#add-forwarder").css('display', 'none');
    $("#add-channel").css('display', 'inline');
};

var showAddForwarder = function() {
    $("#add-forwarder").css('display', 'inline');
    $("#add-channel").css('display', 'none');
};
