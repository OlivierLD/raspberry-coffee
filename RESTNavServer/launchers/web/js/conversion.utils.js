var decToSex = function (val, ns_ew) {
	var absVal = Math.abs(val);
	var intValue = Math.floor(absVal);
	var dec = absVal - intValue;
	var i = intValue;
	dec *= 60;
//    var s = i + "°" + dec.toFixed(2) + "'";
//    var s = i + String.fromCharCode(176) + dec.toFixed(2) + "'";
	var s = "";
	if (ns_ew !== undefined) {
		if (val < 0) {
			s += (ns_ew === 'NS' ? 'S' : 'W');
		} else {
			s += (ns_ew === 'NS' ? 'N' : 'E');
		}
		s += " ";
	} else {
		if (val < 0) {
			s += '-'
		}
	}
	s += i + "\272" + dec.toFixed(2) + "'";

	return s;
};

var hoursDecimalToHMS = function(decHour) {
	var h = Math.floor(decHour);
	var remainder = decHour - h;
	var min = Math.floor(remainder * 60);
	remainder = (remainder * 60) - min;
	var sec = (remainder * 60);
	return h + ":" + min + ":" + sec.toFixed(0);
};
