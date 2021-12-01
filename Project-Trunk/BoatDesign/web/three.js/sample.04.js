"use strict";

// Absolute imports
//import * as THREE from 'three';
//
//import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls';
//import { ConvexGeometry } from 'three/examples/jsm/geometries/ConvexGeometry';
//import * as BufferGeometryUtils from 'three/examples/jsm/utils/BufferGeometryUtils';

// Relative imports
//import * as THREE from './node_modules/three/build/three.module.js'; // That one seems to work, not "from 'three' "
//
//import { OrbitControls } from './node_modules/three/examples/jsm/controls/OrbitControls.js';
//import { ConvexGeometry } from './node_modules/three/examples/jsm/geometries/ConvexGeometry.js';
//import * as BufferGeometryUtils from './node_modules/three/examples/jsm/utils/BufferGeometryUtils.js';

// CDN
import * as THREE from 'https://cdn.skypack.dev/three@0.132.0/build/three.module.js'
import { OrbitControls } from 'https://cdn.skypack.dev/three@0.132.0/examples/jsm/controls/OrbitControls'
import { OBJLoader } from 'https://cdn.skypack.dev/three@0.132.0/examples/jsm/loaders/OBJLoader'
import Stats from 'https://cdn.skypack.dev/three@0.132.0/examples/jsm/libs/stats.module'
import { ConvexGeometry } from 'https://cdn.skypack.dev/three@0.132.0/examples/jsm/geometries/ConvexGeometry'


const scene = new THREE.Scene();

const light = new THREE.HemisphereLight( 0xffffff, 0x000088 );
light.position.set( - 1, 1.5, 1 );
scene.add( light );

const camera = new THREE.PerspectiveCamera( 75, window.innerWidth / window.innerHeight, 0.1, 1000 );

const renderer = new THREE.WebGLRenderer();
renderer.setSize( window.innerWidth, window.innerHeight );
document.body.appendChild( renderer.domElement );

// To drag with the mouse
const controls = new OrbitControls( camera, renderer.domElement );
controls.minDistance = 5; // 20;
controls.maxDistance = 5; // 50;
controls.maxPolarAngle = Math.PI / 1;

// Fill it out
let points = [];
console.log(`Processing ${calculatedPoints.length} points`);
const factor = 100.0;
// From calculated.js
calculatedPoints.forEach(pt => points.push(new THREE.Vector3(pt.x / factor, pt.z / factor, pt.y / factor)));

const geometry = new ConvexGeometry( points );
// https://threejs.org/docs/scenes/material-browser.html#MeshPhongMaterial
const material = // new THREE.MeshBasicMaterial( { color: 0x00ffff, transparent: true, opacity: 0.75 } );
              // new THREE.MeshPhongMaterial({ wireframe: false });
                 new THREE.MeshNormalMaterial({ wireframe: false });
//let spGroup = new THREE.Object3D();
//let material_2 = new THREE.MeshBasicMaterial({color: 0xff0000, transparent: true});
//points.forEach(point => {
//    var spGeom = new THREE.SphereGeometry(0.005); // Red dots diameter
//    var spMesh = new THREE.Mesh(spGeom, material_2);
//    spMesh.position.copy(point);
//    spGroup.add(spMesh);
//});
// add the points as a group to the scene
//scene.add(spGroup);

// const wireFrameMat = new THREE.MeshBasicMaterial();
// wireFrameMat.wireframe = true;

const mesh = // THREE.SceneUtils.createMultiMaterialObject(geometry, [material, wireFrameMat]);
             new THREE.Mesh( geometry, material );
scene.add( mesh );

// White directional light at half intensity shining from the top.
//const directionalLight = new THREE.DirectionalLight( 0xffffff, 0.75 );
//scene.add( directionalLight );

// const light = new THREE.AmbientLight( 0x404040, 0.5 ); // soft white light
// const light = new THREE.HemisphereLight( 0xffffbb, 0x080820, 1 );

// const light = new THREE.PointLight( 0xff0000, 1, 10, 2 );
// light.position.set( 10, 10, 10 );

//const light = new THREE.DirectionalLight( 0xFFFFFF );
//const helper = new THREE.DirectionalLightHelper( light, 5 );
//scene.add( helper );

scene.add( light );

camera.position.z = 4;

const animate = function () {
    requestAnimationFrame( animate );

    // mesh.rotation.x += 0.01;
    // mesh.rotation.y += 0.01;

    renderer.render( scene, camera );
};

animate();
// renderer.render( scene, camera );

console.log("Bam");
