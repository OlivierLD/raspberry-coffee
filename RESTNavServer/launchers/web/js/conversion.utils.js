//if (decToSex === undefined) {
    let decToSex = (val, ns_ew) => {
        let absVal = Math.abs(val);
        let intValue = Math.floor(absVal);
        let dec = absVal - intValue;
        let i = intValue;
        dec *= 60;
    //    var s = i + "°" + dec.toFixed(2) + "'";
    //    var s = i + String.fromCharCode(176) + dec.toFixed(2) + "'";
        let s = "";
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
//}

let hoursDecimalToHMS = (decHour) => {
	let h = Math.floor(decHour);
	let remainder = decHour - h;
	let min = Math.floor(remainder * 60);
	remainder = (remainder * 60) - min;
	let sec = (remainder * 60);
	return h + ":" + min + ":" + sec.toFixed(0);
};
