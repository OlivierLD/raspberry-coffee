const TO_RED = 1;
const TO_BLUE = 2;
const TO_GREEN = 3;
const TO_PURPLE = 4;

/**
 * We assume that the original fax comes in black and white.
 * Only black and white.
 */
transformFax = (imgCanvasName, canvasName, changeBlackTo) => {
  const img = document.getElementById(imgCanvasName);
  const canvas = document.getElementById(canvasName);
  let ctx = canvas.getContext("2d");

  if (img.width === 0 || img.height === 0) {
    // Something's not right...
    throw `image on ${canvasName} has some 0-dimension: w: ${img.width}, h: ${img.height}`;
  }

  canvas.width = img.width;
  canvas.height = img.height;

  ctx.drawImage(img, 0, 0, img.naturalWidth, img.naturalHeight, 0, 0, img.width, img.height);

  originalPixels = ctx.getImageData(0, 0, img.width, img.height);
  currentPixels = ctx.getImageData(0, 0, img.width, img.height);

  let pix = originalPixels.data;
  // console.log(`There are ${pix.length} pixels.`);

  for (let idx=0; idx<pix.length; idx+=4) { // 4 by 4, rgba.
    let r = pix[idx];       // Red
    let g = pix[idx + 1];   // Green
    let b = pix[idx + 2];   // Blue
    let a = pix[idx + 3];   // Alpha
    // console.log(`rgba(${r},${g},${b},${a})`);
    if (r === 0 && b === 0 && g === 0) { // black
      if (changeBlackTo === TO_RED) {
        r = 255;  // Change black to red
      } else if (changeBlackTo === TO_BLUE) {
        b = 255;  // Change black to blue
      } else if (changeBlackTo === TO_GREEN) {
        // 47, 79, 47
        // Change black to dark-green
        r = 47;
        g = 79;
        b = 47;
      } else if (changeBlackTo === TO_PURPLE) {
        // FF00FF
        r = 255;
        // g = 0;
        b = 255;
      }
    } else if (r === 255 && b === 255 && g === 255) { // white
      a = 0;    // Make white transparent
    }
    currentPixels.data[idx] = r;
    currentPixels.data[idx + 1] = g;
    currentPixels.data[idx + 2] = b;
    currentPixels.data[idx + 3] = a;
  }
  ctx.putImageData(currentPixels, 0, 0);  // Bam!
};

const ATLANTIC = 1;
const PACIFIC = 2;

// WiP...
/* 
 * This is a JSON Object holding all the required infos for a given fax display.
 * All elements at level 1 are arrays of faxes.
 * A fax is an Object like this:
 * {
 *   "name": "<Name of the fax>",
 *   "url": "<URL to the fax>",
 *    "downloadTo": "<file name, on your system>",
 *    "fromTo": {
 *      "from": "<name of the element where the raw fax is loaded (invisible)>",
 *      "to": "<name of the element where the fax will be displayed>",
 *      "colorChange": <what color to turn the BLACK to>
 *    }, 
 *    "tx": {
 *      "width": "<css transformation, for the with of the fax>",
 *      "height": "<css transformation, for the height (usually 'auto')>",
 *      "transform": "<css transformation, rotation>",
 *      "left": "<css transformation, left padding>",
 *      "top": "<css transformation, top padding>"
 *    }
 * }
 */
const faxTransformer = {
  "atlantic-n-00" : [
    { 
      "name": "NW Atlantic Surface",
      "url": "https://tgftp.nws.noaa.gov/fax/PYAA12.gif",
      "downloadTo": "NW-Pac.gif",
      "fromTo": {
        "from": "left",
        "to": "left-img-canvas",
        "colorChange": TO_RED
      }, 
      "tx": {
        "width": "600px",
        "height": "auto",
        "transform": "rotate(90deg)",
        "left": "125px",
        "top": "160px"
      }
    }, { 
      "name": "NE Atlantic Surface",
      "url": "https://tgftp.nws.noaa.gov/fax/PYAA11.gif",
      "downloadTo": "NE-Pac.gif",
      "fromTo": {
        "from": "right",
        "to": "right-img-canvas",
        "colorChange": TO_RED
      }, 
      "tx": {
        "width": "600px",
        "height": "auto",
        "transform": "rotate(90deg)",
        "left": "541px",
        "top": "160px"
      }
    }, { 
      "name": "N Atlantic 500mb",
      "url": "https://tgftp.nws.noaa.gov/fax/PPAA10.gif",
      "downloadTo": "N-Atl-500mb.gif",
      "fromTo": {
        "from": "500mb",
        "to": "north-img-canvas",
        "colorChange": TO_BLUE
      }, 
      "tx": {
        "width": "919px",
        "height": "auto",
        "transform": "rotate(0deg)",
        "left": "187px",
        "top": "129px"
      }
    }, { 
      "name": "N Atlantic Sea State",
      "url": "https://tgftp.nws.noaa.gov/fax/PJAA99.gif",
      "downloadTo": "N-Atl-waves.gif",
      "fromTo": {
      "downloadTo": "",
        "from": "waves",
        "to": "waves-img-canvas",
        "colorChange": TO_GREEN
      }, 
      "tx": {
        "width": "919px",
        "height": "auto",
        "transform": "rotate(0deg)",
        "left": "187px",
        "top": "129px"
      }
    }
  ],
  "pacific-n-00" : [
    { 
      "name": "NW Pacific Surface",
      "url": "https://tgftp.nws.noaa.gov/fax/PYBA90.gif",
      "downloadTo": "NW-Pac.gif",
      "fromTo": {
        "from": "left",
        "to": "left-img-canvas",
        "colorChange": TO_RED
      }, 
      "tx": {
        "width": "600px",
        "height": "auto",
        "transform": "rotate(90deg)",
        "left": "125px",
        "top": "160px"
      }
    }, { 
      "name": "NE Pacific Surface",
      "url": "https://tgftp.nws.noaa.gov/fax/PYBA91.gif",
      "downloadTo": "NE-Pac.gif",
      "fromTo": {
        "from": "right",
        "to": "right-img-canvas",
        "colorChange": TO_RED
      }, 
      "tx": {
        "width": "600px",
        "height": "auto",
        "transform": "rotate(90deg)",
        "left": "541px",
        "top": "160px"
      }
    }, { 
      "name": "N Pacific 500mb",
      "url": "https://tgftp.nws.noaa.gov/fax/PPBA10.gif",
      "downloadTo": "N-Pac-500mb.gif",
      "fromTo": {
        "from": "500mb",
        "to": "north-img-canvas",
        "colorChange": TO_BLUE
      }, 
      "tx": {
        "width": "919px",
        "height": "auto",
        "transform": "rotate(0deg)",
        "left": "178px",
        "top": "129px"
      }
    }, { 
      "name": "N Pacific Sea State",
      "url": "https://tgftp.nws.noaa.gov/fax/PJBA99.gif",
      "downloadTo": "N-Pac-waves.gif",
      "fromTo": {
        "from": "waves",
        "to": "waves-img-canvas",
        "colorChange": TO_GREEN
      }, 
      "tx": {
        "width": "919px",
        "height": "auto",
        "transform": "rotate(0deg)",
        "left": "178px",
        "top": "129px"
      }
    }, { 
      "name": "Central Pacific Streamlines",
      "url": "https://tgftp.nws.noaa.gov/fax/PWFA11.gif",
      "downloadTo": "C-Pac-streamlines.gif",
      "fromTo": {
        "from": "sl",
        "to": "sl-img-canvas",
        "colorChange": TO_PURPLE
      }, 
      "tx": {
        "width": "728px",
        "height": "auto",
        "transform": "rotate(90deg)",
        "left": "228px",
        "top": "393px"
      }
    }
  ]
};

doOnLoad = (option) => {

  // res.header('Access-Control-Allow-Origin', '*');
  // res.header('Access-Control-Allow-Methods', '*');
  // res.header('Access-Control-Allow-Headers', '*');
  // res.header('Access-Control-Max-Age', '3600');
  // leftFax.setAttribute('crossOrigin', '*'); // Need Access-Control-Allow-Origin
  // leftFax.src = "https://tgftp.nws.noaa.gov/fax/PYAA12.gif"

  let spotOption = 'atlantic-n-00'; // Default

  switch (option) {
    case ATLANTIC:
      spotOption = 'atlantic-n-00';
      break;
    case PACIFIC:
      spotOption = 'pacific-n-00';
      break;
    default:
      console.log(`Unknown Option ${option}, defaulting to [atlantic-n-00]`);
      break;
  }

  let faxStructure = faxTransformer[spotOption];

  faxStructure.forEach(fax => {
    try {
      transformFax(fax.fromTo.from, fax.fromTo.to, fax.fromTo.colorChange);
      const reworkedFax = document.getElementById(fax.fromTo.to);
      reworkedFax.style.width = fax.tx.width;
      reworkedFax.style.height = fax.tx.height;
      reworkedFax.style.transform = fax.tx.transform;
      reworkedFax.style.top = fax.tx.top;
      reworkedFax.style.left = fax.tx.left;
    } catch (err) {
      console.log(err);
    }
  });

};