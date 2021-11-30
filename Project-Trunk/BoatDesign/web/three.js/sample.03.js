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
const camera = new THREE.PerspectiveCamera( 75, window.innerWidth / window.innerHeight, 0.1, 1000 );

const renderer = new THREE.WebGLRenderer();
renderer.setSize( window.innerWidth, window.innerHeight );
document.body.appendChild( renderer.domElement );

// Fill it out
let points = [];
console.log(`Processing ${calculatedPoints.length} points`);
let factor = 100.0;
// From calculated.js
calculatedPoints.forEach(pt => points.push(new THREE.Vector3(pt.x / factor, pt.z / factor, pt.y / factor)));

const geometry = new ConvexGeometry( points );
const material = new THREE.MeshBasicMaterial( { color: 0x00ff00 } );
const mesh = new THREE.Mesh( geometry, material );
scene.add( mesh );

camera.position.z = 5;

const animate = function () {
    requestAnimationFrame( animate );

    mesh.rotation.x += 0.01;
    mesh.rotation.y += 0.01;

    renderer.render( scene, camera );
};

animate();

console.log("Bam");
