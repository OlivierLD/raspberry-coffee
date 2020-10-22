/**
 * @author Olivier Le Diouris
 */
/*
 * See custom properties in CSS.
 * =============================
 * @see https://developer.mozilla.org/en-US/docs/Web/CSS/--*
 * Relies on a rule named .graphdisplay, like that:
 *
 .graphdisplay {

 --tooltip-color: rgba(250, 250, 210, .7);
 --tooltip-text-color: black;
 --with-bg-gradient: true;
 --bg-gradient-from: rgba(0,0,0,0);
 --bg-gradient-to: cyan;

 --bg-color: LightGray;

 --horizontal-grid-color: gray;
 --horizontal-grid-text-color: black;
 --vertical-grid-color: gray;
 --vertical-grid-text-color: black;

 --raw-data-line-color: green;
 --fill-raw-data: true;
 --raw-data-fill-color: rgba(0, 255, 0, 0.35);

 --smooth-data-line-color: red;
 --fill-smooth-data: true;
 --smooth-data-fill-color: rgba(0, 255, 0, 0.35);

 --clicked-index-color: orange;

 --font: Arial;
 }
 */

/**
 * Recurse from the top down, on styleSheets and cssRules
 *
 * document.styleSheets[0].cssRules[2].selectorText returns ".analogdisplay"
 * document.styleSheets[0].cssRules[2].cssText returns ".analogdisplay { --hand-color: red;  --face-color: white; }"
 * document.styleSheets[0].cssRules[2].style.cssText returns "--hand-color: red; --face-color: white;"
 */
let getColorConfig = () => {
    let colorConfig = defaultGraphColorConfig;
    for (let s = 0; s < document.styleSheets.length; s++) {
        for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
            if (document.styleSheets[s].cssRules[r].selectorText === '.graphdisplay') {
                let cssText = document.styleSheets[s].cssRules[r].style.cssText;
                let cssTextElems = cssText.split(";");
                cssTextElems.forEach((elem) => {
                    if (elem.trim().length > 0) {
                        let keyValPair = elem.split(":");
                        let key = keyValPair[0].trim();
                        let value = keyValPair[1].trim();
                        switch (key) {
                            case '--tooltip-color':
                                colorConfig.tooltipColor = value;
                                break;
                            case '--tooltip-text-color':
                                colorConfig.tooltipTextColor = value;
                                break;
                            case '--with-bg-gradient':
                                colorConfig.withBGGradient = (value === 'true');
                                break;
                            case '--bg-gradient-from':
                                colorConfig.bgGradientFrom = value;
                                break;
                            case '--bg-gradient-to':
                                colorConfig.bgGradientTo = value;
                                break;
                            case '--bg-color':
                                colorConfig.bgColorNoGradient = value;
                                break;
                            case '--horizontal-grid-color':
                                colorConfig.horizontalGridColor = value;
                                break;
                            case '--horizontal-grid-text-color':
                                colorConfig.horizontalGridTextColor = value;
                                break;
                            case '--vertical-grid-color':
                                colorConfig.verticalGridColor = value;
                                break;
                            case '--vertical-grid-text-color':
                                colorConfig.verticalGridTextColor = value;
                                break;
                            case '--raw-data-line-color':
                                colorConfig.rawDataLineColor = value;
                                break;
                            case '--fill-raw-data':
                                colorConfig.fillRawData = (value === 'true');
                                break;
                            case '--raw-data-fill-color':
                                colorConfig.rawDataFillColor = value;
                                break;
                            case '--smooth-data-line-color':
                                colorConfig.smoothDataLineColor = value;
                                break;
                            case '--fill-smooth-data':
                                colorConfig.fillSmoothData = (value === 'true');
                                break;
                            case '--smooth-data-fill-color':
                                colorConfig.smoothDataFillColor = value;
                                break;
                            case '--clicked-index-color':
                                colorConfig.clickedIndexColor = value;
                                break;
                            case '--font':
                                colorConfig.font = value;
                                break;
                            default:
                                break;
                        }
                    }
                });
            }
        }
    }
    return colorConfig;
};

const defaultGraphColorConfig = {
    tooltipColor: "rgba(250, 250, 210, .7)",
    tooltipTextColor: "black",
    withBGGradient: true,
    bgGradientFrom: 'rgba(0,0,0,0)',
    bgGradientTo: 'cyan',
    bgColorNoGradient: "LightGray",
    horizontalGridColor: "gray",
    horizontalGridTextColor: "black",
    verticalGridColor: "gray",
    verticalGridTextColor: "black",
    rawDataLineColor: "green",
    fillRawData: true,
    rawDataFillColor: "rgba(0, 255, 0, 0.35)",
    smoothDataLineColor: "red",
    fillSmoothData: true,
    smoothDataFillColor: "rgba(255, 0, 0, 0.35)",
    clickedIndexColor: 'orange',
    font: 'Arial'
};
let graphColorConfig = defaultGraphColorConfig;

/**
 *
 * @param cName Canvas name
 * @param graphData Structure is
 *           {
 *             curve: array[Tuple],
 *             harmonics: array[{
 *                                name: "coeff",
 *                                data: array[Tuple]
 *                              }],
 *             base: baseHeight,
 *             station: "Station Name",
 *             unit: "unit",
 *             sunRiseSet: array[ {rise: epoch, set: epoch }]
 *           }
 * @param callback On mouse-click
 * @constructor
 */
function TideGraph(cName,       // Canvas Name
                   graphData,   // x,y tuple array
                   callback) {  // Callback on mouseclick

    let instance = this;
    let gData = graphData;
    let plotX;
    let harmonicColors = [];
    let sunRiseSet = undefined;
    let altitudes = undefined;
    let table = undefined;

    this.setSunData = (values) => {
        sunRiseSet = values;
    };

    this.unsetSunData = () => {
        sunRiseSet = undefined;
    };

    this.setAltitudes = (values) => {
        altitudes = values;
    };

    this.setTable = (values) => {
        table = values;
    };

    this.unsetAltitudes = () => {
        altitudes = undefined;
    };

    this.unsetTable = () => {
        table = undefined;
    };

    let initHarmonicColors = () => {
        intRange(0, 98).forEach((obj, idx) => {
            harmonicColors.push(rndColor());
        });
    };

    if (events !== undefined) {
        events.subscribe('color-scheme-changed', (val) => {
            //  console.log('Color scheme changed:', val);
            reloadColorConfig();
        });
    }

    graphColorConfig = getColorConfig();

    let xScale, yScale;
    let minx, miny, maxx, maxy;
    let context;

    let withTooltip = true;

    this.setTooltip = (tt) => {
        withTooltip = tt;
    };
    let canvas = document.getElementById(cName);

    canvas.addEventListener('click', (evt) => {
        let x = evt.pageX - canvas.offsetLeft;
        let y = evt.pageY - canvas.offsetTop;

        let coords = relativeMouseCoords(evt, canvas);
        x = coords.x;
        y = coords.y;
//      console.log("Mouse: x=" + x + ", y=" + y);

        let idx = Math.round(x / xScale);
        if (idx < graphData.length) {
            if (callback !== undefined && callback !== null) {
                callback(idx);
            }
            lastClicked = idx;
        }
    }, 0);

    canvas.addEventListener('mousemove', (evt) => {
        if (withTooltip === true) {
            let x = evt.pageX - canvas.offsetLeft;
            let y = evt.pageY - canvas.offsetTop;

            let coords = relativeMouseCoords(evt, canvas);
            x = coords.x;
            y = coords.y;
//          console.log("Mouse: x=" + x + ", y=" + y);

            let idx = xScale !== 0 ? Math.round(x / xScale) : 0;
            if (gData.curve !== undefined && idx < gData.curve.length) {
                let str = [];
                try {
                    str.push(gData.curve[idx].getY().date);
                    str.push(gData.curve[idx].getY().time + " " + gData.curve[idx].getY().tz);
                    str.push(gData.curve[idx].getY().wh.toFixed(2) + " " + gData.unit);
                    //      console.log("Bubble:" + str);
                } catch (err) {
                    console.log(JSON.stringify(err));
                }

                //    context.fillStyle = '#000';
                //    context.fillRect(0, 0, w, h);
                instance.drawGraph(cName, gData, plotX, table);
                let tooltipW = 100, nblines = str.length;
                context.fillStyle = graphColorConfig.tooltipColor;
//      context.fillStyle = 'yellow';
                let fontSize = 10;
                let x_offset = 10, y_offset = 10;

                if (x > (canvas.getContext('2d').canvas.clientWidth / 2)) {
                    x_offset = -(tooltipW + 10);
                }
                if (y > (canvas.getContext('2d').canvas.clientHeight / 2)) {
                    y_offset = -(10 + 6 + (nblines * fontSize));
                }
                context.fillRect(x + x_offset, y + y_offset, tooltipW, 6 + (nblines * fontSize)); // Background
                context.fillStyle = graphColorConfig.tooltipTextColor;
                context.font = /*'bold ' +*/ fontSize + 'px verdana';
                for (let i = 0; i < str.length; i++) {
                    context.fillText(str[i], x + x_offset + 5, y + y_offset + (3 + (fontSize * (i + 1)))); //, 60);
                }
            }
        } else {
            console.log("No tooltip");
        }
    }, 0);

    let relativeMouseCoords = (event, element) => {
        let totalOffsetX = 0;
        let totalOffsetY = 0;
        let canvasX = 0;
        let canvasY = 0;
        let currentElement = element;

        do {
            totalOffsetX += currentElement.offsetLeft - currentElement.scrollLeft;
            totalOffsetY += currentElement.offsetTop - currentElement.scrollTop;
        } while (currentElement = currentElement.offsetParent)

        canvasX = event.pageX - totalOffsetX;
        canvasY = event.pageY - totalOffsetY;

        return {x: canvasX, y: canvasY};
    };

    this.minX = (data) => {
        let min = Number.MAX_VALUE;
        for (let i = 0; i < data.length; i++) {
            min = Math.min(min, data[i].getX());
        }
        return min;
    };

    this.minY = (data) => {
        let min = Number.MAX_VALUE;
        for (let i = 0; i < data.length; i++) {
            min = Math.min(min, data[i].getY().wh);
        }
        return min;
    };

    this.maxX = (data) => {
        let max = Number.MIN_VALUE;
        for (let i = 0; i < data.length; i++) {
            max = Math.max(max, data[i].getX());
        }
        return max;
    };

    this.maxY = (data) => {
        let max = Number.MIN_VALUE;
        for (let i = 0; i < data.length; i++) {
            max = Math.max(max, data[i].getY().wh);
        }
        return max;
    };

    this.getMinMax = (data) => {
        let mini = Math.floor(this.minY(data));
        let maxi = Math.ceil(this.maxY(data));

        if (false && Math.abs(maxi - mini) < 5) { // This is to have a significant Y scale, when applied.
            maxi += 3;
            if (mini > 0) {
                mini -= 1;
            } else {
                maxi += 1;
            }
        }
        return {mini: mini, maxi: maxi};
    };

    let reloadColor = false;
    let reloadColorConfig = () => {
//  console.log('Color scheme has changed');
        reloadColor = true;
    };

    /**
     * Drawing happens here.
     * Draws:
     * - Grids and background
     * - Main curve
     * - Tooltips
     * - Harmonic curves
     * - Daylight
     * - Moon declination (TODO)
     * - Moon phases
     * @param displayCanvasName
     * @param data
     * @param idx
     * @param table
     */
    this.drawGraph = (displayCanvasName, data, idx, table) => {
        console.log("In DrawGraph");
        gData = data;
        plotX = idx;
        instance.setTable(table);

        if (reloadColor) {
            // In case the CSS has changed, dynamically.
            getColorConfig();
        }
        reloadColor = false;

        if (data === undefined || data.curve === undefined) {
            return;
        }
        if (data !== undefined && data.curve !== undefined && data.curve.length < 2) {
            return;
        }

        context = canvas.getContext('2d');

        let width = context.canvas.clientWidth;
        let height = context.canvas.clientHeight;

        if (width === 0 || height === 0) { // Not visible
            return;
        }
        this.init(data.curve);

        // Set the canvas size from its container.
        canvas.width = width;
        canvas.height = height;

        let _idxX;
        if (idx !== undefined) {
//          _idxX = idx * xScale;
            // Find the corresponding time
            for (let x = 0; x < data.curve.length; x++) {
                if (data.curve[x].getX() > idx) {
                    _idxX = x * xScale;
                    break;
                }
            }
        }

        document.getElementById(displayCanvasName).title = "Tide Curve for " + data.station;

        let gridXStep = Math.round(data.curve.length / 10);
        let gridYStep = (maxy - miny) < 5 ? 1 : Math.round((maxy - miny) / 5);

        // Sort the tuples (on X, time)
//   data.sort(sortTupleX);

        // Cleanup
        context.fillStyle = "white";
        context.fillRect(0, 0, width, height);

        // Background
        if (graphColorConfig.withBGGradient === false) {
            context.fillStyle = graphColorConfig.bgColorNoGradient;
            context.fillRect(0, 0, width, height);
        } else {
            let grV = context.createLinearGradient(0, 0, 0, height);
            grV.addColorStop(0, graphColorConfig.bgGradientFrom);
            grV.addColorStop(1, graphColorConfig.bgGradientTo);

            context.fillStyle = grV;
            context.fillRect(0, 0, width, height);
        }
        // Horizontal grid (Data Unit)
        for (let i = Math.round(miny); gridYStep > 0 && i < maxy; i += gridYStep) {
            context.beginPath();
            context.lineWidth = 1;
            context.strokeStyle = graphColorConfig.horizontalGridColor;
            context.moveTo(0, height - (i - miny) * yScale);
            context.lineTo(width, height - (i - miny) * yScale);
            context.stroke();

            context.save();
            context.font = "bold 10px " + graphColorConfig.font;
            context.fillStyle = graphColorConfig.horizontalGridTextColor;
            let str = i.toString() + " " + gData.unit;
            let len = context.measureText(str).width;
            context.fillText(str, width - (len + 2), height - ((i - miny) * yScale) - 2);
            context.restore();
            context.closePath();
        }

        // Vertical grid (index)
        for (let i = gridXStep; i < data.curve.length; i += gridXStep) {
            context.beginPath();
            context.lineWidth = 1;
            context.strokeStyle = graphColorConfig.verticalGridColor;
            context.moveTo(i * xScale, 0);
            context.lineTo(i * xScale, height);
            context.stroke();

            // Rotate the whole context, and then write on it (that's why we need the translate)
            context.save();
            context.translate(i * xScale, height);
            context.rotate(-Math.PI / 2);
            context.font = "bold 10px " + graphColorConfig.font;
            context.fillStyle = graphColorConfig.verticalGridTextColor;

//          let str = new Date(parseInt(data.curve[i].getX())).format('H:i X'); // i.toString();
            let str = data.curve[i].getY().time + " " + data.curve[i].getY().tz; // i.toString();
            let len = context.measureText(str).width;
            context.fillText(str, 2, -1); //i * xScale, cHeight - (len));
            context.restore();
            context.closePath();
        }
        // Base Height
        if (data.base !== undefined) {
            context.beginPath();
            context.lineWidth = 2;
            context.strokeStyle = "blue";
            context.moveTo(0, height - (data.base - miny) * yScale);
            context.lineTo(width, height - (data.base - miny) * yScale);
            context.stroke();
        }
        // Main curve
        if (data.curve.length > 0) {
            context.beginPath();
            context.lineWidth = 3; // For the main curve
            context.strokeStyle = "red"; // graphColorConfig.rawDataLineColor;

            let previousPoint = data.curve[0];
            context.moveTo((0 - minx) * xScale, height - (data.curve[0].getY().wh - miny) * yScale);
            for (let i = 1; i < data.curve.length; i++) {
                //  context.moveTo((previousPoint.getX() - minx) * xScale, cHeight - (previousPoint.getY() - miny) * yScale);
                context.lineTo((i - minx) * xScale, height - (data.curve[i].getY().wh - miny) * yScale);
                //  context.stroke();
                previousPoint = data.curve[i];
            }
            if (graphColorConfig.fillRawData === true) {
                context.lineTo(width, height);
                context.lineTo(0, height);
                context.closePath();
            }
            context.stroke();
            if (graphColorConfig.fillRawData === true) {
                context.fillStyle = graphColorConfig.rawDataFillColor;
                context.fill();
            }
        }

        /*
         * Current date, usually
         */
        if (idx !== undefined) {
            context.beginPath();
            context.lineWidth = 3;
            context.strokeStyle = graphColorConfig.clickedIndexColor;
            context.moveTo(_idxX, 0);
            context.lineTo(_idxX, height);
            context.stroke();
            context.closePath();
        }
        /*
         * Harmonic curves?
         */
        if (data.harmonics !== undefined) {
//	  console.log("Plotting Harmonics: " + data.harmonics.length + " curve(s)");
            context.lineWidth = 1;
            context.strokeStyle = "black";

            if (harmonicColors.length === 0) {
                initHarmonicColors();
            }
            for (let i = 0; i < data.harmonics.length; i++) {
                context.strokeStyle = harmonicColors[i];
//		        console.log("plotting " + data.harmonics[i].name);
                let tupleArray = data.harmonics[i].data;
                context.beginPath();
                context.moveTo((0 - minx) * xScale, height - (tupleArray[0].getY() - miny) * yScale);
                for (let idx = 1; idx < tupleArray.length; idx++) {
                    let x = idx; // tupleArray[idx].getX();
                    let y = tupleArray[idx].getY();
                    context.lineTo((idx - minx) * xScale, height - (y - miny) * yScale);
                }
                context.stroke();
                context.closePath();
            }
        }

        /*
         * Daylight?
         */
        if (sunRiseSet !== undefined) {
//          console.log("Drawing daylight.");
            let previousX = 0;
            let lastOne = 0;
            console.log(`>> ${Object.keys(sunRiseSet).length} key(s)`);
            for (key in sunRiseSet) { // Loop, useful when more than one day
                console.log("At " + key + ", ", new Date(parseInt(key)));
                let riseTime = sunRiseSet[key].riseTime;
                let setTime = sunRiseSet[key].setTime;

                if (true) {
                    console.log("Rise %s (%d), Set %s (%d)", new Date(riseTime), riseTime, new Date(setTime), setTime);
                    console.log("Curve data from %s (%d) to %s (%d)",
                        new Date(parseInt(data.curve[0].getX())),
                        parseInt(data.curve[0].getX()),
                        new Date(parseInt(data.curve[data.curve.length - 1].getX())),
                        parseInt(data.curve[data.curve.length - 1].getX()));
                }
                if (riseTime !== undefined) {
                    // Find the corresponding time
                    for (let x = lastOne; x < data.curve.length; x++) {
                        if (data.curve[x].getX() > riseTime) {
                            console.log(`Found sun rise at label ${x} (${new Date(riseTime)}), looking for ${riseTime}, found at ${data.curve[x].getX()} (delta ${data.curve[x].getX() - riseTime})`);
                            lastOne = x;
                            riseX = x * xScale;

                            context.beginPath();
                            context.lineWidth = 1;
                            context.strokeStyle = "gray"; // graphColorConfig.clickedIndexColor;
                            context.moveTo(riseX, 0);
                            context.lineTo(riseX, height);
                            context.stroke();
                            // context.closePath();
                            // Draw night previousX to riseX
                            let grV = context.createLinearGradient(previousX, 0, riseX, height);
                            grV.addColorStop(0, 'rgba(169, 169, 169, 0.5)'); // graphColorConfig.bgGradientFrom);
                            grV.addColorStop(1, 'rgba(211, 211, 211, 0.5)');  // graphColorConfig.bgGradientTo);

                            context.fillStyle = grV;
                            console.log(`1 - Daylight: filling rect from ${previousX} to ${riseX - previousX}`);
                            if (true || (riseX - previousX) > previousX) {
                                context.fillRect(previousX, 0, (riseX - previousX), height); // FIXME, (riseX - previousX) can be negative...
                            }
                            context.closePath();
                            previousX = -1;
                            break;
                        }
                    }
                }

                if (setTime !== undefined) {
                    // Find the corresponding time
                    for (let x = lastOne; x < data.curve.length; x++) {
                        if (data.curve[x].getX() > setTime) {
                            console.log(`Found sun set at label ${x} (${new Date(setTime)})`);
                            lastOne = x;
                            let setX = x * xScale;
                            context.beginPath();
                            context.lineWidth = 1;
                            context.strokeStyle = "gray"; // graphColorConfig.clickedIndexColor;
                            context.moveTo(setX, 0);
                            context.lineTo(setX, height);
                            context.stroke();
                            context.closePath();
                            previousX = setX;
                            break;
                        }
                    }
                }
            }
            if (previousX !== -1) { // Last night part.
                // Draw night from previousX to width
                let grV = context.createLinearGradient(previousX, 0, width, height);
                grV.addColorStop(0, 'rgba(169, 169, 169, 0.5)'); // graphColorConfig.bgGradientFrom);
                grV.addColorStop(1, 'rgba(211, 211, 211, 0.5)');  // graphColorConfig.bgGradientTo);

                context.beginPath();
                context.fillStyle = grV;
                console.log(`2- Daylight: filling rect from ${previousX} to ${width}`);
                // if () ...
                context.fillRect(previousX, 0, width, height);
                context.closePath();
            }
        }

        /*
         * Sun and Moon altitudes (and more)
         */
        if (altitudes !== undefined) {
            // console.log("Altitudes! %d elements", altitudes.length);
            // let min = 0, max = 0;
            // altitudes.forEach((el, idx) => {
            // 	  min = Math.min(min, el.sunAlt);
            //    max = Math.max(max, el.sunAlt);
            // });
            // console.log("Min: %f, Max: %f", min, max);
            context.lineWidth = 1;
            context.strokeStyle = "black";

            // Zero = base height: height - (data.base - miny) * yScale
            let zero = height - (data.base - miny) * yScale;
            // context.beginPath();
            // context.moveTo(0, zero);
            // context.lineTo(width, zero);
            // context.stroke();
            // context.closePath();

            // Sun
            context.beginPath();
            context.moveTo((0 - minx) * xScale, zero - ((height / 2) * (altitudes[0].sunAlt / 90)));
            altitudes.forEach((el, idx) => {
                let alt = el.sunAlt;
                let _y = (height / 2) * (alt / 90);
                context.lineTo((idx - minx) * xScale, zero - _y);
                if (_idxX !== undefined) {
                    if (((idx - minx) * xScale).toFixed(0) === _idxX.toFixed(0)) {
                        //		  	      console.log("Plotting the Sun");
                        let img = document.getElementById("sun-png");
                        context.drawImage(img, _idxX, zero - _y - 12); //, 150, 180);
                    }
                }
            });
            context.stroke();
            context.closePath();

            // Moon
            context.beginPath();
            context.moveTo((0 - minx) * xScale, zero - ((height / 2) * (altitudes[0].moonAlt / 90)));
            let phase;
            altitudes.forEach((el, idx) => {
                let alt = el.moonAlt;
                let _y = (height / 2) * (alt / 90);
                context.lineTo((idx - minx) * xScale, zero - _y);
                if (_idxX !== undefined) {
                    if (((idx - minx) * xScale).toFixed(0) === _idxX.toFixed(0)) {
                   //	console.log("Plotting the Moon");
                        let img = document.getElementById("moon-png");
                        context.drawImage(img, _idxX, zero - _y - 12); //, 150, 180);
                    }
                }
                // Moon phases?
                phase = Math.round(el.moonPhase / (360 / 28)) + 1;
                if (phase > 28) phase = 28;
                if (phase < 1) phase = 1;
            });
            context.stroke();
            context.closePath();
            if (phase !== undefined) {
                let pixName = "phase-" + (phase < 10 ? "0" : "") + phase.toFixed(0);
                let moonPix = document.getElementById(pixName);
                context.drawImage(moonPix, width - 60, 10);
            }
        }
        if (table !== undefined) {
            // Display table
            context.beginPath();
            context.save();
            context.font = "bold 20px Courier New"; // + graphColorConfig.font;
            context.fillStyle = 'navy'; // graphColorConfig.horizontalGridTextColor;
            context.fillText(gData.station, 20, 30);
            table.forEach((line, idx) => {
                let str = line.type + " : " + line.formattedDate + ", " + line.value.toFixed(2) + " " + line.unit;
                context.fillText(str, 20, 30 + ((idx + 1) * 21));
            });
            context.restore();
            context.closePath();

        }
    };

    this.init = (dataArray) => {
        if (dataArray !== undefined && dataArray.length > 0) {
            let minMax = this.getMinMax(dataArray);
            miny = minMax.mini;
            maxy = minMax.maxi;

            minx = 0; // instance.minX(dataArray);
            maxx = dataArray.length - 1; //instance.maxX(dataArray);

            if (maxx !== minx) {
                xScale = canvas.getContext('2d').canvas.clientWidth / (maxx - minx);
            }
            if (maxy !== miny) {
                yScale = canvas.getContext('2d').canvas.clientHeight / (maxy - miny);
            }
        }
    };

    (() => {
        instance.init(graphData.curve);
        instance.drawGraph(cName, graphData);
    })(); // Invoked automatically when 'new' is invoked.
};

function Tuple(_x, _y) {
    let x = _x;
    let y = _y;

    this.getX = () => {
        return x;
    };
    this.getY = () => {
        return y;
    };
}

let sortTupleX = (t1, t2) => {
    if (t1.getX() < t2.getX()) {
        return -1;
    }
    if (t1.getX() > t2.getX()) {
        return 1;
    }
    return 0;
};

let intRange = (fromInclusive, toExclusive) => {
    let array = [];
    for (let i = fromInclusive; i < toExclusive; i++) {
        array.push(i);
    }
    return array;
};

let rndColor = () => {
    let r = Math.floor(Math.random() * 255);
    let g = Math.floor(Math.random() * 255);
    let b = Math.floor(Math.random() * 255);
    let R = r.toString(16);
    if (R.length < 2) {
        R = '0' + R;
    }
    let G = g.toString(16);
    if (G.length < 2) {
        G = '0' + G;
    }
    let B = b.toString(16);
    if (B.length < 2) {
        B = '0' + B;
    }
    let color = "#" + R + G + B;

    return color;
};
