var int;
$(document).ready(function() {
  int = setInterval(getImageBase64String, 1000); // Refresh every second.
});

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

var getImageBase64String = function() {
  var k = $("#a-key").val();

  if (k.trim().length > 0) {
    $("#mess").text('');
    $("#data").css('display', 'inline');

    setTimeout(function() {
      $('body').css('cursor', 'progress');
    }, 1);

    // Produce data, the promise
    var fetchData = getData('picture');
    fetchData.done(function(value) {
      //  console.log("Done :" + value); // Raw data
      // Display it...
      var img = JSON.parse(value).last_value;
   // <img src=”data:image/png;base64,iVBORw0KGgoAAAANS… (see source for full base64 encoded image) …8bgAAAAASUVORK5CYII=”>
      $("#img-value").attr('src', ("data:image/png;base64," + img));
      clearInterval(int);
      setTimeout(function() {
        $('body').css('cursor', 'auto');
      }, 1);
    });

    // Errors etc
    fetchData.fail(function(error) {
      alert('Data request failed (timeout?), try again later.\n' + (error !== undefined ? error : ''));
    });
  } else {
    $("#mess").text('Please enter your Adafruit-IO key in the field above');
    $("#data").css('display', 'none');
  }
};
