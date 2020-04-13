/*
 * The data below are elaborated from the spreadsheet boat.ods.
 * To generate the vertices, jus run this file (like in node.js).
 */
var boat = {
    keel: [
        { x: -3, y: 0, z: 1},
        { x: -2, y: 0, z: 0},
        { x: -1, y: 0, z: -0.6},
        { x:  0, y: 0, z: -0.75},
        { x:  1, y: 0, z: -0.75},
        { x:  2, y: 0, z: -0.5},
        { x:  3, y: 0, z: -0.3},
    ],
    chine: [
        { x: -3, y: 0, z: 1},
        { x: -2, y: 0.6, z: 0.4},
        { x: -1, y: 1.1, z: 0},
        { x:  0, y: 1.3, z: -0.2},
        { x:  1, y: 1.25, z: -0.2},
        { x:  2, y: 1.1, z: -0.1},
        { x:  3, y: 0.8, z: 0.2},
    ],
    rail: [
        { x: -3, y: 0, z: 1},
        { x: -2, y: 0.75, z: 0.95},
        { x: -1, y: 1.2, z: 0.9},
        { x:  0, y: 1.47, z: 0.9},
        { x:  1, y: 1.5, z: 0.9},
        { x:  2, y: 1.425, z: 0.93},
        { x:  3, y: 1.2, z: 1}
    ]
};

var validate = function(obj) {
  var valid = true;
  if (obj.keel === undefined || obj.chine === undefined || obj.rail === undefined) {
    valid = false;
  } else {
    var dim = obj.keel.length;
    if (obj.chine.length !== dim || obj.rail.length != dim) {
      valid = false;
    }
  }

  return valid;
};

var makeVertice = function(obj) {
  var allVert = [];
  var nb = 0;
  var dim = obj.keel.length;
  for (var i=0; i<dim - 1; i++) {
    var vert = [ obj.keel[i].x, obj.keel[i].y, obj.keel[i].z,
                 obj.keel[i + 1].x, obj.keel[i + 1].y, obj.keel[i + 1].z,
                 obj.chine[i].x, obj.chine[i].y, obj.chine[i].z,
                 obj.chine[i + 1].x, obj.chine[i + 1].y, obj.chine[i + 1].z ];
    for (var j=0; j<vert.length;j++) {
      allVert.push(vert[j]);
    }
    nb += 4;
    var vert = [ obj.rail[i].x, obj.rail[i].y, obj.rail[i].z,
          obj.rail[i + 1].x, obj.rail[i + 1].y, obj.rail[i + 1].z,
          obj.chine[i].x, obj.chine[i].y, obj.chine[i].z,
          obj.chine[i + 1].x, obj.chine[i + 1].y, obj.chine[i + 1].z ];
    for (var j=0; j<vert.length;j++) {
      allVert.push(vert[j]);
    }
    nb += 4;
  }
  return { nb: nb, vert: allVert };
};

var valid = validate(boat);
console.log("Valid:", valid);

var factor = 3.0;

if (valid === true) {
  var x = makeVertice(boat);
  console.log(x.nb + " vertices per side");
  // One side
  for (var i=0; i<x.nb; i++) {
    console.log((i === 0 ? " " : ", ") + (x.vert[(i*3) + 0] / factor) + ", " + (x.vert[(i*3) + 1] / factor) + ", " +  (x.vert[(i*3) + 2] / factor));
  }
  // Other side
  for (var i=0; i<x.nb; i++) {
    console.log((false && i === 0 ? " " : ", ") + (x.vert[(i*3) + 0] / factor) + ", " + (-x.vert[(i*3) + 1] / factor) + ", " +  (x.vert[(i*3) + 2] / factor));
  }
//console.log(x.vert);
}
