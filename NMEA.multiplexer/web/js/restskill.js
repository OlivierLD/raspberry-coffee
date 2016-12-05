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
        var html = "<ul>";
        for (var i=0; i<json.length; i++) {
          var type = json[i].type;
          switch (type) {
              case 'file':
                html += ("<li><b>file</b>: " + json[i].file + " <small><a href='' onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>Delete</a></small></li>");
                break;
              case 'serial':
                  html += ("<li><b>serial</b>: " + json[i].port + ":" + json[i].br + " <small><a href='' onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>Delete</a></small></li>");
                  break;
              case 'tcp':
                  html += ("<li><b>tcp</b>: Port " + json[i].port + " <small><a href='' onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>Delete</a></small></li>");
                  break;
              case 'ws':
                  html += ("<li><b>ws</b>: " + json[i].wsUri + " <small><a href='' onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>Delete</a></small></li>");
                  break;
              case 'rnd':
                  html += ("<li><b>rnd</b> <small><a href='' onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>Delete</a></small></li>");
                  break;
              case 'bme280':
                  html += ("<li><b>bme280</b> <small><a href='' onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>Delete</a></small></li>");
                  break;
              case 'htu21df':
                  html += ("<li><b>htu21df</b> <small><a href='' onclick='removeChannel(" + JSON.stringify(json[i]) + ");'>Delete</a></small></li>");
                  break;
              default:
                break;
          }
        }
        html += "</ul>";
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
        var html = "<ul>";
        for (var i=0; i<json.length; i++) {
            var type = json[i].type;
            switch (type) {
                case 'file':
                    html += ("<li><b>file</b>: " + json[i].log + " <small><a href='' onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>Delete</a></small></li>");
                    break;
                case 'serial':
                    html += ("<li><b>serial</b>: " + json[i].port + ":" + json[i].br + " <small><a href='' onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>Delete</a></small></li>");
                    break;
                case 'tcp':
                    html += ("<li><b>tcp</b>: Port " + json[i].port + " <small><a href='' onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>Delete</a></small></li>");
                    break;
                case 'ws':
                    html += ("<li><b>ws</b>: " + json[i].wsUri + " <small><a href='' onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>Delete</a></small></li>");
                    break;
                case 'console':
                    html += ("<li><b>console</b> <small><a href='' onclick='removeForwarder(" + JSON.stringify(json[i]) + ");'>Delete</a></small></li>");
                    break;
                default:
                    break;
            }
        }
        html += "</ul>";
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
    });
    postData.fail(function(error) {
        alert("Failed to create channel..." + (error !== undefined ? error : ''));
    });
};

var createForwarder = function(channel) {
    var postData = addForwarder(channel);
    postData.done(function(value) {
        console.log("Done:", value);
    });
    postData.fail(function(error) {
        alert("Failed to create channel..." + (error !== undefined ? error : ''));
    });
};

var removeChannel = function(channel) {
    var deleteData = deleteChannel(channel);
    deleteData.done(function(value) {
        console.log("Done:", value);
    });
    deleteData.fail(function(error) {
        alert("Failed to delete channel..." + (error !== undefined ? error : ''));
    });
};

var removeForwarder = function(channel) {
    var deleteData = deleteForwarder(channel);
    deleteData.done(function(value) {
        console.log("Done:", value);
    });
    deleteData.fail(function(error) {
        alert("Failed to delete channel..." + (error !== undefined ? error : ''));
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
