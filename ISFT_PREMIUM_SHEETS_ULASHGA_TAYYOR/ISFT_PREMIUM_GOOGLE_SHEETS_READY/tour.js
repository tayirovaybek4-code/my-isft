import * as THREE from 'three';
import {OrbitControls} from 'three/addons/controls/OrbitControls.js';
const host=document.getElementById('tour');const scene=new THREE.Scene();scene.background=new THREE.Color('#1a2a35');scene.fog=new THREE.Fog('#1a2a35',55,105);
const camera=new THREE.PerspectiveCamera(43,host.clientWidth/host.clientHeight,.1,200);camera.position.set(27,19,31);
let renderer;try{renderer=new THREE.WebGLRenderer({antialias:true});}catch(e){document.getElementById('tourError').hidden=false;document.getElementById('tourError').textContent='3D uchun WebGL yoqilgan brauzer kerak: '+e.message;throw e;}renderer.setPixelRatio(Math.min(devicePixelRatio,2));renderer.setSize(host.clientWidth,host.clientHeight);renderer.shadowMap.enabled=true;renderer.outputColorSpace=THREE.SRGBColorSpace;host.prepend(renderer.domElement);
scene.add(new THREE.HemisphereLight(0xd9efff,0x53616a,2));const sun=new THREE.DirectionalLight(0xffffff,3);sun.position.set(-15,28,20);sun.castShadow=true;sun.shadow.mapSize.set(2048,2048);scene.add(sun);
const mat=(c,metalness=0)=>new THREE.MeshStandardMaterial({color:c,roughness:.75,metalness});const white=mat('#d6dde0'),dark=mat('#30383e'),glass=new THREE.MeshStandardMaterial({color:'#172c38',metalness:.5,roughness:.17}),red=mat('#c70e2b'),roof=mat('#667078'),grass=mat('#2c5640'),path=mat('#b8b5aa');
function box(w,h,d,x,y,z,m,shadow=true){const a=new THREE.Mesh(new THREE.BoxGeometry(w,h,d),m);a.position.set(x,y,z);a.castShadow=shadow;a.receiveShadow=true;scene.add(a);return a}
box(80,.2,65,0,-.15,0,grass,false);box(16,.12,29,0,-.01,24,path,false);
// Main front façade faces +Z, with approximately four visible floors.
box(30,14,10,0,7,0,white);box(30,3.3,10,0,15.7,0,dark);box(32,.6,12,0,17.5,0,roof);
for(let floor=0;floor<4;floor++){const y=2.1+floor*3.25;for(let col=0;col<13;col++){const x=-13.7+col*2.27;box(1.38,1.65,.08,x,y,5.06,glass,false);box(1.38,1.65,.08,x,y,-5.06,glass,false);box(.16,1.7,.12,x,y,5.13,dark,false);box(1.48,.12,.12,x,y+.86,5.13,dark,false)}}
for(let i=0;i<15;i++){const x=-14.5+i*2.07;box(.23,13.4,.65,x,7.05,5.35,dark)}
for(let i=0;i<5;i++){const y=2.1+i*3.2;for(let z=-3.5;z<=3.5;z+=2.35){box(.09,1.6,1.5,-15.05,y,z,glass,false);box(.09,1.6,1.5,15.05,y,z,glass,false)}}
box(5.9,1,4,2.3,3.2,7.1,dark);box(4.8,3.1,.15,2.3,1.5,5.16,glass,false);box(2,.12,12,2.3,.12,12,path,false);
// ISFT's distinct vertical red and white accent on a gray façade.
box(1.35,17.3,.38,10.9,8.7,5.58,dark);box(.19,17.3,.43,11.6,8.7,5.81,red);box(.12,17.3,.43,10.18,8.7,5.81,white);
function textSprite(label,fg='#ffffff',bg='#a80e2a'){const canvas=document.createElement('canvas');canvas.width=512;canvas.height=128;const c=canvas.getContext('2d');c.fillStyle=bg;c.fillRect(0,0,512,128);c.fillStyle=fg;c.font='bold 70px Arial';c.textAlign='center';c.textBaseline='middle';c.fillText(label,256,65);const tex=new THREE.CanvasTexture(canvas);tex.colorSpace=THREE.SRGBColorSpace;return new THREE.Sprite(new THREE.SpriteMaterial({map:tex,transparent:true}))}
const sign=textSprite('ISFT INSTITUTI');sign.scale.set(5,1.25,1);sign.position.set(2.2,3.55,9.22);scene.add(sign);
for(let i=0;i<12;i++){const x=-28+i*5.1;const z=i%2?19:-16;box(.25,2.2,.25,x,1.1,z,mat('#59442e'));const tree=new THREE.Mesh(new THREE.ConeGeometry(1.15,3,7),mat('#1f6447'));tree.position.set(x,3.2,z);scene.add(tree)}
const controls=new OrbitControls(camera,renderer.domElement);controls.enableDamping=true;controls.target.set(0,7,0);controls.minDistance=18;controls.maxDistance=75;controls.maxPolarAngle=Math.PI*.49;controls.update();document.getElementById('resetCamera').onclick=()=>{camera.position.set(27,19,31);controls.target.set(0,7,0);controls.update()};
new ResizeObserver(()=>{const w=host.clientWidth,h=host.clientHeight;camera.aspect=w/h;camera.updateProjectionMatrix();renderer.setSize(w,h)}).observe(host);
function render(){requestAnimationFrame(render);controls.update();renderer.render(scene,camera)}render();
