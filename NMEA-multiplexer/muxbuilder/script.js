//

// Channels descriptions
const CHANNEL_DESCRIPTION = {
    'serial': "Read NMEA from a Serial port.\nProvide port name\nand baud rate.\nExtra parameters available.",
    'file': "Replays NMEA Data\nfrom a log file.\nCan be an entry\nof a zip file.",
    'tcp': "Reads NMEA Data\nfrom a TCP server.",
    'rnd':   "Random number generation\n(for Debugging purpose).",
    'zda':   "Generates a ZDA\nsentence from the server's\nsystem time.",
    'bme280': "Temperature\nHumidity\nPressure sensor.",
    'bmp180': "Temperature\nHumidity\nPressure sensor.",
    'htu21df': "Temperature and\nHumidity sensor.",
    'ws': "Read NMEA Data\nfrom a WebSocket\nserver.",
    'lsm303': "Magnetometer and\naccelerometer sensor.",
    'hmc5883l': "Magnetometer sensor."
};

const FORWARDER_DESCRIPTION = {
    'serial': "Forward received NMEA Data\non a serial port.",
    'tcp': "Act as a TCP server.\nAll received NMEA data\nare forwarded\non the given port\non the current machine.",
    'file': "Log all NMEA data.\nIn a single file\nor in several files,\nwith their names based on UTC time,\nin a given folder,\nreset on a regural basis.",
    'ws': "Web Socket",
    'mqtt': "MQTT, Work in Progress",
    'wsp': "Web Socket Processor.\nDo look in the code!\nIt may require customization!",
    'rest': "Will POST received data\nto the provided REST\nendpoint.",
    'gpsd': "Spits all received\nNMEA Data\nin a GPSD format.",
    'rmi': "RMI server",
    'console': "Spits out all received\nNMEA Data\non STDOUT."
};

function showDiv(divId) {
	let elmt = document.getElementById(divId);

	elmt.classList.toggle('visible-div');

	let newH = '100%';
	let newV = 'visible';
	let newO = '1';

	elmt.style.height = newH;
	elmt.style.visibility = newV;
	elmt.style.opacity = newO;
}

function hideDiv(divId) {
	let elmt = document.getElementById(divId);

	elmt.classList.toggle('visible-div');

	let newH = '0';
	let newV = 'hidden';
	let newO = '0';
	elmt.style.height = newH;
	elmt.style.visibility = newV;
	elmt.style.opacity = newO;
}

let showHTTP = false;
function expandCollapseHTTP(cb) {
    let divId = 'http-specific';
    showHTTP = !showHTTP;
    if (showHTTP) {
        showDiv(divId);
    } else {
        hideDiv(divId);
    }
}

function addChannel() {
    let channelList = document.getElementById('channel-list');
    let oneChannelDefinition = document.getElementById('add-channel');
    let channelLength = channelList.children.length;
    console.log(`${channelLength} Children (channels)`);
    let newChannel = oneChannelDefinition.cloneNode(true); // true: deep!
    newChannel.id = `add-channel-${channelLength.toFixed(0)}`;
    newChannel.style.display = 'block';
    channelList.appendChild(newChannel);
    // Set the channel parameters
    setChannelParameters('serial', // TODO Get the value from the list
                         newChannel.querySelector('table').querySelector('tr').querySelector('select'));
}

function addForwarder() {
    let forwarderList = document.getElementById('forwarder-list');
    let oneForwarderDefinition = document.getElementById('add-forwarder');
    let listLength = forwarderList.children.length;
    let newForwarder = oneForwarderDefinition.cloneNode(true); // true: deep!
    newForwarder.id = `add-forwarder-${listLength.toFixed(0)}`;
    newForwarder.style.display = 'block';
    forwarderList.appendChild(newForwarder);
    // Set the forwarder parameters
    setForwarderParameters('serial', // TODO Get the value from the list
                           newForwarder.querySelector('table').querySelector('tr').querySelector('select'));
}

function addComputer() {
    console.log("Will add computer");
}

function removeChannel(button) {
    console.log("Removing channel...");
    let channelList = document.getElementById('channel-list');
    let channelDiv = button;
    while (!(channelDiv instanceof HTMLDivElement)) {
        channelDiv = channelDiv.parentElement;
    }
    channelList.removeChild(channelDiv);
}

function removeForwarder(button) {
    console.log("Removing forwarder...");
    let forwarderList = document.getElementById('forwarder-list');
    let forwarderDiv = button;
    while (!(forwarderDiv instanceof HTMLDivElement)) {
        forwarderDiv = forwarderDiv.parentElement;
    }
    forwarderList.removeChild(forwarderDiv);
}
/**
 * channel: the value of the channel ('serial', 'file', etc)
 * element: the <select> element
 */
function setChannelParameters(channel, element) {
    console.log(`Setting parameters for ${channel}`);
    let divId = null;
    switch (channel) {
      case 'serial':
        divId = 'serial-channel-parameters';
        break;
      case 'file':
        divId = 'file-channel-parameters';
        break;
      case 'tcp':
        divId = 'tcp-channel-parameters';
        break;
      case 'rnd':  
      case 'zda':  
        // No parameter, just verbose
        divId = 'no-prm-channel-parameters';
        break;
      case 'bme280':
      case 'bmp180':
      case 'htu21df':
          divId = 'device-prefix-channel-parameters';
          break;
      case 'ws':
          divId = 'ws-channel-parameters';
          break;
      case 'lsm303':
          divId = 'lsm303-channel-parameters';
          break;
      case 'hmc5883l':
          divId = 'hcm5883l-channel-parameters';
          break;
      default:
        divId = 'generic-channel-parameters';
        break;
    }
    if (divId !== null) {
        let prmElements = document.getElementById(divId);
        let newPrmElement = prmElements.cloneNode(true);
        newPrmElement.style.display = 'block';
        let td = element.parentElement.parentElement.children[2];
        while (td.firstChild) {
            td.removeChild(td.firstChild)
        }
        td.appendChild(newPrmElement);
        // Description ?
        let descField = td.parentElement.querySelector('.channel-descr');
        while (descField.firstChild) {
            descField.removeChild(descField.firstChild);
        }
        descField.innerHTML = `<pre>${CHANNEL_DESCRIPTION[channel]}</pre>`;
    } else {
        console.log(`No Div ID for ${channel}`);
    }
}

function setForwarderParameters(forwarder, element) {
    console.log(`Setting parameters for ${forwarder}`);
    let divId = null;
    switch (forwarder) {
      case 'serial':
          divId = 'serial-fwd-parameters';
          break;
      case 'tcp':
      // case 'udp':
          divId = 'tcp-fwd-parameters';
          break;
      case 'file':
          divId = 'file-fwd-parameters';
          break;
      case 'ws':
      case 'wsp': // May require customization!
          divId = 'ws-fwd-parameters';
          break;
      // case 'mqtt':
      case 'rest':
          divId = 'rest-fwd-parameters';
          break;
      case 'gpsd':
          divId = 'gpsd-fwd-parameters';
          break;
      case 'console':
          // No parameters
          divId = 'console-fwd-parameters';
          break;
      case 'rmi':
          divId = 'rmi-fwd-parameters';
          break;
      default:
        divId = 'generic-forwarder-parameters';
        break;
    }
    if (divId !== null) {
        let prmElements = document.getElementById(divId);
        let newPrmElement = prmElements.cloneNode(true);
        newPrmElement.style.display = 'block';
        let td = element.parentElement.parentElement.children[2];
        while (td.firstChild) { // Remove existing content
            td.removeChild(td.firstChild)
        }
        td.appendChild(newPrmElement);
        // Description ?
        let descField = td.parentElement.querySelector('.forwarder-descr');
        while (descField.firstChild) {
            descField.removeChild(descField.firstChild);
        }
        descField.innerHTML = `<pre>${FORWARDER_DESCRIPTION[forwarder]}</pre>`;
        // Specificities?
        if (forwarder === 'file') { // file specific option
            let specificFileOptionOne = document.getElementById('file-fwd-option-one');
            if (specificFileOptionOne !== null) {
                let newOptionElement = specificFileOptionOne.cloneNode(true);
                newOptionElement.style.display = 'block';
                let optionLocation = newPrmElement.querySelector('.file-fwd-prms');
                while (optionLocation.firstChild) { // Remove what's in there
                    optionLocation.removeChild(optionLocation.firstChild)
                }
                optionLocation.appendChild(newOptionElement);
                console.log('Done');
            }
        }
    } else {
        console.log(`No Div ID for ${forwarder}`);
    }
}

function getSerialChannelCode(node) {
    let code = "";
    let portName = node.querySelector('.port-name').value;
    code += `    port: ${portName}\n`;
    let baudRate = node.querySelector('.baud-rate').value;
    code += `    baud.rate: ${baudRate}\n`;
    let verbose = node.querySelector('.verbose').checked;
    code += `    verbose: ${verbose}\n`;
    // filters
    let deviceFilters = node.querySelector('.device-filter').value;
    if (deviceFilters.trim().length > 0) {
        code += `    device.filters: ${deviceFilters}\n`;
    }
    let sentenceFilters = node.querySelector('.sentence-filter').value;
    if (sentenceFilters.trim().length > 0) {
        code += `    sentence.filters: ${senteminceFilters}\n`;
    }
    let resetInterval = node.querySelector('.reset-interval').value;
    if (resetInterval.trim().length > 0) {
        code += `    reset.interval: ${resetInterval}\n`;
    }
    return code;
}

function getSerialFwdlCode(node) {
    let code = "";
    let portName = node.querySelector('.port-name').value;
    code += `    port: ${portName}\n`;
    let baudRate = node.querySelector('.baud-rate').value;
    code += `    baud.rate: ${baudRate}\n`;
    return code;
}

function getTCPChannelCode(node) {
    let code = "";
    let serverName = node.querySelector('.server-name').value;
    code += `    server: ${serverName}\n`;
    let baudRate = node.querySelector('.port-num').value;
    code += `    port: ${baudRate}\n`;
    let verbose = node.querySelector('.verbose').checked;
    code += `    verbose: ${verbose}\n`;
    // filters
    let deviceFilters = node.querySelector('.device-filter').value;
    if (deviceFilters.trim().length > 0) {
        code += `    device.filters: ${deviceFilters}\n`;
    }
    let sentenceFilters = node.querySelector('.sentence-filter').value;
    if (sentenceFilters.trim().length > 0) {
        code += `    sentence.filters: ${sentenceFilters}\n`;
    }
    return code;
}

function getTCPTwdCode(node) {
    let code = "";
    let baudRate = node.querySelector('.port-num').value;
    code += `    port: ${baudRate}\n`;
    return code;
}

function getFileFwdCode(node) {
    let code = "";
    let option = node.querySelector('.file-fwd-option').value;
    if (option === 'one-file') {
        code += "    timebase.filename: false\n";
        let fName = node.querySelector('.logfile-name').value;
        code += `    filename: ${fName}\n`;
        let append = node.querySelector('.append').checked;
        code += `    append: ${append}\n`;
    } else if (option === 'time-based') {
        code += "    timebase.filename: true\n";
        let suffix = node.querySelector('.logfile-suffix').value;
        code += `    filename.suffix: ${suffix}\n`;
        let dir = node.querySelector('.logfile-dir').value;
        code += `    log.dir: ${dir}\n`;
        let split = node.querySelector('.log-file-split').value;
        code += `    split: ${split}\n`;
    }
    let flush = node.querySelector('.flush').checked;
    code += `    flush: ${flush}\n`;
    return code;
}

function getRESTFwdCode(node) {
    let code = "";
    let serverName = node.querySelector('.server-name').value;
    code += `    server.name: ${serverName}\n`;
    let serverPort = node.querySelector('.server-port').value;
    code += `    server.port: ${serverPort}\n`;
    let restResource = node.querySelector('.server-resource').value;
    code += `    rest.resource: ${restResource}\n`;
    let restVerb = node.querySelector('.rest-verb').value;
    code += `    rest.verb: ${restVerb}\n`;
    let restProtocol = node.querySelector('.rest-protocol').value;
    code += `    rest.protocol: ${restProtocol}\n`;
    let restHeaders = node.querySelector('.rest-headers').value;
    code += `    http.headers: ${restHeaders}\n`;
    return code;
}

function getWSFwdCode(node) {
    let code = "";
    let wsUri = node.querySelector('.ws-uri').value;
    code += `    wsuri: ${wsUri}\n`;
    return code;
}

function getGPSDFwdCode(node) {
    let code = "";
    let serverPort = node.querySelector('.port-num').value;
    code += `    port: ${serverPort}\n`;
    return code;
}

function getRMIFwdCode(node) {
    let code = "";
    let serverPort = node.querySelector('.port-num').value;
    code += `    port: ${serverPort}\n`;
    let serverName = node.querySelector('.rmi-name').value;
    code += `    name: ${serverName}\n`;
    return code;
}

function getFileChannelCode(node) {
    let code = "";
    let fileName = node.querySelector('.file-name').value;
    code += `    filename: ${fileName}\n`;
    let zipOption = node.querySelector('.zip-option').checked;
    code += `    zip: ${zipOption}\n`;

    let zipPath = node.querySelector('.zip-path').value;
    if (zipOption && zipPath.trim().length > 0) {
        code += `    path.in.zip: ${zipPath}\n`;
    }
    let betweenRecords = node.querySelector('.between-records').value;
    if (betweenRecords.trim().length > 0) {
        code += `    between-records: ${betweenRecords}\n`;
    }
    let loopOption = node.querySelector('.loop').checked;
    code += `    loop: ${loopOption}\n`;

    let verbose = node.querySelector('.verbose').checked;
    code += `    verbose: ${verbose}\n`;
    // filters
    let deviceFilters = node.querySelector('.device-filter').value;
    if (deviceFilters.trim().length > 0) {
        code += `    device.filters: ${deviceFilters}\n`;
    }
    let sentenceFilters = node.querySelector('.sentence-filter').value;
    if (sentenceFilters.trim().length > 0) {
        code += `    sentence.filters: ${sentenceFilters}\n`;
    }
    return code;
}

function getNoPrmChannelCode(node) {
    let code = "";
    let verbose = node.querySelector('.verbose').checked;
    code += `    verbose: ${verbose}\n`;

    return code;
}

function getCodeDevicePrefix(node) {
    let code = "";

    let devicePrefix = node.querySelector('.device-prefix').value;
    code += `    device.prefix: ${devicePrefix}\n`;

    let verbose = node.querySelector('.verbose').checked;
    code += `    verbose: ${verbose}\n`;

    return code;
}

function getWSChannelCode(node) {
    let code = "";
    let wsUrl = node.querySelector('.ws-url').value;
    code += `    wsuri: ${wsUrl}\n`;
    let verbose = node.querySelector('.verbose').checked;
    code += `    verbose: ${verbose}\n`;
    // filters
    let deviceFilters = node.querySelector('.device-filter').value;
    if (deviceFilters.trim().length > 0) {
        code += `    device.filters: ${deviceFilters}\n`;
    }
    let sentenceFilters = node.querySelector('.sentence-filter').value;
    if (sentenceFilters.trim().length > 0) {
        code += `    sentence.filters: ${sentenceFilters}\n`;
    }
    return code;
}

function getLSM303hannelCode(node) {
    let code = "";
    let devicePrefix = node.querySelector('.device-prefix').value;
    code += `    device.prefix: ${devicePrefix}\n`;
    let verbose = node.querySelector('.verbose').checked;
    code += `    verbose: ${verbose}\n`;
    let headingOffset = node.querySelector('.heading-offset').value;
    code += `    heading.offset: ${headingOffset}\n`;
    let dampingSize = node.querySelector('.damping-size').value;
    code += `    damping.size: ${dampingSize}\n`;
    let readFrequency = node.querySelector('.read-frequency').value;
    code += `    read.frequency: ${readFrequency}\n`;
    let feature = node.querySelector('.lsm-303-feature').value;
    code += `    feature: ${feature}\n`;
    let calProps = node.querySelector('.calibration-properties').value;
    code += `    lsm303.cal.prop.file: ${calProps}\n`;

    return code;
}

function getHCM5883LhannelCode(node) {
    let code = "";
    let devicePrefix = node.querySelector('.device-prefix').value;
    code += `    device.prefix: ${devicePrefix}\n`;
    let verbose = node.querySelector('.verbose').checked;
    code += `    verbose: ${verbose}\n`;
    let headingOffset = node.querySelector('.heading-offset').value;
    code += `    heading.offset: ${headingOffset}\n`;
    let dampingSize = node.querySelector('.damping-size').value;
    code += `    damping.size: ${dampingSize}\n`;
    let readFrequency = node.querySelector('.read-frequency').value;
    code += `    read.frequency: ${readFrequency}\n`;
    let calProps = node.querySelector('.calibration-properties').value;
    code += `    hmc5883l.cal.prop.file: ${calProps}\n`;

    return code;
}

function getConsoleFwdCode(node) {
    return "";
}

function generateTheCode() {
    let code = '';
    let codeElement = document.getElementById('generated-yaml');
    code += "#\n";
    code += `# Generated on ${new Date()}\n`;
    code += "#\n";
    code += `name: \"${ document.getElementById('mux-title').value }\"\n`;
    code += "context:\n";
    code += `  with.http.server: ${ document.getElementById('with-http').checked }\n`;
    if (document.getElementById('with-http').checked) {
        code += `  http.port: ${ document.getElementById('http-port').value }\n`;
        code += `  init.cache: ${ document.getElementById('init-cache').checked }\n`;
    }
    code += `  default.declination: ${ document.getElementById('default-decl').value }\n`;
    let devFileName = document.getElementById('dev-file-name').value;
    if (devFileName.trim().length > 0) {
        code += `  deviation.file.name: ${ document.getElementById('dev-file-name').value }\n`;
    }
    code += `  max.leeway: ${ document.getElementById('max-leeway').value }\n`;
    code += `  bsp.factor: ${ document.getElementById('bsp-factor').value }\n`;
    code += `  aws.factor: ${ document.getElementById('aws-factor').value }\n`;
    code += `  hdg.offset: ${ document.getElementById('hdg-offset').value }\n`;
    code += `  awa.offset: ${ document.getElementById('awa-offset').value }\n`;
    code += `  damping: ${ document.getElementById('damping').value }\n`;

    // Input Channels
    let channelList = document.getElementById('channel-list');
    code += `# ${channelList.children.length === 0 ? 'No' : channelList.children.length} Channel${channelList.children.length > 1 ? 's' : ''}\n`;
    if (channelList.children.length > 0) {
        code += "channels:\n";
        for (let i=0; i<channelList.childElementCount; i++) {
            let channel = channelList.children[i];
            let channelType = channel.querySelectorAll('select')[0].value;
            // console.log(`Adding channel ${channelType}`);
            code += `  - type: ${channelType}\n`;
            switch (channelType) {
                case 'serial':
                    code += getSerialChannelCode(channel);
                    break;
                case 'tcp':
                    code += getTCPChannelCode(channel);
                    break;
                case 'file':
                    code += getFileChannelCode(channel);
                    break;
                case 'bme280':
                case 'bmp180':
                case 'htu21df':
                    code += getCodeDevicePrefix(channel);
                    break;
                case 'ws':
                    code += getWSChannelCode(channel);
                    break;
                case 'lsm303':
                    code += getLSM303hannelCode(channel);
                    break;
                case 'zda':
                case 'rnd':
                    code += getNoPrmChannelCode(channel);
                    break;
                case 'hmc5883l':
                    code += getHCM5883LhannelCode(channel);
                    break;
                default:
                    code += "    # Not managed...\n";
                    break;
            }
        }
    }

    // Forwarders
    let fwdList = document.getElementById('forwarder-list');
    code += `# ${fwdList.children.length === 0 ? 'No' : fwdList.children.length} Forwarder${fwdList.children.length > 1 ? 's' : ''}\n`;
    if (fwdList.children.length > 0) {
        code += "forwarders:\n";
        for (let i=0; i<fwdList.childElementCount; i++) {
            let fwd = fwdList.children[i];
            let fwdType = fwd.querySelectorAll('select')[0].value;
            // console.log(`Adding channel ${channelType}`);
            code += `  - type: ${fwdType}\n`;
            switch (fwdType) {
                case 'console':
                    code += getConsoleFwdCode(fwd);
                    break;
                case 'serial':
                    code += getSerialFwdlCode(fwd);
                    break;
                case 'tcp':
                    code += getTCPTwdCode(fwd);
                    break;
                case 'file':
                    code += getFileFwdCode(fwd);
                    break;
                case 'ws':
                case 'wsp':
                    code += getWSFwdCode(fwd);
                    break;
                case 'rest':
                    code += getRESTFwdCode(fwd);
                    break;
                case 'gpsd':
                    code += getGPSDFwdCode(fwd);
                    break;
                case 'rmi':
                    code += getRMIFwdCode(fwd);
                    break;
                default:
                    code += '    # coming\n'; 
                    break;
            }
        }
    }

    // Computers
    code += "# Computers\n";


    // Good to go.
    codeElement.innerText = code;
    showGeneratedDialog();
}

function showGeneratedDialog() {
    let codeDialog = document.getElementById("generated-code-dialog");
    codeDialog.show();
}
function closeGeneratedDialog() {
    let codeDialog = document.getElementById("generated-code-dialog");
    codeDialog.close();    
}
