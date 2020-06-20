/**
 * Et allez!
 *
 * @author Olivier Le Diouris
 * Uses ES6 Promises for Ajax.
 */

const DEBUG = false;
const DEFAULT_TIMEOUT = 60000; // 1 minute

let targetNameSpace = {};

function getQSPrm(prm) {
	let value;
	let loc = document.location.toString();
	if (loc.indexOf("?") > -1) {
		let qs = loc.substring(loc.indexOf("?") + 1);
		let prms = qs.split('&');
		for (let i=0; i<prms.length; i++) {
			let nv = prms[i].split('=');
			if (nv.length === 2) {
				if (nv[0] === prm) {
					return nv[1];
				}
			}
		}
	}
	return value;
}

/* Uses ES6 Promises */
function getPromise(
		url, // full api path
		timeout, // After that, fail.
		verb, // GET, PUT, DELETE, POST, etc
		headers, // Request headers
		happyCode, // if met, resolve, otherwise fail.
		data = null) { // payload, when needed (PUT, POST...)

	let promise = new Promise(function(resolve, reject) {
		let xhr = new XMLHttpRequest();

		// let req = verb + ' ' + url;
		// if (data !== undefined && data !== null) {
		// 	req += ("\n" + JSON.stringify(data)); // , null, 2));
		// }

		xhr.open(verb, url, true);
		if (headers !== undefined) {
			headers.forEach(header => {
				xhr.setRequestHeader(header.name, header.value);
			});
		}
		try {
			if (data === undefined || data === null) {
				xhr.send();
			} else {
				xhr.send(JSON.stringify(data));
			}
		} catch (err) {
			console.log('Send Error ', err);
		}

		var requestTimer;
		if (timeout !== undefined) {
			requestTimer = setTimeout(function() {
				xhr.abort();
				let mess = {
					code: 408,
					message: 'Timeout',
					url: url,
					verb: verb
				};
				reject({
					err: mess,
					accessToken: xhr.getResponseHeader('Access-Token')
				});
			}, timeout);
		}

		xhr.onload = function() {
			if (requestTimer !== undefined) {
				clearTimeout(requestTimer);
			}
			if ((Array.isArray(happyCode) && happyCode.includes(xhr.status)) || xhr.status === happyCode) {
				resolve({
					response: xhr.response,
					accessToken: xhr.getResponseHeader('Access-Token')
				});
			} else {
				reject({
					code: xhr.status,
					message: xhr.response,
					accessToken: xhr.getResponseHeader('Access-Token')
				});
			}
		};
	});
	return promise;
}

function enableParseButton(textarea, buttonId) {

	if (textarea.value.length > 0) {
		document.getElementById(buttonId).disabled = false;
	} else {
		document.getElementById(buttonId).disabled = true;
	}

}

function getJSONData(data) {
	return getPromise('/server/xml-to-json', DEFAULT_TIMEOUT, 'POST', [{name:"Content-type", value:"text/xml"}], 201, data);
}

function loadJSONData(fieldId, resultId, formId, buttonId) {
	let payload = unescape(document.getElementById(fieldId).value);
	// debugger;
	let getData = getJSONData(payload); // Empty obj, means use default
	getData.then((value) => { // Resolve
//  		console.log("Done:", value);
		try {
			let json = JSON.parse(value.response);
			// console.log('JSON Data', json);
			document.getElementById(resultId).value = JSON.stringify(json, null, 2);
			// Compose the form here
			composeForm(json, formId);
			// Enable submitButton
			document.getElementById(buttonId).disabled = false;

		} catch (err) {
			console.log("Error:", err, ("\nfor value [" + JSON.stringify(value, null, 2) + "]"));
		}
	}, (error) => { // Reject
		console.log("Failed to get JSON Data..." + (error !== undefined && error.code !== undefined ? error.code : ' - ') + ', ' + (error !== undefined && error.message !== undefined ? error.message : ' - '));
	});
}

function processXSL(xml, xsl) {
	return getPromise('/server/xml-xsl', DEFAULT_TIMEOUT, 'POST', [{name:"Content-type", value:"application/json"}], 201, {"xml": xml, "xsl": xsl});
}

function applyXSL(xmlId, xslId, outputId) {
	let xmlPayload = unescape(document.getElementById(xmlId).value);
	let xslPayload = unescape(document.getElementById(xslId).value);
	// debugger;
	let getData = processXSL(xmlPayload, xslPayload);
	getData.then((value) => { // Resolve
//  		console.log("Done:", value);
		try {
			document.getElementById(outputId).innerText = value.response;
		} catch (err) {
			console.log("Error:", err, ("\nfor value [" + JSON.stringify(value, null, 2) + "]"));
		}
	}, (error) => { // Reject
		console.log("Failed to get JSON Data..." + (error !== undefined && error.code !== undefined ? error.code : ' - ') + ', ' + (error !== undefined && error.message !== undefined ? error.message : ' - '));
	});
}

/**
 * Generate the form for the user to select the fields
 * @param parentNode
 * @param jsonData
 */
function walkTree(parentNode, jsonData) {
	Object.keys(jsonData).forEach(function (key) {
		let value = jsonData[key];

		// console.log("Value:", key, ", type:", typeof(value));
		// if (value instanceof Array) {
		// 	console.log(key, " is an array of ", value.length, " element(s)")
		// }

		if (!key.startsWith("@xmlns:")) {
			let li = document.createElement('li');
			parentNode.appendChild(li);
			let span = document.createElement('span');
			li.appendChild(span);
			let nameSpan = document.createElement('span');
			span.appendChild(nameSpan);
			nameSpan.appendChild(document.createTextNode(key));

			if (typeof(value) === "string") {
				let typeSpan = document.createElement('span');
				span.appendChild(typeSpan);
				let type = isNaN(value) ? 'string' : 'number';
				typeSpan.setAttribute('style', 'font-style: italic; padding-left: 6px;');
				typeSpan.appendChild(document.createTextNode('(' + type + ')'));
			}

			if (value instanceof Array) {
				let typeSpan = document.createElement('span');
				span.appendChild(typeSpan);
				let type = 'array';
				typeSpan.setAttribute('style', 'font-style: italic; font-weight: bold; padding-left: 6px;');
				typeSpan.appendChild(document.createTextNode('(' + type + ')'));
			}

			if (typeof(value) === "string") { // leafs only
				let cb = document.createElement('input');
				cb.setAttribute('type', 'checkbox');
				cb.setAttribute('checked', 'true');
				span.appendChild(cb);
			}
			if (typeof(value) === "object") {
				let ul = document.createElement('ul');
				parentNode.appendChild(ul);
				if (value instanceof Array) {
					walkTree(ul, value[0]);
				} else {
					walkTree(ul, value);
				}
			}
		} else {
			targetNameSpace = { "alias": key.substring("@xmlns:".length), "value": value };
		}
	});
}

function composeForm(jsonData, formId) {
	// Clean the form
	let form = document.getElementById(formId);
	while (form.hasChildNodes()) {
		form.removeChild(form.firstChild);
	}
	let ul = document.createElement('ul');
	form.appendChild(ul);
	walkTree(ul, jsonData);
}

/**
 * Generate the XPath elements from the user's choices.
 *
 * @param finalList
 * @param currentPath
 * @param element
 */
function drillDownList(finalList, currentPath, element) {
	if (element.nodeName === 'UL') {
		let children = element.childNodes;
		children.forEach(childNode => {
			if (childNode.nodeName === 'LI') {
				drillDownList(finalList, currentPath, childNode);
			}
		});
	} else if (element.nodeName === 'LI') {
		let liSpan = element.firstChild;
		currentPath += ('/' + liSpan.querySelector('span').innerText); // First span
		// Array?
		let typeSpan = liSpan.querySelectorAll('span')[1];
		if (typeSpan !== undefined && typeSpan.innerText === '(array)') {
			currentPath += '[]';
		}
		// console.log(typeSpan);
		// console.log('CurrentPath', currentPath);
		let cb = liSpan.querySelector('input[type=checkbox]');
		if (cb !== null && cb !== undefined) { // A leaf
			if (cb.checked === true) {
				finalList.push(currentPath);
			}
			// return;
		} else {
			let siblingElement = element.nextElementSibling;
			if (siblingElement != null && siblingElement !== undefined) {
				// console.log("Sibling of", element, " is a ", siblingElement);
				if (siblingElement.nodeName === 'UL') {
					drillDownList(finalList, currentPath, siblingElement);
				}
			}
		}
	}
}

function generateMapping(formId, resultId, xslId, tryId) {
	let form = document.getElementById(formId);
	let finalDoc = [];
	let ul = form.firstChild;
	if (ul !== null && ul !== undefined) {
		drillDownList(finalDoc, '', ul);
		console.log('Final doc', finalDoc);
		let result = document.getElementById(resultId);
		let content = '<pre>';
		finalDoc.forEach(line => content += (line + '\n'));
		content += '</pre>';
		result.innerHTML = content;
		result.style.display = 'inline-block';

		let xslDoc = generateXSL(finalDoc);
		console.log("Generated XSL:\n", xslDoc);
		let final = document.getElementById(xslId);
		final.value = /*'<pre>' +*/ xslDoc /*+ '</pre>'*/;

		if (tryId !== undefined) {
			document.getElementById(tryId).disabled = false;
		}
	}
}

function generateXSL(xpaths) {

  let doc = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" +
		  "<xsl:stylesheet version=\"1.0\"\n" +
		  "                xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"\n" +
		  "								 xmlns:" + targetNameSpace.alias + "=\"" + targetNameSpace.value + "\">\r\n";
  doc += "	<xsl:output method=\"text\" indent=\"no\"/>\r\n" +
		  "	<xsl:template match=\"/\">\r\n";

  let idx = 0;
  while (idx < xpaths.length) {
    let xpath = xpaths[idx];
  	let arrayIndex = xpath.indexOf('[]');
  	if (arrayIndex > -1) {
  		console.log('==> Array detected');
  		let prefix = xpath.substring(0, arrayIndex);
  		let suffix = xpath.substring(arrayIndex + '[]'.length);
  		console.log(prefix, ", ", suffix);
  		doc += "<xsl:for-each select=\"" + prefix + "\">\r\n";
  		while (xpath.startsWith(prefix) && idx < xpaths.length && arrayIndex > -1) {
			  doc += "<xsl:value-of select=\"." + suffix + "\"></xsl:value-of><xsl:text>;</xsl:text>\r\n";
			  idx++;
			  if (idx < xpaths.length) {
				  xpath = xpaths[idx];
				  arrayIndex = xpath.indexOf('[]');
				  if (arrayIndex > -1) {
					  prefix = xpath.substring(0, arrayIndex);
					  suffix = xpath.substring(arrayIndex + '[]'.length);
				  } else {
					  idx--;
				  }
			  }
		  }
  		doc += "</xsl:for-each>\r\n";
	  } else {
		  doc += "<xsl:value-of select=\"" + xpath + "\"></xsl:value-of><xsl:text>;</xsl:text>\r\n";
	  }
  	idx++;
  }
  doc += "<xsl:text>\n</xsl:text>\r\n"; // New Line at the end?

  doc += "	</xsl:template>\r\n" +
		  "</xsl:stylesheet>\r\n";

  return doc;
}
