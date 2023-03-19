let lpad = (str, pad, len) => {
    while (str.length < len) {
        str = pad + str;
    }
    return str;
};

let openNav = () => {
    document.getElementById("side-nav").style.width = getComputedStyle(document.documentElement).getPropertyValue('--expanded-nav-width'); // "450px";
};

let closeNav = () => {
    document.getElementById("side-nav").style.width = "0";
};

const ALERT_DIALOG_ID = "custom-alert";
const PRMS_DIALOG_ID = "background-prms-dialog";
const HELP_DIALOG_ID = "help-dialog";
const MARQUEE_DIALOG_ID = "marquee-dialog";
const SUN_PATH_DIALOG_ID = "sun-path-dialog";

const BAD_BROWSER = "Your browser does not know about dialogs!\nPlease find one that does.\nYou can do it.";

let showPrmsDialog = () => {
    let prmsDialog = document.getElementById(PRMS_DIALOG_ID);
    if (prmsDialog.show !== undefined) {
        prmsDialog.show();
    } else {
        // alert(BAD_BROWSER);
        prmsDialog.style.display = 'inline';
    }
};

let closePrmsDialog = () => {
    let prmsDialog = document.getElementById(PRMS_DIALOG_ID);
    if (prmsDialog.close !== undefined) {
        prmsDialog.close();
    } else {
        // alert(BAD_BROWSER);
        prmsDialog.style.display = 'none';
    }
  };

  let showHelpDialog = () => {
    let helpDialog = document.getElementById(HELP_DIALOG_ID);
    if (helpDialog.show !== undefined) {
        helpDialog.show();
    } else {
        // alert(BAD_BROWSER);
        helpDialog.style.display = 'inline';
    }
};

let closeHelpDialog = () => {
    let helpDialog = document.getElementById(HELP_DIALOG_ID);
    if (helpDialog.close !== undefined) {
        helpDialog.close();
    } else {
        // alert(BAD_BROWSER);
        helpDialog.style.display = 'none';
    }
};

let showCustomAlert = (header, content) => {
    document.getElementById("alert-header").innerHTML = header;
    document.getElementById("alert-content").innerHTML = content;

    let customAlertDialog = document.getElementById(ALERT_DIALOG_ID);
    if (customAlertDialog.show !== undefined) {
        customAlertDialog.show();
    } else {
        // alert(BAD_BROWSER);
        customAlertDialog.style.display = 'inline';
    }
};

let closeCustomAlert = () => {
    let customAlertDialog = document.getElementById(ALERT_DIALOG_ID);
    if (customAlertDialog.close !== undefined) {
        customAlertDialog.close();
    } else {
        // alert(BAD_BROWSER);
        customAlertDialog.style.display = 'none';
    }
};

let showMarqueeDialog = () => {
    let marqueeDialog = document.getElementById(MARQUEE_DIALOG_ID);
    if (marqueeDialog.show !== undefined) {
        marqueeDialog.show();
    } else {
        // alert(BAD_BROWSER);
        marqueeDialog.style.display = 'inline';
    }
    marqueeScrollUp("marquee-panel-01"); // Start
};

let closeMarqueeDialog = () => {
    let marqueeDialog = document.getElementById(MARQUEE_DIALOG_ID);
    if (marqueeDialog.close !== undefined) {
        marqueeDialog.close();
    } else {
        // alert(BAD_BROWSER);
        marqueeDialog.style.display = 'none';
    }
    marqueeScrollUp("marquee-panel-01"); // Stop if started
};

let showSunPathDialog = () => {
    if (!globalAstroData) {
        showCustomAlert('Missing Data', '<p>⚠️&nbsp;<i><b>Compute something first!</b></i></p><p>(Menu > Background Parameters)</p>');
        // alert("Compute something first!\n(Menu > Background Parameters)");
        return;
    }
    let sunPathDialog = document.getElementById(SUN_PATH_DIALOG_ID);
    
    setSunPathData(); // Display current data (TODO: Animate?)

    if (sunPathDialog.show != undefined) {
        sunPathDialog.show();
    } else {
        // alert(BAD_BROWSER);
        sunPathDialog.style.display = 'inline';
    }
};

let closeSunPathDialog = () => {
    let sunPathDialog = document.getElementById(SUN_PATH_DIALOG_ID);
    if (sunPathDialog.close !== undefined) {
        sunPathDialog.close();
    } else {
        // alert(BAD_BROWSER);
        sunPathDialog.style.display = 'none';
    }
    if (sunPathUpdater !== undefined) {
        window.clearInterval(sunPathUpdater);
    }
};

let dragStartX = 0;
let dragStartY = 0;
let dialogXOrig = 0;
let dialogYOrig = 0;

let dragStartSP = event => {
    // console.log("Start dragging");
    event.dataTransfer.setData("drag-data", `${event.target.id}: ${new Date()} `); // Dummy, just for the example. See the ondrop function.
    dragStartX = event.x; // clientX;
    dragStartY = event.y; // clientY;
    let dialogBox = document.getElementById('sun-path-dialog');
    let computedStyle = window.getComputedStyle(dialogBox, null);
    dialogXOrig = parseFloat(computedStyle.marginLeft.replace('px', ''));
    dialogYOrig = parseFloat(computedStyle.marginTop.replace('px', ''));
    // console.log("End");
};

let draggingSP = event => {
    // console.log("Dragging!");
    if (true) {
        let dialogBox = document.getElementById('sun-path-dialog');
        // let computedStyle = window.getComputedStyle(dialogBox, null);
        if (event.buttons > 0) {
            // if (event.x === 0 && event.y === 0) {
            //     debugger;
            // }
            let deltaX = event.x - dragStartX; // clientX ;
            let deltaY = event.y - dragStartY; // clientY;
            let newLeft = `${dialogXOrig + deltaX}px`;
            let newTop = `${dialogYOrig + deltaY}px`;
            dialogBox.style.marginLeft = newLeft;
            dialogBox.style.marginTop = newTop;
            // console.log(`event.x:${event.x}, event.y:${event.y}, dragStartX: ${dragStartX}, dragStartY: ${dragStartY}, dialogXOrig:${dialogXOrig}, dialogYOrig:${dialogYOrig}, New Left: ${newLeft}, New Top: ${newTop}`);
            // console.log("evt:", event);
        }
        event.preventDefault();
    }
};

let customAlertExpanded = false;
let expandCollapseAlertData = () => {
    document.getElementById('alert-zone').classList.toggle('visible-div');
    customAlertExpanded = !customAlertExpanded;
};

let customAlert = (errMess) => {
    document.getElementById('alert-mess').innerText = errMess;
    if (!customAlertExpanded) {
        expandCollapseAlertData();
    }
    setTimeout(hideAlert, 5000);
};

let hideAlert = () => {
    if (customAlertExpanded) {
        expandCollapseAlertData();
    }
};

let copyToClipboard = (fieldId) => {
    let value = document.getElementById(fieldId).innerHTML;
    let codeContent = value.replaceAll("<br>", "\n");
    // console.log(codeContent);
    let codeHolder = document.createElement("textarea"); // To keep the format, 'input' would not.
    codeHolder.value = codeContent;
    document.body.appendChild(codeHolder);
    codeHolder.select();
    document.execCommand("copy");
    document.body.removeChild(codeHolder);
    customAlert(`Value ${value} copied to clipboard`);
};

// TODO Calculate real sunPath & others
// See getSunDataForAllDay, in astrorest.RESTImplementation, module RESTNauticalAlmanac
const sunPath1 = [
    {
        "epoch": 1526993906000,
        "alt": -0.2099868844664718,
        "z": 63.606812406293436
    },
    {
        "epoch": 1526994506000,
        "alt": 1.5725579156196041,
        "z": 65.12679994159187
    },
    {
        "epoch": 1526995106000,
        "alt": 3.3769591507214347,
        "z": 66.62202808307599
    },
    {
        "epoch": 1526995706000,
        "alt": 5.2016579554516245,
        "z": 68.09509263145956
    },
    {
        "epoch": 1526996306000,
        "alt": 7.045171637599468,
        "z": 69.54860826085613
    },
    {
        "epoch": 1526996906000,
        "alt": 8.90608707106982,
        "z": 70.98521652239666
    },
    {
        "epoch": 1526997506000,
        "alt": 10.7830534265924,
        "z": 72.40759587016622
    },
    {
        "epoch": 1526998106000,
        "alt": 12.674774950839689,
        "z": 73.81847434523108
    },
    {
        "epoch": 1526998706000,
        "alt": 14.58000365007892,
        "z": 75.22064493383381
    },
    {
        "epoch": 1526999306000,
        "alt": 16.497531826233377,
        "z": 76.61698379624755
    },
    {
        "epoch": 1526999906000,
        "alt": 18.42618439806157,
        "z": 78.01047166274785
    },
    {
        "epoch": 1527000506000,
        "alt": 20.364810785800085,
        "z": 79.40421870679711
    },
    {
        "epoch": 1527001106000,
        "alt": 22.31227702589541,
        "z": 80.80149398703578
    },
    {
        "epoch": 1527001706000,
        "alt": 24.26745601706344,
        "z": 82.20575873277927
    },
    {
        "epoch": 1527002306000,
        "alt": 26.229218321531746,
        "z": 83.62070619823055
    },
    {
        "epoch": 1527002906000,
        "alt": 28.196421348207615,
        "z": 85.05030775046349
    },
    {
        "epoch": 1527003506000,
        "alt": 30.16789742176711,
        "z": 86.49886707912646
    },
    {
        "epoch": 1527004106000,
        "alt": 32.14244026439827,
        "z": 87.97108407744167
    },
    {
        "epoch": 1527004706000,
        "alt": 34.11878946091102,
        "z": 89.47213042537287
    },
    {
        "epoch": 1527005306000,
        "alt": 36.09561236563241,
        "z": 91.00773937888256
    },
    {
        "epoch": 1527005906000,
        "alt": 38.07148263991452,
        "z": 92.5843127443934
    },
    {
        "epoch": 1527006506000,
        "alt": 40.04485534157959,
        "z": 94.209049456021
    },
    {
        "epoch": 1527007106000,
        "alt": 42.014035476556565,
        "z": 95.89009878172664
    },
    {
        "epoch": 1527007706000,
        "alt": 43.97714124147301,
        "z": 97.63674584837959
    },
    {
        "epoch": 1527008306000,
        "alt": 45.93205814252986,
        "z": 99.45963455698741
    },
    {
        "epoch": 1527008906000,
        "alt": 47.87638244113664,
        "z": 101.37103626346723
    },
    {
        "epoch": 1527009506000,
        "alt": 49.80735076732526,
        "z": 103.38517278483485
    },
    {
        "epoch": 1527010106000,
        "alt": 51.721752026294446,
        "z": 105.5186027588207
    },
    {
        "epoch": 1527010706000,
        "alt": 53.61581666744556,
        "z": 107.79067941795196
    },
    {
        "epoch": 1527011306000,
        "alt": 55.4850770129682,
        "z": 110.22408396411828
    },
    {
        "epoch": 1527011906000,
        "alt": 57.324191858498935,
        "z": 112.84543091624622
    },
    {
        "epoch": 1527012506000,
        "alt": 59.12672470464755,
        "z": 115.68591964049823
    },
    {
        "epoch": 1527013106000,
        "alt": 60.88486885277489,
        "z": 118.78197623420235
    },
    {
        "epoch": 1527013706000,
        "alt": 62.58910959967611,
        "z": 122.17575975632079
    },
    {
        "epoch": 1527014306000,
        "alt": 64.22782206479397,
        "z": 125.91530036312844
    },
    {
        "epoch": 1527014906000,
        "alt": 65.78681636436573,
        "z": 130.05386162104537
    },
    {
        "epoch": 1527015506000,
        "alt": 67.24887106296073,
        "z": 134.64787132682525
    },
    {
        "epoch": 1527016106000,
        "alt": 68.59334857792707,
        "z": 139.7524770762683
    },
    {
        "epoch": 1527016706000,
        "alt": 69.79606891593049,
        "z": 145.41360328901172
    },
    {
        "epoch": 1527017306000,
        "alt": 70.82972279723717,
        "z": 151.65568458986596
    },
    {
        "epoch": 1527017906000,
        "alt": 71.66518032738175,
        "z": 158.46561591250043
    },
    {
        "epoch": 1527018506000,
        "alt": 72.27399097230337,
        "z": 165.7763978348453
    },
    {
        "epoch": 1527019106000,
        "alt": 72.63201897982606,
        "z": 173.45767882667383
    },
    {
        "epoch": 1527019706000,
        "alt": 72.72350680384675,
        "z": 181.3218707806489
    },
    {
        "epoch": 1527020306000,
        "alt": 72.54423055836675,
        "z": 189.14969797520763
    },
    {
        "epoch": 1527020906000,
        "alt": 72.10241505749144,
        "z": 196.72837293726573
    },
    {
        "epoch": 1527021506000,
        "alt": 71.41698091962526,
        "z": 203.88708596619713
    },
    {
        "epoch": 1527022106000,
        "alt": 70.51392705866238,
        "z": 210.5163872885313
    },
    {
        "epoch": 1527022706000,
        "alt": 69.42228323831496,
        "z": 216.56841130430126
    },
    {
        "epoch": 1527023306000,
        "alt": 68.17080380572354,
        "z": 222.0441898190897
    },
    {
        "epoch": 1527023906000,
        "alt": 66.7858742985252,
        "z": 226.9767816064943
    },
    {
        "epoch": 1527024506000,
        "alt": 65.29053479194843,
        "z": 231.41624947446195
    },
    {
        "epoch": 1527025106000,
        "alt": 63.704280119859746,
        "z": 235.41883878798131
    },
    {
        "epoch": 1527025706000,
        "alt": 62.04329615332145,
        "z": 239.04029645835809
    },
    {
        "epoch": 1527026306000,
        "alt": 60.32088600855201,
        "z": 242.33232088945616
    },
    {
        "epoch": 1527026906000,
        "alt": 58.54794003152568,
        "z": 245.34104946102016
    },
    {
        "epoch": 1527027506000,
        "alt": 56.73337634134839,
        "z": 248.10673224046548
    },
    {
        "epoch": 1527028106000,
        "alt": 54.884521922325725,
        "z": 250.6640257636111
    },
    {
        "epoch": 1527028706000,
        "alt": 53.0074301240562,
        "z": 253.04256147259633
    },
    {
        "epoch": 1527029306000,
        "alt": 51.10713688304214,
        "z": 255.26760108765885
    },
    {
        "epoch": 1527029906000,
        "alt": 49.18786614949623,
        "z": 257.3606776204307
    },
    {
        "epoch": 1527030506000,
        "alt": 47.253193036757025,
        "z": 259.3401782446223
    },
    {
        "epoch": 1527031106000,
        "alt": 45.30617322207758,
        "z": 261.22185304900444
    },
    {
        "epoch": 1527031706000,
        "alt": 43.34944569039383,
        "z": 263.0192487774731
    },
    {
        "epoch": 1527032306000,
        "alt": 41.3853145750893,
        "z": 264.744073546755
    },
    {
        "epoch": 1527032906000,
        "alt": 39.41581479262191,
        "z": 266.4065010228446
    },
    {
        "epoch": 1527033506000,
        "alt": 37.44276425374744,
        "z": 268.01542372591217
    },
    {
        "epoch": 1527034106000,
        "alt": 35.46780742077168,
        "z": 269.5786622038156
    },
    {
        "epoch": 1527034706000,
        "alt": 33.49244972918296,
        "z": 271.1031396967581
    },
    {
        "epoch": 1527035306000,
        "alt": 31.518086537288667,
        "z": 272.5950269036466
    },
    {
        "epoch": 1527035906000,
        "alt": 29.546027121076158,
        "z": 274.05986277640625
    },
    {
        "epoch": 1527036506000,
        "alt": 27.57751486493755,
        "z": 275.5026555587792
    },
    {
        "epoch": 1527037106000,
        "alt": 25.613744454971783,
        "z": 276.92796760545053
    },
    {
        "epoch": 1527037706000,
        "alt": 23.655876703232806,
        "z": 278.3399868660604
    },
    {
        "epoch": 1527038306000,
        "alt": 21.705051635474486,
        "z": 279.7425872785133
    },
    {
        "epoch": 1527038906000,
        "alt": 19.762399457518548,
        "z": 281.139380529669
    },
    {
        "epoch": 1527039506000,
        "alt": 17.8290516805221,
        "z": 282.53375930685945
    },
    {
        "epoch": 1527040106000,
        "alt": 15.906150043427512,
        "z": 283.9289351574024
    },
    {
        "epoch": 1527040706000,
        "alt": 13.994855406684971,
        "z": 285.3279705352584
    },
    {
        "epoch": 1527041306000,
        "alt": 12.09635599506581,
        "z": 286.73380639506433
    },
    {
        "epoch": 1527041906000,
        "alt": 10.211875256363399,
        "z": 288.1492858642048
    },
    {
        "epoch": 1527042506000,
        "alt": 8.342679440591583,
        "z": 289.5771744802884
    },
    {
        "epoch": 1527043106000,
        "alt": 6.490084986222032,
        "z": 291.0201773574161
    },
    {
        "epoch": 1527043706000,
        "alt": 4.655465891970013,
        "z": 292.48095344860377
    },
    {
        "epoch": 1527044306000,
        "alt": 2.8402603835320046,
        "z": 293.96212766140843
    },
    {
        "epoch": 1527044906000,
        "alt": 1.0459787359533554,
        "z": 295.4662994436908
    }
];
const sunData1 = {
    "epoch": 1527006247015,
    "lat": 37.76661945,
    "lng": -122.5166988,
    "body": "Sun",
    "decl": 20.463700660620308,
    "gha": 66.85965851342138,
    "altitude": 39.19342734948766,
    "z": 93.50129805928104,
    "eot": 20.112413796883015,
    "riseTime": 1526993906000,
    "setTime": 1527045303000,
    "riseZ": 63.028849635612275,
    "setZ": 296.9711503643877
};
const userPosition1 = {
    "latitude": 37.76661945,
    "longitude": -122.5166988
};

const moonPos = {
    he: -24.75,
    z: 249.12
};
const venusPos = {he: 55.96217881422134, z: 144.3333252902306};
const marsPos = {he: 58.47991564246026, z: 149.3054495194241};
const jupiterPos = {he: -28.40640020324427, z: 97.22917381313722};
const saturnPos = {he: -53.297318027659344, z: 76.66895229998141};

let sunPathUpdater;

let setSunPathData = () => {
    let elem1 = document.getElementById('sun-path-01');

    let current = new Date(globalAstroData.epoch);
    let year = current.getUTCFullYear();
	let month = current.getUTCMonth() + 1;
	let day = current.getUTCDate();
	let hour = current.getUTCHours();
	let minute = current.getUTCMinutes();
	let second = current.getUTCSeconds();
    let duration = `${year.toString()}-${lpad(month.toString(), '0', 2)}-${lpad(day.toString(), '0', 2)}T${lpad(hour.toString(), '0', 2)}:${lpad(minute.toString(), '0', 2)}:${lpad(second.toFixed(0), '0', 2) + '.000'}Z`;

    // That step is demanding... (Rise & Set adjustments)
    let sunBodyData = CelestialComputer.getSunDataForDate(globalAstroData.deltaT, 
                                                          duration, 
                                                          userPos.latitude, 
                                                          userPos.longitude, 
                                                          globalAstroData.epoch,
                                                          globalAstroData.sun.DEC.raw, 
                                                          globalAstroData.sun.GHA.raw,
                                                          globalAstroData.sun.HP.raw, 
                                                          globalAstroData.sun.SD.raw, 
                                                          globalAstroData.EOT.raw);
    sunBodyData.altitude =  sunBodyData.elev; // Small Tx.                                                        

    // elem1.sunPath = sunPath1;       // get it from CelestialComputer.getSunDataForAllDay
    const STEP = 20;
    let sunPath = CelestialComputer.getSunDataForAllDay(sunBodyData, 
                                                        globalAstroData.deltaT, 
                                                        userPos.latitude, 
                                                        userPos.longitude, 
                                                        STEP, 
                                                        globalAstroData.epoch);
    let txSunPath = [];
    sunPath.forEach( sp => {
        txSunPath.push({ epoch: sp.epoch, alt: sp.he, z: sp.z });
    });                                                        
    elem1.sunPath = txSunPath;
    // elem1.sunData = sunData1;       // get it from CelestialComputer.getSunDataForDate
    elem1.sunData = sunBodyData;

    elem1.userPos = userPos;

    let bodiesUpdater = () => {
        let srSun = sightReduction(userPos.latitude, userPos.longitude, globalAstroData.sun.GHA.raw, globalAstroData.sun.DEC.raw);
        let srMoon = sightReduction(userPos.latitude, userPos.longitude, globalAstroData.moon.GHA.raw, globalAstroData.moon.DEC.raw);
        let srVenus = sightReduction(userPos.latitude, userPos.longitude, globalAstroData.venus.GHA.raw, globalAstroData.venus.DEC.raw);
        let srMars = sightReduction(userPos.latitude, userPos.longitude, globalAstroData.mars.GHA.raw, globalAstroData.mars.DEC.raw);
        let srJupiter = sightReduction(userPos.latitude, userPos.longitude, globalAstroData.jupiter.GHA.raw, globalAstroData.jupiter.DEC.raw);
        let srSaturn = sightReduction(userPos.latitude, userPos.longitude, globalAstroData.saturn.GHA.raw, globalAstroData.saturn.DEC.raw);

        elem1.sunPos = { he: srSun.alt, z: srSun.Z }; 
        elem1.moonPos = { he: srMoon.alt, z: srMoon.Z, phase: globalAstroData.moon.phase.phaseAngle }; // moonPos;
        elem1.venusPos = { he: srVenus.alt, z: srVenus.Z }; // venusPos;
        elem1.marsPos = { he: srMars.alt, z: srMars.Z }; // marsPos;
        elem1.jupiterPos = { he: srJupiter.alt, z: srJupiter.Z }; // jupiterPos;
        elem1.saturnPos = { he: srSaturn.alt, z: srSaturn.Z }; // saturnPos;
        elem1.repaint();
    };
    sunPathUpdater = window.setInterval(bodiesUpdater, 1_000);

    elem1.sunRise = { time:  sunBodyData.riseTime, z:  sunBodyData.riseZ };
    elem1.sunSet = { time:  sunBodyData.setTime, z:  sunBodyData.setZ };

    let tt = CelestialComputer.getSunMeridianPassageTime(userPos.latitude, userPos.longitude, globalAstroData.EOT.raw);
	let dms = CelestialComputer.decimalToDMS(tt);

    let ttDate, ttEpoch;
    if (false) { // Firefox does not like it
        ttDate = new Date(`${year.toString()}-${lpad(month.toString(), '0', 2)}-${lpad(day.toString(), '0', 2)} 00:00:00 GMT+0000`);
        ttEpoch = ttDate.getTime() + (dms.hours * 3600 * 1000) + (dms.minutes * 60 * 1000) + (dms.seconds * 1000);
    } else {
        ttEpoch = Date.UTC(year, month - 1, day, 0, 0, 0) + (dms.hours * 3600 * 1000) + (dms.minutes * 60 * 1000) + (dms.seconds * 1000);
    }

    let transitEl = 0;
    for (let i=0; i<sunPath.length; i++) {
        // if (sunPath[i].epoch > ttEpoch) {
        if (sunPath[i].z < 270 && sunPath[i].z >= 180) { // Approx. < 270: avoid values like 359...
            transitEl = sunPath[i].he;
            break;
        }
    }
    // console.log(`NavBar: Transit Elev: ${transitEl} deg.`);

    elem1.sunTransit = { time: ttEpoch, elev: transitEl };

    elem1.repaint();
}
