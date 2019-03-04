/*
 * @author Olivier Le Diouris
 */

// Date formatting

// Provide month names
Date.prototype.getMonthName = function () {
    const MONT_NAMES = [
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

    return MONT_NAMES[this.getMonth()];
};

// Provide month abbreviation
Date.prototype.getMonthAbbr = function () {
    const MONTH_ABBR = [
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

    return MONTH_ABBR[this.getMonth()];
};

// Provide full day of week name
Date.prototype.getDayFull = function () {
    const DAYS_FULL = [
        'Sunday',
        'Monday',
        'Tuesday',
        'Wednesday',
        'Thursday',
        'Friday',
        'Saturday'
    ];
    return DAYS_FULL[this.getDay()];
};

// Provide full day of week name
Date.prototype.getDayAbbr = function () {
    const DAYS_ABBR = [
        'Sun',
        'Mon',
        'Tue',
        'Wed',
        'Thur',
        'Fri',
        'Sat'
    ];
    return DAYS_ABBR[this.getDay()];
};

// Provide the day of year 1-365
Date.prototype.getDayOfYear = function () {
    let janFirst = new Date(this.getFullYear(), 0, 1);
    return Math.ceil((this - janFirst) / 86400000);
};

// Provide the day suffix (st,nd,rd,th)
Date.prototype.getDaySuffix = function () {
    let d = this.getDate();
    let sfx = ["th", "st", "nd", "rd"];
    let val = d % 100;

    return (sfx[(val - 20) % 10] || sfx[val] || sfx[0]);
};

// Provide Week of Year
Date.prototype.getWeekOfYear = function () {
    let janFirst = new Date(this.getFullYear(), 0, 1);
    return Math.ceil((((this - janFirst) / 86400000) + janFirst.getDay() + 1) / 7);
};

// Provide if it is a leap year or not
Date.prototype.isLeapYear = function () {
    let yr = this.getFullYear();
    if ((parseInt(yr, 10) % 4) === 0) {
        if (parseInt(yr, 10) % 100 === 0) {
            if (parseInt(yr, 10) % 400 !== 0) {
                return false;
            }
            if (parseInt(yr, 10) % 400 === 0) {
                return true;
            }
        }
        if (parseInt(yr, 10) % 100 !== 0) {
            return true;
        }
    }
    if ((parseInt(yr, 10) % 4) !== 0) {
        return false;
    }
};

// Provide Number of Days in a given month
Date.prototype.getMonthDayCount = function () {
    const MONTH_DAY_COUNT = [
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

    return MONTH_DAY_COUNT[this.getMonth()];
};

// format provided date into this.format format
Date.prototype.format = function (dateFormat) {
    // break apart format string into array of characters
    dateFormat = dateFormat.split("");

    let date = this.getDate(),
        month = this.getMonth(),
        hours = this.getHours(),
        minutes = this.getMinutes(),
        seconds = this.getSeconds(),
        milli = this.getTime() % 1000,
        tzName = this.toString().substring(this.toString().indexOf('(') + 1, this.toString().indexOf(')')),
        tzOffset = -(this.getTimezoneOffset() / 60);

    function lpad(s, w, len) {
        let str = s;
        while (str.length < len) {
            str = w + str;
        }
        return str;
    }

    // get all date properties ( based on PHP date object functionality )
    const DATE_PROPS = {
        d: date < 10 ? '0' + date : date,
        D: this.getDayAbbr(),
        j: this.getDate(),
        l: this.getDayFull(),
        S: this.getDaySuffix(),
        w: this.getDay(),
        z: this.getDayOfYear(),
        W: this.getWeekOfYear(),
        F: this.getMonthName(),
        m: month < 9 ? '0' + (month + 1) : month + 1,
        M: this.getMonthAbbr(),
        n: month + 1,
        t: this.getMonthDayCount(),
        L: this.isLeapYear() ? '1' : '0',
        Y: this.getFullYear(),
        y: this.getFullYear() + ''.substring(2, 4),
        a: hours > 12 ? 'pm' : 'am',
        A: hours > 12 ? 'PM' : 'AM',
        g: hours % 12 > 0 ? hours % 12 : 12,
        G: hours > 0 ? hours : "12",
        h: hours % 12 > 0 ? hours % 12 : 12,
        H: hours < 10 ? '0' + hours : hours,
        i: minutes < 10 ? '0' + minutes : minutes,
        s: seconds < 10 ? '0' + seconds : seconds,
        Z: "UTC" + (tzOffset > 0 ? "+" : "") + tzOffset,
        X: tzName,
        _: lpad(milli, '0', 3)
    };

    // loop through format array of characters and add matching data else add the format character (:,/, etc.)
    let dateString = "";
    for (let i = 0; i < dateFormat.length; i++) {
        let f = dateFormat[i];
        if (f.match(/[a-zA-Z|_]/g)) {
            dateString += DATE_PROPS[f] ? DATE_PROPS[f] : f; //'';
        } else {
            dateString += f;
        }
    }
    return dateString;
};
