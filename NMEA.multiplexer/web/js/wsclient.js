"use strict";

var connection;

(function () {
    var ws = window.WebSocket || window.MozWebSocket;
    if (!ws) {
        displayMessage('Sorry, but your browser does not support WebSockets.');
        return;
    }

    // open connection
    var rootUri = "ws://" + (document.location.hostname === "" ? "localhost" : document.location.hostname) + ":" +
        (document.location.port === "" ? "8080" : document.location.port);
    console.log(rootUri);
    connection = new WebSocket(rootUri); // 'ws://localhost:9876');

    connection.onopen = function () {
        displayMessage('Connected.')
    };

    connection.onerror = function (error) {
        // just in there were some problems with connection...
        displayMessage('Sorry, but there is some problem with your connection or the server is down.');
    };

    // most important part - incoming messages
    connection.onmessage = function (message) {
//      console.log('onmessage:' + message);
        if (filters.value.length > 0) {
            var elements = filters.value.split(",");
            var doDisplay = false;
            var dontDisplay = false;
            for (var i=0; i<elements.length; i++) {
                var filter = elements[i];
                if (filter.startsWith('~')) { // Negation, like ~RMC : Don't display RMC
                    var _filter = filter.substr(1);
                    if (_filter.trim().length > 0) {
                        if (message.data.indexOf(_filter.trim()) > 0) {
                            dontDisplay = true;
                            console.log(filter + " (" + _filter.trim() + ") => Do NOT display " + message.data);
                            break;
                        }
                    }
                } else {
                    if (message.data.indexOf(filter.trim()) > 0) {
                        doDisplay = true;
                        console.log(filter + " => DO display " + message.data);
                        break;
                    }
                }
            }
            if (doDisplay && !dontDisplay) {
                displayMessage(message.data);
            }
        } else {
            displayMessage('Unfiltered: ' + message.data);
        }
    };

    /**
     * This method is optional. If the server wasn't able to respond to the
     * in 3 seconds then show some error message to notify the user that
     * something is wrong.
     */
    setInterval(function() {
        if (connection.readyState !== 1) {
            displayMessage('Unable to communicate with the WebSocket server. Try again.');
        }
    }, 3000); // Ping
})();

var displayMessage = function(mess) {
    var messList = statusFld.innerHTML;
    if (messList !== undefined) {
        var lines = messList.split('<br>');
        while (lines.length > 10) { // Limit number of messages to 10.
            lines.shift();
        }
        messList = '';
        for (var i=0; i<lines.length; i++) {
            messList += (lines[i] + '<br>');
        }
    }
    messList = (((messList !== undefined && messList.length) > 0 ? messList + (messList.endsWith('<br>') ? '' : '<br>') : '') + mess);
    statusFld.innerHTML = messList;
    statusFld.scrollTop = statusFld.scrollHeight; // Scroll down
};

var resetStatus = function() {
    statusFld.innerHTML = "";
};
