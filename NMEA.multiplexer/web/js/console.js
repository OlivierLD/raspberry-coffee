/*
 * @author Olivier Le Diouris
 */
let displayBSP, displayLog, displayTWD, displayTWS, thermometer, athermometer, displayHDG, rose,
    displayBaro, displayHum, displayDate, displayTime, displayOverview,
    jumboBSP, jumboHDG, jumboTWD, jumboLWY, jumboAWA, jumboTWA, jumboAWS, jumboTWS, jumboCOG, jumboCDR, jumboSOG, jumboCSP, jumboVMG,
    displayAW, displayCurrent,
    twdEvolution, twsEvolution;

let jumboList = [];

let editing = false;

function init() {
    displayBSP = new AnalogDisplay('bspCanvas', 100, 15, 5, 1);
    displayLog = new NumericDisplay('logCanvas', 60, 5);

    displayHDG = new Direction('hdgCanvas', 100, 45, 5, true);
    displayTWD = new Direction('twdCanvas', 100, 45, 5, true);
    displayTWS = new AnalogDisplay('twsCanvas', 100, 50, 10, 1, true, 40);
    thermometer = new Thermometer('tmpCanvas', 200);
    athermometer = new Thermometer('atmpCanvas', 200);
    rose = new CompassRose('roseCanvas', 400, 50);
    displayDate = new DateDisplay('dateCanvas', 60);
    displayTime = new TimeDisplay('timeCanvas', 60);
    displayBaro = new AnalogDisplay('baroCanvas', 100, 1040, 10, 1, true, 40, 980);
    displayHum = new AnalogDisplay('humCanvas', 100, 100, 10, 1, true, 40);

    displayOverview = new BoatOverview('overviewCanvas');

    jumboBSP = new JumboDisplay('jumboBSPCanvas', 'BSP', 120, 60, "0.00");
    jumboHDG = new JumboDisplay('jumboHDGCanvas', 'HDG', 120, 60, "000");
    jumboTWD = new JumboDisplay('jumboTWDCanvas', 'TWD', 120, 60, "000", 'cyan');
    jumboLWY = new JumboDisplay('jumboLWYCanvas', 'LWY', 120, 60, "000", 'red');
    jumboAWA = new JumboDisplay('jumboAWACanvas', 'AWA', 120, 60, "000");
    jumboTWA = new JumboDisplay('jumboTWACanvas', 'TWA', 120, 60, "000", 'cyan');
    jumboAWS = new JumboDisplay('jumboAWSCanvas', 'AWS', 120, 60, "00.0");
    jumboTWS = new JumboDisplay('jumboTWSCanvas', 'TWS', 120, 60, "00.0", 'cyan');
    jumboCOG = new JumboDisplay('jumboCOGCanvas', 'COG', 120, 60, "000");
    jumboCDR = new JumboDisplay('jumboCDRCanvas', 'CDR', 120, 60, "000", 'cyan');
    jumboSOG = new JumboDisplay('jumboSOGCanvas', 'SOG', 120, 60, "0.00");
    jumboCSP = new JumboDisplay('jumboCSPCanvas', 'CSP', 120, 60, "00.0", 'cyan');
    jumboVMG = new JumboDisplay('jumboVMGCanvas', 'VMG', 120, 60, "0.00", 'yellow');

    jumboList = [jumboBSP, jumboHDG, jumboTWD, jumboLWY, jumboAWA, jumboTWA, jumboAWS, jumboTWS, jumboCOG, jumboCDR, jumboSOG, jumboCSP, jumboVMG];

    displayAW = new AWDisplay('awDisplayCanvas', 80, 45, 5);
    displayCurrent = new CurrentDisplay('currentDisplayCanvas', 80, 45, 5);
    twdEvolution = new TWDEvolution('twdEvolutionCanvas');
    twsEvolution = new TWSEvolution('twsEvolutionCanvas');
}

function changeBorder(b) {
    displayBSP.setBorder(b);
    displayHDG.setBorder(b);
    displayTWD.setBorder(b);
    displayTWS.setBorder(b);
    displayBaro.setBorder(b);
    displayBaro.repaint();
    displayHum.setBorder(b);
    displayHum.repaint();
    displayAW.setBorder(b);
    displayCurrent.setBorder(b);
}

const TOTAL_WIDTH = 1200;

function resizeDisplays(width) {
    if (displayBSP !== undefined && displayTWS !== undefined) { // TODO Other displays
        displayBSP.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH));
        displayTWS.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH));
        displayHDG.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH));
        displayTWD.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH));
        thermometer.setDisplaySize(200 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH));
        athermometer.setDisplaySize(200 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH));
        rose.setDisplaySize(400 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH));
        displayBaro.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH));
        displayHum.setDisplaySize(100 * (Math.min(width, TOTAL_WIDTH) / TOTAL_WIDTH));
        displayOverview.drawGraph();
        twdEvolution.drawGraph();
        twsEvolution.drawGraph();

        let jumboFactor = width / TOTAL_WIDTH;
        for (let i = 0; i < jumboList.length; i++) {
            if (jumboList[i] !== undefined)
                jumboList[i].setDisplaySize(120 * jumboFactor, 60 * jumboFactor);
        }
    }
}

function lpad(str, pad, len) {
    while (str.length < len) {
        str = pad + str;
    }
    return str;
}
