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

function generateSerialComsumerCode(node) {
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

function generateTCPComsumerCode(node) {
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

function dumpIt() {

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
      console.log("It's a Serial Channel");
      code += "  - type: serial\n";
      code += generateSerialComsumerCode(prms);
    } else if (prms.classList.contains("tcp-channel")) { 
      console.log("It's a TCP Channel");
      code += "  - type: tcp\n";
      code += generateTCPComsumerCode(prms);
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
    // TODO Implement
  }

  // Computers
  dropId = "drop-div-computers";
  dropBox = document.getElementById(dropId);
  code += `# ${dropBox.childElementCount === 0 ? 'No' :dropBox.childElementCount} Computer${dropBox.childElementCount > 1 ? 's' : ''}\n`;
  if (dropBox.childElementCount > 0) {
    code += "computers:\n";
  }
  for (let i=0; i<dropBox.childElementCount; i++) {
    // TODO Implement
  }

  let textContent = document.getElementById('generated-list');
  textContent.innerHTML = `<pre>${code}</pre>`;
  showGeneratedDialog();
}
