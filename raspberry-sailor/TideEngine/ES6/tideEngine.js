// Load the data. These are big files.
import * as constituents from './json/constituents.js';
import * as stations from './json/stations.js';

const VERBOSE = false; 

if (VERBOSE) {  // This is for tests
    let keys = Object.keys(constituents.default);
    console.log(`Constituents: ${keys.length} keys.`);
    keys.forEach(k => {
        console.log(`1 - ${k}`);
    });

    // First key
    let firstObj = constituents.default[keys[0]];
    let secondLevelKeys = Object.keys(firstObj);
    secondLevelKeys.forEach(k => {
        console.log(`2 - ${k}`);
    });
}

let tideStations = stations.default["stations"];

if (VERBOSE) {
    console.log(`We have ${Object.keys(tideStations).length} stations.`);
}

const COEFF_FOR_EPOCH = 0.01745329251994329;

function getAmplitudeFix(constMap, year, name) {
    let speedMap = constMap[name];
    if (speedMap) {
        let factors = speedMap.factors;
        let d = factors[year];
        if (d !== null) {
            return d;
        }
    }
    return 0;
}

function getEpochFix(constMap, year, name) {
    let speedMap = constMap[name];
    if (speedMap) {
        let equilibrium = speedMap.equilibrium;
        let d = equilibrium[year];
        if (d !== null) {
            return d * COEFF_FOR_EPOCH;
        }
    }
    return 0;
}

/**
 * The heart of the whole thing.
 * Exported at the end.
 */
let tideComputer = {

    getStations: () => {
        return stations.default.stations;
    },
    // TODO Manage the reload? Duplicate the maps? See harmonicFixedForYear. Look for 'harmonicFixedForYear' below.
    buildSiteConstSpeed: () => {

        let coeffMap = constituents.default["constSpeedMap"];
        let coeffKeys = Object.keys(coeffMap);

        let siteConstSpeed = {};
        coeffKeys.forEach(k => {
            let constSpeed = coeffMap[k];
            siteConstSpeed[k] = {
                coeffName: k,
                coeffValue: COEFF_FOR_EPOCH * constSpeed.coeffValue
            };
        });
        return siteConstSpeed;
    },
    findTideStation: (stationName, year) => { // also fixex the coefficients for the given year.
        // Try full match
        let ts = null;
        let keys = Object.keys(tideStations);
        let keyLen = keys.length;
        for (let i = 0; i < keyLen; i++) {
            if (keys[i] === stationName) {
                ts = tideStations[stationName];
                break;
            }
        }
        if (ts === null) {
            // Try first partial match
            for (let i = 0; i < keyLen; i++) {
                if (keys[i].toLowerCase().includes(stationName.toLowerCase())) {
                    ts = tideStations[keys[i]];
                    if (ts !== null) {
                        break;
                    }
                }
            }
        }
        if (ts !== null) { // Fix coeffs
            // console.log(`Year requested: ${year}, harmonicFixedForYear: ${ts.harmonicFixedForYear}`);
            let harmonics = ts.harmonics;
            // TODO Check condition below...
            if (ts.harmonicFixedForYear === undefined || ts.harmonicFixedForYear !== year) {
                    harmonics.forEach(harm => {
                    if (harm.name !== "x") {
                        let amplitudeFix = getAmplitudeFix(constituents.default["constSpeedMap"],
                                                           year,
                                                           harm.name);
                        let epochFix = getEpochFix(constituents.default["constSpeedMap"],
                                                   year,
                                                   harm.name);
                        let originalAmplitude = harm.amplitude;
                        let originalEpoch = harm.epoch * COEFF_FOR_EPOCH;

                        harm.amplitude = originalAmplitude * amplitudeFix;
                        harm.epoch = originalEpoch - epochFix;
                    }
                });
            } else {
                console.log(`Harmonics already fixed for year ${ts.harmonicFixedForYear}`);
            }
            ts.harmonicFixedForYear = year;
        }
        return ts;
    },
    getWaterHeight: (date, jan1st, station, constSpeed) => {
        let value = 0;
        let baseHeight = station.baseHeight;

        let dateMS = date.getTime();
        let jan1stMS = jan1st.getTime();

        let nbSecSinceJan1st = (dateMS - jan1stMS) / 1000;
        let timeOffset = nbSecSinceJan1st * 0.00027777777777777778; // 1 / 3600

        if (VERBOSE) {
            console.log(`Used TimeOffset in hours: ${timeOffset}, base height: ${baseHeight}`);
        }

        value = baseHeight;
        let stationHarmonics = station.harmonics;
        let csKeys = Object.keys(constSpeed);
        csKeys.forEach(k => {
            let stationHarmonicCoeff = null;
            for (let i = 0; i < stationHarmonics.length; i++) {
                if (stationHarmonics[i].name === k) {
                    stationHarmonicCoeff = stationHarmonics[i];
                    break;
                }
            }
            if (stationHarmonicCoeff === null) {
                // Oooch! TODO Honk.
            }
            let cs = constSpeed[k];
            let addition = (stationHarmonicCoeff.amplitude * Math.cos(cs.coeffValue * timeOffset - stationHarmonicCoeff.epoch));
            value += addition;
            if (VERBOSE) {
                console.log(`Coeff ${stationHarmonicCoeff.name} - Amplitude: ${stationHarmonicCoeff.amplitude}, Speed Value: ${cs.coeffValue}, Epoch: ${stationHarmonicCoeff.epoch} => Value: ${value}`);
            }
        });
        return value;
    }
};

/**
 * Utility function.
 * Decimal degrees to degrees, decimal minutes (2 decimal places).
 * If ns_ew is provided ("ES" or "NS"), E, W, N, S will be used as prefixes
 * If ns_ew is not provided, result will be signed.
 * @param {float} val, the value to convert 
 * @param {string} ns_ew, optional
 * @returns string. The converted value.
 */
export function decToSex(val, ns_ew) {
    let absVal = Math.abs(val);
    let intValue = Math.floor(absVal);
    let dec = absVal - intValue;
    let i = intValue;
    dec *= 60; //    let s = i + "°" + dec.toFixed(2) + "'";
    //    let s = i + String.fromCharCode(176) + dec.toFixed(2) + "'";
  
    let s = ""; // sign
  
    if (ns_ew !== undefined) {
      if (val < 0) {
        s += ns_ew === 'NS' ? 'S' : 'W';
      } else {
        s += ns_ew === 'NS' ? 'N' : 'E';
      }
  
      s += " ";
    } else {
      if (val < 0) {
        s += '-';
      }
    }
  
    s += i + "°" + dec.toFixed(2) + "'";
    return s;
};

export default tideComputer;

