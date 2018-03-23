/* global $, FEED_NAME */
var int;
$(document).ready(function() {
  int = setInterval(getImageBase64String, 1000); // Refresh every second.
});

var getQSPrm = function(prm) {
	var value;
	var loc = document.location.toString();
	if (loc.indexOf("?") > -1) {
		var qs = loc.substring(loc.indexOf("?") + 1);
		var prms = qs.split('&');
		for (var i=0; i<prms.length; i++) {
			var nv = prms[i].split('=');
			if (nv.length === 2) {
				if (nv[0] === prm) {
					return nv[1];
				}
			}
		}
	}
	return value;
};

var getData = function(feed) {

  if (feed === undefined) {
    feed = FEED_NAME;
  }

  var deferred = $.Deferred(),  // a jQuery deferred
      url = 'https://io.adafruit.com/api/feeds/' + feed,
      xhr = new XMLHttpRequest(),
      TIMEOUT = 10000;

  xhr.open('GET', url, true);
  var key = $("#a-key").val();
  xhr.setRequestHeader("X-AIO-Key", key);

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

var loopRefresh = false;
var refreshInt;
var refreshImg = function() {
  if (loopRefresh === true && refreshInt !== undefined) {
    clearInterval(refreshInt);
    loopRefresh = false;
    // Reset button label
    $("#button").text('Refresh image');
    $("#ref-rate").removeAttr('disabled');
  } else {
    var interval = parseInt($("#ref-rate").val());
    if (interval !== undefined && !isNaN(interval) && interval > 0) {
      $("#button").text('Stop refreshing');
      $("#ref-rate").attr('disabled', 'true');
      refreshInt = setInterval(getImageBase64String, 1000 * interval);
      loopRefresh = true;
    } else {
      getImageBase64String();
    }
  }
};

var MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
var durationToDate = function(duration) { // like "2018-03-23T13:59:25Z"
	var dateTime = duration.split("T");
	var ymd = dateTime[0].split("-");
//var hms = dateTime[1].split(":");
	return ymd[0] + ' ' + MONTHS[parseInt(ymd[1]) - 1] + ' ' + ymd[2] + ' at ' + dateTime[1];
};

var getImageBase64String = function() {
  var k = $("#a-key").val();

  if (k.trim().length > 0) {
    $("#mess").text('');
    $("#data").css('display', 'inline');

    setTimeout(function() {
      $('body').css('cursor', 'progress');
    }, 1);

    console.log("Refreshing image");
    $("#spinner").html("<img src='spinner.gif' width='24' height='24' style='vertical-align: middle;'>");

    console.log("Fading Out");
    $("#img-value").fadeOut("fast"); // Prm: slow, fast, or milliseconds. See http://www.w3schools.com/jquery/eff_fadeout.asp
    // Produce data, the promise
    var now = new Date().getMilliseconds();
    var fetchData = getData('picture');
    fetchData.done(function(value) {
      //  console.log("Done :" + value); // Raw data
      var elapsed = new Date().getMilliseconds() - now;
      // Display it...
	    var payload = JSON.parse(value);
      var img = payload.last_value;
      console.log("Image is in (%d ms)", elapsed);
      var updated = payload.updated_at; // "2018-03-23T13:59:25Z"
			$("#updated").text(durationToDate(updated));
   // <img src=”data:image/png;base64,iVBORw0KGgoAAAANS… (see source for full base64 encoded image) …8bgAAAAASUVORK5CYII=”>
   // $("#img-value").slideToggle("slow");
   // $("#img-value").slideUp();
      $("#img-value").attr('src', ("data:image/png;base64," + img));
   // $("#img-value").slideToggle("slow");
   // $("#img-value").slideDown();
      console.log("Fading In");
      $("#img-value").fadeIn();
      clearInterval(int);
      setTimeout(function() {
        $('body').css('cursor', 'auto');
        $("#spinner").html("");
      }, 1);
    });

    // Errors etc
    fetchData.fail(function(error) {
      $("#spinner").html("");
      alert('Data request failed (timeout?), try again later.\n' + (error !== undefined ? error : ''));
    });
  } else {
    $("#mess").text('Please enter your Adafruit-IO key in the field above');
    $("#data").css('display', 'none');
  }
};
