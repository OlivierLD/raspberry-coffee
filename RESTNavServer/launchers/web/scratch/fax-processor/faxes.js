const TO_RED = 1;
const TO_BLUE = 2;
const TO_GREEN = 3;

/**
 * We assume that the original fax comes in black and white.
 * Only black and white.
 */
messWithCanvas = (img, canvasName, changeBlackTo) => {
  const canvas = document.getElementById(canvasName);
  let ctx = canvas.getContext("2d");

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

doOnLoad = () => {
  const leftFax = document.getElementById('left');
  const rightFax = document.getElementById('right');
  const fax500mb = document.getElementById('500mb');
  const faxWaves = document.getElementById('waves');


  // res.header('Access-Control-Allow-Origin', '*');
  // res.header('Access-Control-Allow-Methods', '*');
  // res.header('Access-Control-Allow-Headers', '*');
  // res.header('Access-Control-Max-Age', '3600');
  // leftFax.setAttribute('crossOrigin', '*'); // Need Access-Control-Allow-Origin
  // leftFax.src = "https://tgftp.nws.noaa.gov/fax/PYAA12.gif"

  messWithCanvas(leftFax, 'left-img-canvas', TO_RED);
  const leftReworkedFax = document.getElementById('left-img-canvas');
  leftReworkedFax.style.width = '600px';
  leftReworkedFax.style.height = 'auto';
  leftReworkedFax.style.transform = 'rotate(90deg)';
  // leftReworkedFax.style.marginLeft = '0px';
  leftReworkedFax.style.top = '160px';
  leftReworkedFax.style.left = '125px';

  messWithCanvas(rightFax, 'right-img-canvas', TO_RED);
  const rightReworkedFax = document.getElementById('right-img-canvas');
  rightReworkedFax.style.width = '600px';
  rightReworkedFax.style.height = 'auto';
  rightReworkedFax.style.transform = 'rotate(90deg)';
  // rightReworkedFax.style.marginLeft = '-188px';
  rightReworkedFax.style.top = '160px';
  rightReworkedFax.style.left = '541px';

  messWithCanvas(fax500mb, 'north-img-canvas', TO_BLUE);
  const northReworkedFax = document.getElementById('north-img-canvas');
  northReworkedFax.style.width = '919px';
  northReworkedFax.style.height = 'auto';
  northReworkedFax.style.transform = 'rotate(0deg)';
  // northReworkedFax.style.marginLeft = '62px';
  // northReworkedFax.style.marginTop = '-537px';
  northReworkedFax.style.top = '129px';
  northReworkedFax.style.left = '187px';

  messWithCanvas(faxWaves, 'waves-img-canvas', TO_GREEN);
  const waveshReworkedFax = document.getElementById('waves-img-canvas');
  waveshReworkedFax.style.width = '919px';
  waveshReworkedFax.style.height = 'auto';
  waveshReworkedFax.style.transform = 'rotate(0deg)';
  // waveshReworkedFax.style.marginLeft = '62px';
  // waveshReworkedFax.style.marginTop = '-537px';
  waveshReworkedFax.style.top = '129px';
  waveshReworkedFax.style.left = '187px';
};