function allowDrop(ev) {
  let origin =  ev.target.id;
  ev.preventDefault();
}

// When drag starts
function drag(ev) {
  ev.dataTransfer.setData("dragged-id", ev.target.id);
}

// When dropped
function drop(ev) {
  ev.preventDefault();
  let data = ev.dataTransfer.getData("dragged-id"); // Id of the dragged Node
  if (data == 'no-drag-1') {
      // Prevent Element Four
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
  // console.log("Aha!");
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

function dumpIt() {
  let dropId = "drop-div";
  let dropBox = document.getElementById(dropId);
  console.log('Here');
  let allText = '';
  for (let i=0; i<dropBox.childElementCount; i++) {
    // Look for the type and parameters
    let prms = dropBox.children[i].querySelector(".editable-zone");
    if (prms.classList.contains("serial-channel")) {
      console.log("It's a Serial Channel");
      let portName = prms.querySelector(".port-name").value;
      let baudRate = prms.querySelector(".baud-rate").value;
      allText += `Serial : ${portName}:${baudRate}\n`;
      // console.log(`Port : ${portName}:${baudRate}`);
    } else if (prms.classList.contains("tcp-channel")) { 
      console.log("It's a TCP Channel");
      let serverName = prms.querySelector(".server-name").value;
      let portNum = prms.querySelector(".port-num").value;
      allText += `TCP : ${serverName}:${portNum}\n`;
      console.log(`TCP : ${serverName}:${portNum}`);
    } else {
      // TODO Others
      console.log("Duh.");
    }
  }
  let textContent = document.getElementById('generated-list');
  textContent.innerHTML = `<pre>${allText}</pre>`;
  showGeneratedDialog();
}
