// App specific code

let nbMessReceived = 0;

let init = () => {
  ws.onopen = () => {
    try { 
      let text;
      try  { 
        text = 'Message:';
      } catch (err) {
        text = '<small>Connected</small>';
      }
      promptFld.innerHTML = text; 
      if (nbMessReceived === 0) {
        statusFld.innerHTML = "";
      }
      statusFld.innerHTML += ((nbMessReceived === 0?"":"<br>") + "<small>" + 
                              (new Date()).format("d-M-Y H:i:s._ Z") + "</small>:<font color='blue'>" + ' Connection opened.' + "</font>");
      statusFld.scrollTop = statusFld.scrollHeight;
      nbMessReceived++;
    } catch (err) {}
  };
  ws.onerror = (error) => {
    if (nbMessReceived === 0) {
      statusFld.innerHTML = "";
    }
    statusFld.innerHTML += ((nbMessReceived === 0?"":"<br>") + "<small>" + 
                            (new Date()).format("d-M-Y H:i:s._ Z") + "</small>:<font color='red'>" + error.err + "</font>");
    statusFld.scrollTop = statusFld.scrollHeight;
    nbMessReceived++;
  };
  ws.onmessage = (message) => { // message/event
    let json = {};
    if (typeof(message.data) === 'string') {
      try {
        json = JSON.parse(message.data); 
      } catch (e) {
        console.log(e);
        console.log('This doesn\'t look like a valid JSON: ' + message.data);
      }
    }
  //console.log("Split: type=" + json.type + ", data=" + json.data);
    if (json.type !== undefined && json.type === 'message' && typeof(json.data.text) === 'string') { // it's a single message, text
      let dt = new Date();
     /**
       * Add message to the chat window
       */
      let existing = contentFld.innerHTML; // Content already there
      let toDisplay = "";
      try { toDisplay = json.data.text; }
      catch (err) {}
      contentFld.innerHTML = existing + 
           ('At ' +
           + (dt.getHours() < 10 ? '0' + dt.getHours() : dt.getHours()) + ':'
           + (dt.getMinutes() < 10 ? '0' + dt.getMinutes() : dt.getMinutes())
           + ': ' + toDisplay + '<br>');
      contentFld.scrollTop = contentFld.scrollHeight;
    } else {  // Unexpected
      let payload = {};
    }
  };
  ws.onclose = () => {
    if (nbMessReceived === 0) {
      statusFld.innerHTML = "";
    }
    statusFld.innerHTML += ((nbMessReceived === 0?"":"<br>") + "<small>" + 
                            (new Date()).format("d-M-Y H:i:s._ Z") + "</small>:<font color='blue'>" + ' Connection closed' + "</font>");
    promptFld.innerHTML = 'Connection closed'; 
  };
};

let send = (mess) => {
  ws.send(mess);
};

let getClass = (obj) => {
  if (obj && typeof obj === 'object' &&
      Object.prototype.toString.call(obj) !== '[object Array]' &&
      obj.constructor)  {
    let arr = obj.constructor.toString().match(/function\s*(\w+)/);
    if (arr && arr.length === 2) {
      return arr[1];
    }
  }
  return false;
};

// Date formatting
// Provide month names
Date.prototype.getMonthName = function() {
  let month_names = [
                      'January',
                      'February',
                      'March',
                      'April',
                      'May',
                      'June',
                      'July',
                      'August',
                      'September',
                      'October',
                      'November',
                      'December'
                  ];

  return month_names[this.getMonth()];
};

// Provide month abbreviation
Date.prototype.getMonthAbbr = function() {
  let month_abbrs = [
                      'Jan',
                      'Feb',
                      'Mar',
                      'Apr',
                      'May',
                      'Jun',
                      'Jul',
                      'Aug',
                      'Sep',
                      'Oct',
                      'Nov',
                      'Dec'
                  ];

  return month_abbrs[this.getMonth()];
};

// Provide full day of week name
Date.prototype.getDayFull = function() {
  let days_full = [
                    'Sunday',
                    'Monday',
                    'Tuesday',
                    'Wednesday',
                    'Thursday',
                    'Friday',
                    'Saturday'
                  ];
  return days_full[this.getDay()];
};

// Provide full day of week name
Date.prototype.getDayAbbr = function() {
  let days_abbr = [
                    'Sun',
                    'Mon',
                    'Tue',
                    'Wed',
                    'Thur',
                    'Fri',
                    'Sat'
                  ];
  return days_abbr[this.getDay()];
};

// Provide the day of year 1-365
Date.prototype.getDayOfYear = function() {
  let onejan = new Date(this.getFullYear(),0,1);
  return Math.ceil((this - onejan) / 86400000);
};

// Provide the day suffix (st,nd,rd,th)
Date.prototype.getDaySuffix = function() {
  let d = this.getDate();
  let sfx = ["th", "st", "nd", "rd"];
  let val = d % 100;

  return (sfx[(val-20)%10] || sfx[val] || sfx[0]);
};

// Provide Week of Year
Date.prototype.getWeekOfYear = function() {
  let onejan = new Date(this.getFullYear(),0,1);
  return Math.ceil((((this - onejan) / 86400000) + onejan.getDay()+1)/7);
};

// Provide if it is a leap year or not
Date.prototype.isLeapYear = function() {
  let yr = this.getFullYear();
  if ((parseInt(yr) % 4) === 0) {
    if (parseInt(yr) % 100 === 0) {
      if (parseInt(yr) % 400 !== 0) {
        return false;
      }
      if (parseInt(yr) % 400 === 0) {
        return true;
      }
    }
    if (parseInt(yr) % 100 !== 0) {
      return true;
    }
  }
  if ((parseInt(yr) % 4) !== 0) {
    return false;
  }
};

// Provide Number of Days in a given month
Date.prototype.getMonthDayCount = function() {
  let month_day_counts = [
                            31,
                            this.isLeapYear() ? 29 : 28,
                            31,
                            30,
                            31,
                            30,
                            31,
                            31,
                            30,
                            31,
                            30,
                            31
                         ];

  return month_day_counts[this.getMonth()];
}; 

// format provided date into this.format format
Date.prototype.format = function(dateFormat) {
  // break apart format string into array of characters
  dateFormat = dateFormat.split("");

  let date = this.getDate(),
      month = this.getMonth(),
      hours = this.getHours(),
      minutes = this.getMinutes(),
      seconds = this.getSeconds(),
      milli = this.getTime() % 1000,
      tzOffset = - (this.getTimezoneOffset() / 60);

  let lpad = function(s, w, len) {
    let str = s;
    while (str.length < len)
      str = w + str;
    return str;
  };

  // get all date properties ( based on PHP date object functionality )
  let date_props = {
    d: date < 10 ? '0'+date : date,
    D: this.getDayAbbr(),
    j: this.getDate(),
    l: this.getDayFull(),
    S: this.getDaySuffix(),
    w: this.getDay(),
    z: this.getDayOfYear(),
    W: this.getWeekOfYear(),
    F: this.getMonthName(),
    m: month < 10 ? '0'+(month+1) : month+1,
    M: this.getMonthAbbr(),
    n: month+1,
    t: this.getMonthDayCount(),
    L: this.isLeapYear() ? '1' : '0',
    Y: this.getFullYear(),
    y: this.getFullYear()+''.substring(2,4),
    a: hours > 12 ? 'pm' : 'am',
    A: hours > 12 ? 'PM' : 'AM',
    g: hours % 12 > 0 ? hours % 12 : 12,
    G: hours > 0 ? hours : "12",
    h: hours % 12 > 0 ? hours % 12 : 12,
    H: hours < 10 ? '0' + hours : hours,
    i: minutes < 10 ? '0' + minutes : minutes,
    s: seconds < 10 ? '0' + seconds : seconds,
    Z: "UTC" + (tzOffset > 0 ?"+":"") + tzOffset,
    _: lpad(milli, '0', 3)
  };

  // loop through format array of characters and add matching data else add the format character (:,/, etc.)
  let date_string = "";
  for (let i=0; i<dateFormat.length; i++) {
    let f = dateFormat[i];
    if (f.match(/[a-zA-Z|_]/g)) {
      date_string += date_props[f] ? date_props[f] : '';
    } else  {
      date_string += f;
    }
  }
  return date_string;
};
