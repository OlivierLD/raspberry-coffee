function allowDrop(ev) {
  let origin =  ev.target.id;
  ev.preventDefault();
}

// When drag starts
function drag(ev) {
  console.log(`Start dragging ${ev.target.id}`);
  ev.dataTransfer.setData("dragged-id", ev.target.id);
}

// When dropped
function drop(ev) {
  ev.preventDefault();
  let data = ev.dataTransfer.getData("dragged-id"); // Id of the dragged Node
  if (data == 'no-drag-1') {
      // Prevent a given Element...
      console.log(`Preventing drop from no-drag-1 to ${ev.target.id}`);
  } else {
      let newNode = document.getElementById(data).cloneNode(true); // Copy
      // show edit-icon and close-icon when dropped
      newNode.querySelector(".edit-icon").style.display = 'inline-block';
      newNode.querySelector(".close-icon").style.display = 'block';
      ev.target.appendChild(newNode);
  }
}

function remove(origin) {
  let draggable = origin;
  while (!draggable.classList.contains("draggable-container")) {
    draggable = draggable.parentElement;
  }
  let dropContainer = draggable;
  while (dropContainer != null && !dropContainer.classList.contains("drop-container")) {
    dropContainer = dropContainer.parentElement;
  }
  if (dropContainer !== null) {
    dropContainer.removeChild(draggable);
  } else {
    alert("Removable from the right pane only...");
  }
}

function edit(origin) {
  let editableDialog = origin.parentElement.querySelector(".editable-prm");
  // Specific case for the file fowarder
  let fileForwarder = editableDialog.querySelector('.file-fwd-prms');
  if (fileForwarder !== null) {
    if (fileForwarder.firstElementChild === null) { // Not initialized yet
      fileFwdOption(editableDialog.querySelector(".file-fwd-option"));
    }
  }
  // End of specific
  editableDialog.showModal();
}

function closeDialog(clicked) {
  let dialog = clicked;
  while (!(dialog instanceof HTMLDialogElement)) { // Find the dialog the close icon belongs to.
    dialog = dialog.parentElement;
  }
  dialog.close();
}

// Show/Hide generated yaml code
function showGeneratedDialog() {
    let codeDialog = document.getElementById("generated-code-dialog");
    codeDialog.show(); // showModal();
}
function closeGeneratedDialog() {
    let codeDialog = document.getElementById("generated-code-dialog");
    codeDialog.close();
}

const TABS = ['one', 'two', 'three'];

function switchTab(evt, tabNum) {
	let tabLinks = document.getElementsByClassName("tablinks");
	for (let i=0; i<tabLinks.length; i++) {
		tabLinks[i].classList.remove("active"); // Reset all tabs
	}
	for (let i=0; i<TABS.length; i++) {
		document.getElementById(TABS[i]).style.display = (i === tabNum) ? 'block' : 'none';
	}
	evt.currentTarget.classList.add("active");
}

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

function generateNoPrmConsumerCode(node) {
  let code = "";
  let verbose = false;
  try {
    verbose = node.querySelector('.verbose').checked;
  } catch (err) {
    // No vervbose
  }
  code += `    verbose: ${verbose}\n`;
  return code;
}

function generateSerialConsumerCode(node) {
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
      code += `    sentence.filters: ${sentenceFilters}\n`;
  }
  let resetInterval = node.querySelector('.reset-interval').value;
  if (resetInterval.trim().length > 0) {
      code += `    reset.interval: ${resetInterval}\n`;
  }
  return code;
}

function generateTCPConsumerCode(node) {
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

function generateFileConsumerCode(node) {
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

function generateCodeDevicePrefix(node) {
  let code = "";

  let devicePrefix = node.querySelector('.device-prefix').value;
  code += `    device.prefix: ${devicePrefix}\n`;

  let verbose = node.querySelector('.verbose').checked;
  code += `    verbose: ${verbose}\n`;

  return code;
}

function generateWSConsumerCode(node) {
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

function generateLSM303ConsumerCode(node) {
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

function generateHCM5883LConsumerCode(node) {
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

function generateSerialFwdCode(node) {
  let code = "";
  let portName = node.querySelector('.port-name').value;
  code += `    port: ${portName}\n`;
  let baudRate = node.querySelector('.baud-rate').value;
  code += `    baud.rate: ${baudRate}\n`;
  return code;
}

function generateTCPFwdCode(node) {
  let code = "";
  let baudRate = node.querySelector('.port-num').value;
  code += `    port: ${baudRate}\n`;
  return code;
}

function generateFileFwdCode(node) {
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

function generateRESTFwdCode(node) {
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

function generateWSFwdCode(node) {
  let code = "";
  let wsUri = node.querySelector('.ws-uri').value;
  code += `    wsuri: ${wsUri}\n`;
  return code;
}

function generateGPSDFwdCode(node) {
  let code = "";
  let serverPort = node.querySelector('.port-num').value;
  code += `    port: ${serverPort}\n`;
  return code;
}

function generateRMIFwdCode(node) {
  let code = "";
  let serverPort = node.querySelector('.port-num').value;
  code += `    port: ${serverPort}\n`;
  let serverName = node.querySelector('.rmi-name').value;
  code += `    name: ${serverName}\n`;
  return code;
}

function generateTWCurrentComputerCode(node) {
  let code = "";
  let devicePrefix = node.querySelector('.device-prefix').value;
  code += `    prefix: ${devicePrefix}\n`;
  let bufferLengths = node.querySelector('.time-buffer-length').value;
  code += `    time.buffer.length: ${bufferLengths}\n`;
  // Other props? like verbose

  return code;
}


function dumpIt() { // YAML Generation

  let code = '';
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


  // Consumers
  let dropId = "drop-div-consumers";
  let dropBox = document.getElementById(dropId);
  code += `# ${dropBox.childElementCount === 0 ? 'No' :dropBox.childElementCount} Channel${dropBox.childElementCount > 1 ? 's' : ''}\n`;
  if (dropBox.childElementCount > 0) {
    code += "channels:\n";
  }
  for (let i=0; i<dropBox.childElementCount; i++) {
    // Look for the type and parameters
    let prms = dropBox.children[i].querySelector(".editable-zone");
    if (prms.classList.contains("serial-channel")) {
      code += "  - type: serial\n";
      code += generateSerialConsumerCode(prms);
    } else if (prms.classList.contains("tcp-channel")) { 
      code += "  - type: tcp\n";
      code += generateTCPConsumerCode(prms);
    } else if (prms.classList.contains("file-channel")) { 
      code += "  - type: file\n";
      code += generateFileConsumerCode(prms);
    } else if (prms.classList.contains("ws-channel")) { 
      code += "  - type: ws\n";
      code += generateWSConsumerCode(prms);
    } else if (prms.classList.contains("htu21df-channel")) { 
      code += "  - type: htu21df\n";
      code += generateCodeDevicePrefix(prms);
    } else if (prms.classList.contains("bme280-channel")) { 
      code += "  - type: bme280\n";
      code += generateCodeDevicePrefix(prms);
    } else if (prms.classList.contains("bmp180-channel")) { 
      code += "  - type: bmp180\n";
      code += generateCodeDevicePrefix(prms);
    } else if (prms.classList.contains("hcm5883l-channel")) { 
      code += "  - type: hcm5883l\n";
      code += generateHCM5883LConsumerCode(prms);
    } else if (prms.classList.contains("lsm303-channel")) { 
      code += "  - type: lsm303\n";
      code += generateLSM303ConsumerCode(prms);
    } else if (prms.classList.contains("zda-channel")) { 
      code += "  - type: zda\n";
      code += generateNoPrmConsumerCode(prms);
    } else if (prms.classList.contains("rnd-channel")) { 
      code += "  - type: rnd\n";
      code += generateNoPrmConsumerCode(prms);
    } else {
      // TODO Others
      console.log("Duh.");
    }
  }

  // Forwarders
  dropId = "drop-div-forwarders";
  dropBox = document.getElementById(dropId);
  code += `# ${dropBox.childElementCount === 0 ? 'No' :dropBox.childElementCount} Forwarder${dropBox.childElementCount > 1 ? 's' : ''}\n`;
  if (dropBox.childElementCount > 0) {
    code += "forwarders:\n";
  }
  for (let i=0; i<dropBox.childElementCount; i++) {
    // Look for the type and parameters
    let prms = dropBox.children[i].querySelector(".editable-zone");
    if (prms.classList.contains("serial-forwarder")) {
      code += "  - type: serial\n";
      code += generateSerialFwdCode(prms);
    } else if (prms.classList.contains("file-forwarder")) {
      code += "  - type: file\n";
      code += generateFileFwdCode(prms);
    } else if (prms.classList.contains("tcp-forwarder")) {
      code += "  - type: tcp\n";
      code += generateTCPFwdCode(prms);
    } else if (prms.classList.contains("gpsd-forwarder")) {
      code += "  - type: gpsd\n";
      code += generateGPSDFwdCode(prms);
    } else if (prms.classList.contains("ws-forwarder")) {
      code += "  - type: ws\n";
      code += generateWSFwdCode(prms);
    } else if (prms.classList.contains("rmi-forwarder")) {
      code += "  - type: rmi\n";
      code += generateRMIFwdCode(prms);
    } else if (prms.classList.contains("console-forwarder")) {
      code += "  - type: console\n";
      // code += "";""
    } else if (prms.classList.contains("rest-forwarder")) {
      code += "  - type: rest\n";
      code += generateRESTFwdCode(prms);
    }
  }

  // Computers
  dropId = "drop-div-computers";
  dropBox = document.getElementById(dropId);
  code += `# ${dropBox.childElementCount === 0 ? 'No' :dropBox.childElementCount} Computer${dropBox.childElementCount > 1 ? 's' : ''}\n`;
  if (dropBox.childElementCount > 0) {
    code += "computers:\n";
  }
  for (let i=0; i<dropBox.childElementCount; i++) {
     // Look for the type and parameters
     let prms = dropBox.children[i].querySelector(".editable-zone");
     if (prms.classList.contains("tw-cc-computer")) {
       code += "  - type: tw-current\n";
       code += generateTWCurrentComputerCode(prms);
     } else {
       console.log('???');
     }
   }

  let textContent = document.getElementById('generated-yaml');
  textContent.innerHTML = `${code}`; // Already framed with <pre>
  showGeneratedDialog();
}
