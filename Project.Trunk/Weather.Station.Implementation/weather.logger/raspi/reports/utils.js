var parseSQLDate = function(strDate) {
  var date;
  var re = new RegExp("(\\d{4})-(\\d{2})-(\\d{2})\\s(\\d{2}):(\\d{2}):(\\d{2})");
  var match = strDate.match(re);
  if (match.length === 7) {
    var year  = parseInt(match[1]);
    var month = parseInt(match[2]) - 1;
    var day   = parseInt(match[3]);
    var hour  = parseInt(match[4]);
    var min   = parseInt(match[5]);
    var sec   = parseInt(match[6]);
    date = new Date(year, month, day, hour, min, sec, 0);
  }
  return date;
};

var reformatDate = function(utcDate, fmt) {
  if (fmt === undefined) {
      fmt = "D d-M-Y H:i";
  }
  // 07-03 00:00
  var dateRegExpr = new RegExp("(\\d{2})-(\\d{2})\\s(\\d{2}):(\\d{2})");
  var matches = dateRegExpr.exec(utcDate);
  if (matches !== null) {
    var month   = matches[1];
    var day     = matches[2];
    var hours   = matches[3];
    var minutes = matches[4];
  }
  var date = new Date();
  date.setMonth(parseInt(month - 1));
  date.setDate(parseInt(day));
  date.setHours(parseInt(hours));
  date.setMinutes(parseInt(minutes));
  date.setSeconds(0);
 // console.log(date.toString());
  var time = date.getTime();
  var offset = 0; // parseInt(document.getElementById("tz").value);
  offset *= (60 * 60 * 1000);
  time += offset;
  date = new Date(time);
// console.log("becomes: " + date.toString());
  return date.format(fmt);
};

/*
var d = parseSQLDate("2014-12-14 20:13:04");
console.log(d);
*/
