$(document).ready(function() {
  setInterval(go, 1000); // Refresh every second.
});

var getData = function() {
  var deferred = $.Deferred(),  // a jQuery deferred
      url = 'https://io.adafruit.com/api/feeds/onoff',
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

var go = function() {
  var k = $("#a-key").val();

  if (k.trim().length > 0) {
    $("#mess").text('');

    setTimeout(function() {
      $('body').css('cursor', 'progress');
    }, 1);

    // Produce data, the promise
    var fetchData = getData();
    fetchData.done(function(value) {
  //  console.log("Done :" + value); // Raw data
      // Display it...
      var status = JSON.parse(value).last_value;
      $("#last-value").text(new Date() + ':  ' + status);
      setTimeout(function() {
        $('body').css('cursor', 'auto');
      }, 1);
    });

    // Errors etc
    fetchData.fail(function(error) {
      alert('Data request failed (timeout?), try again later.\n' + (error !== undefined ? error : ''));
    });
  } else {
    $("#mess").text('Please enter your Adafruit IO key in the field above');
  }
};
