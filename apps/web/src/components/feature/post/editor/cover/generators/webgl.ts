/**
 * Shared WebGL renderer for the `aurora` (domain-warped fBm noise) and `mesh`
 * (soft multi-point gradient) algorithms. Both use one fragment program,
 * switched by the `uMode` uniform. Grain + vignette are applied in-shader.
 */
import { buildBg, buildPalette, deriveSeed } from './palette';
import { strHash } from './rng';
import type { CoverParams } from './types';
import { COVER_HEIGHT, COVER_WIDTH } from './types';

const VERT = `attribute vec2 aPos; varying vec2 vUv;
void main(){ vUv=aPos*0.5+0.5; gl_Position=vec4(aPos,0.0,1.0); }`;

const FRAG = `
precision highp float;
varying vec2 vUv;
uniform float uTime, uSeed, uGrain, uVignette, uScale, uWarp, uAspect;
uniform int uMode;
uniform vec2 uRes;
uniform vec3 uColors[4];
uniform vec2 uPoints[6];
uniform vec3 uPointColors[6];

vec3 mod289(vec3 x){return x-floor(x*(1.0/289.0))*289.0;}
vec2 mod289(vec2 x){return x-floor(x*(1.0/289.0))*289.0;}
vec3 permute(vec3 x){return mod289(((x*34.0)+1.0)*x);}
float snoise(vec2 v){
  const vec4 C=vec4(0.211324865405187,0.366025403784439,-0.577350269189626,0.024390243902439);
  vec2 i=floor(v+dot(v,C.yy));
  vec2 x0=v-i+dot(i,C.xx);
  vec2 i1=(x0.x>x0.y)?vec2(1.0,0.0):vec2(0.0,1.0);
  vec4 x12=x0.xyxy+C.xxzz; x12.xy-=i1;
  i=mod289(i);
  vec3 p=permute(permute(i.y+vec3(0.0,i1.y,1.0))+i.x+vec3(0.0,i1.x,1.0));
  vec3 m=max(0.5-vec3(dot(x0,x0),dot(x12.xy,x12.xy),dot(x12.zw,x12.zw)),0.0);
  m=m*m; m=m*m;
  vec3 x=2.0*fract(p*C.www)-1.0;
  vec3 h=abs(x)-0.5;
  vec3 ox=floor(x+0.5);
  vec3 a0=x-ox;
  m*=1.79284291400159-0.85373472095314*(a0*a0+h*h);
  vec3 g;
  g.x=a0.x*x0.x+h.x*x0.y;
  g.yz=a0.yz*x12.xz+h.yz*x12.yw;
  return 130.0*dot(m,g);
}
float fbm(vec2 p){ float v=0.0,a=0.5; mat2 r=mat2(1.6,1.2,-1.2,1.6); for(int i=0;i<5;i++){ v+=a*snoise(p); p=r*p; a*=0.5; } return v; }
float hash1(vec2 p){ return fract(sin(dot(p,vec2(12.9898,78.233)))*43758.5453); }

vec3 ramp(float t){
  t=clamp(t,0.0,1.0);
  if(t<0.3333) return mix(uColors[0],uColors[1],t/0.3333);
  else if(t<0.6666) return mix(uColors[1],uColors[2],(t-0.3333)/0.3333);
  return mix(uColors[2],uColors[3],(t-0.6666)/0.3334);
}
vec3 aurora(){
  vec2 uv=vUv; uv.x*=uAspect;
  vec2 sp=uv*uScale+vec2(uSeed);
  float t2=uTime*0.05;
  vec2 q=vec2(fbm(sp+vec2(0.0,t2)), fbm(sp+vec2(5.2,1.3)));
  vec2 r=vec2(fbm(sp+uWarp*q+vec2(1.7,9.2)+t2), fbm(sp+uWarp*q+vec2(8.3,2.8)));
  float f=fbm(sp+uWarp*r);
  vec3 col=ramp(f*0.5+0.5);
  col*=0.85+0.3*(q.x*0.5+0.5);
  return col;
}
vec3 meshg(){
  vec2 uv=vUv; uv.x*=uAspect;
  vec2 w=uv+uWarp*0.12*vec2(fbm(uv*2.0+uSeed+uTime*0.03), fbm(uv*2.0+uSeed+3.1));
  vec3 acc=vec3(0.0); float wsum=0.0;
  for(int i=0;i<6;i++){
    vec2 pt=uPoints[i]; pt.x*=uAspect;
    float d=distance(w,pt);
    float wt=1.0/(pow(d,3.0)+0.0009);
    acc+=uPointColors[i]*wt; wsum+=wt;
  }
  return acc/wsum;
}
void main(){
  vec3 col = (uMode==0) ? aurora() : meshg();
  col+=(hash1(vUv*uRes+vec2(uSeed))-0.5)*uGrain;
  float vd=distance(vUv,vec2(0.5));
  col*=1.0-uVignette*smoothstep(0.32,0.98,vd);
  gl_FragColor=vec4(clamp(col,0.0,1.0),1.0);
}`;

function compile(
  gl: WebGLRenderingContext,
  type: number,
  src: string,
): WebGLShader {
  const sh = gl.createShader(type);
  if (!sh) throw new Error('cover: failed to create shader');
  gl.shaderSource(sh, src);
  gl.compileShader(sh);
  if (!gl.getShaderParameter(sh, gl.COMPILE_STATUS)) {
    const log = gl.getShaderInfoLog(sh);
    gl.deleteShader(sh);
    throw new Error(`cover: shader compile failed: ${log ?? 'unknown'}`);
  }
  return sh;
}

export interface GLCoverRenderer {
  /** Render `params` into the bound canvas. `time` drives ambient motion (0 = still). */
  render(params: CoverParams, time?: number): void;
  dispose(): void;
}

/**
 * Build a reusable renderer bound to `canvas`. The canvas must not have had a
 * non-WebGL context requested previously. Throws if WebGL is unavailable.
 */
export function createGLCoverRenderer(
  canvas: HTMLCanvasElement,
): GLCoverRenderer {
  const gl = canvas.getContext('webgl', {
    antialias: true,
    preserveDrawingBuffer: true,
  });
  if (!gl) throw new Error('cover: WebGL is not available');

  const prog = gl.createProgram();
  if (!prog) throw new Error('cover: failed to create program');
  const vs = compile(gl, gl.VERTEX_SHADER, VERT);
  const fs = compile(gl, gl.FRAGMENT_SHADER, FRAG);
  gl.attachShader(prog, vs);
  gl.attachShader(prog, fs);
  gl.linkProgram(prog);
  if (!gl.getProgramParameter(prog, gl.LINK_STATUS)) {
    throw new Error(
      `cover: program link failed: ${gl.getProgramInfoLog(prog) ?? 'unknown'}`,
    );
  }
  gl.useProgram(prog);

  const buf = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, buf);
  gl.bufferData(
    gl.ARRAY_BUFFER,
    new Float32Array([-1, -1, 3, -1, -1, 3]),
    gl.STATIC_DRAW,
  );
  const aPos = gl.getAttribLocation(prog, 'aPos');
  gl.enableVertexAttribArray(aPos);
  gl.vertexAttribPointer(aPos, 2, gl.FLOAT, false, 0, 0);

  const u = (n: string): WebGLUniformLocation | null =>
    gl.getUniformLocation(prog, n);
  const uni = {
    time: u('uTime'),
    seed: u('uSeed'),
    grain: u('uGrain'),
    vig: u('uVignette'),
    scale: u('uScale'),
    warp: u('uWarp'),
    aspect: u('uAspect'),
    mode: u('uMode'),
    res: u('uRes'),
    colors: u('uColors[0]'),
    points: u('uPoints[0]'),
    pcolors: u('uPointColors[0]'),
  };
  const aspect = canvas.width / canvas.height;

  function render(params: CoverParams, time = 0): void {
    if (!gl) return;
    const d = deriveSeed(params.seed);
    const pal = buildPalette(d, params.hueShift, params.mood);
    const bg = buildBg(d, params.hueShift, params.mood);

    const cols = new Float32Array(12);
    pal.forEach((c, i) => cols.set(c, i * 3));
    const pts = new Float32Array(12);
    d.points.slice(0, 6).forEach((p, i) => pts.set(p, i * 2));
    const pcRgb = [pal[0], pal[1], pal[2], pal[3], pal[1], bg];
    const pc = new Float32Array(18);
    pcRgb.forEach((c, i) => pc.set(c, i * 3));

    gl.uniform3fv(uni.colors, cols);
    gl.uniform2fv(uni.points, pts);
    gl.uniform3fv(uni.pcolors, pc);
    gl.uniform1f(uni.seed, (strHash(params.seed) % 10000) / 97.0);
    gl.uniform1f(uni.grain, params.grain);
    gl.uniform1f(uni.vig, params.vignette);
    gl.uniform1f(uni.aspect, aspect);
    gl.uniform2f(uni.res, canvas.width, canvas.height);
    gl.uniform1f(uni.scale, 1.1 + params.complexity * 2.6);
    gl.uniform1f(
      uni.warp,
      params.algo === 'mesh' ? params.complexity * 1.7 : 2.4,
    );
    gl.uniform1i(uni.mode, params.algo === 'mesh' ? 1 : 0);
    gl.uniform1f(uni.time, time);
    gl.viewport(0, 0, canvas.width, canvas.height);
    gl.drawArrays(gl.TRIANGLES, 0, 3);
  }

  function dispose(): void {
    if (!gl) return;
    gl.deleteProgram(prog);
    gl.deleteShader(vs);
    gl.deleteShader(fs);
    gl.deleteBuffer(buf);
  }

  return { render, dispose };
}

export { COVER_HEIGHT, COVER_WIDTH };
