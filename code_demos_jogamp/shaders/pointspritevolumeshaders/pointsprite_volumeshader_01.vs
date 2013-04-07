/**
 **   __ __|_  ___________________________________________________________________________  ___|__ __
 **  //    /\                                           _                                  /\    \\  
 ** //____/  \__     __ _____ _____ _____ _____ _____  | |     __ _____ _____ __        __/  \____\\ 
 **  \    \  / /  __|  |     |   __|  _  |     |  _  | | |  __|  |     |   __|  |      /\ \  /    /  
 **   \____\/_/  |  |  |  |  |  |  |     | | | |   __| | | |  |  |  |  |  |  |  |__   "  \_\/____/   
 **  /\    \     |_____|_____|_____|__|__|_|_|_|__|    | | |_____|_____|_____|_____|  _  /    /\     
 ** /  \____\                       http://jogamp.org  |_|                              /____/  \    
 ** \  /   "' _________________________________________________________________________ `"   \  /    
 **  \/____.                                                                             .____\/     
 **
 ** Simple vertex shader that calculates a volume noise function that resembles some
 ** sort of 'plasma'-ish look for a voxel represented by a pointsprite (experimental state).
 ** The base code for the shader was taken from Mr.Doobs "GLSL Sandbox" wich can be found 
 ** here: http://glsl.heroku.com/. Some minor adjsutments were made to get the shader to 
 ** generate a volume plasma.
 **
 ** Remark: This routine currently has a couple of rendering problems on NVidia GPUs.
 **/

uniform sampler2D sampler0;
varying float depth;
varying vec4 color;
uniform float time;

uniform float pointspritesize;

float phi = 2.0/(1.0+sqrt(5.0));
float vx_offset = 0.54;
float rt_w = 0.5; // GeeXLab built-in
float rt_h = 0.5; // GeeXLab built-in
float hatch_y_offset = 1.; // 5.0
float lum_threshold_1 = 1.0; // 1.0
float lum_threshold_2 = 0.7; // 0.7
float lum_threshold_3 = 0.5; // 0.5
float lum_threshold_4 = 0.3; // 0.3

vec3 mod289(vec3 x)
{
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 mod289(vec4 x)
{
  return x - floor(x  *(1.0 / 289.0)) * 289.0;
}

vec4 permute(vec4 x)
{
  return mod289(((x *34.0)+1.0)*x);
}

vec4 taylorInvSqrt(vec4 r)
{
  return 1.79284291400159 - 0.85373472095314 * r * phi;
}

vec3 fade(vec3 t) {
  return t*t*t*(t*(t*6.0-15.0)+10.0);
}

float cnoise(vec3 P)
{
  vec3 Pi0 = floor(P); // Integer part for indexing
  vec3 Pi1 = Pi0 + vec3(1.0); // Integer part + 1
  Pi0 = mod289(Pi0);
  Pi1 = mod289(Pi1);
  vec3 Pf0 = fract(P); // Fractional part for interpolation
  vec3 Pf1 = Pf0 - vec3(1.0); // Fractional part - 1.0
  vec4 ix = vec4(Pi0.x, Pi1.x, Pi0.x, Pi1.x);
  vec4 iy = vec4(Pi0.yy, Pi1.yy);
  vec4 iz0 = Pi0.zzzz;
  vec4 iz1 = Pi1.zzzz;

  vec4 ixy = permute(permute(ix) + iy);
  vec4 ixy0 = permute(ixy + iz0);
  vec4 ixy1 = permute(ixy + iz1);

  vec4 gx0 = ixy0 * (1.0 / 7.0);
  vec4 gy0 = fract(floor(gx0) * (1.0 / 7.0)) - 0.5;
  gx0 = fract(gx0);
  vec4 gz0 = vec4(0.5) - abs(gx0) - abs(gy0);
  vec4 sz0 = step(gz0, vec4(0.0));
  gx0 -= sz0 * (step(0.0, gx0) - 0.5);
  gy0 -= sz0 * (step(0.0, gy0) - 0.5);

  vec4 gx1 = ixy1 * (1.0 / 7.0);
  vec4 gy1 = fract(floor(gx1) * (1.0 / 7.0)) - 0.5;
  gx1 = fract(gx1);
  vec4 gz1 = vec4(0.5) - abs(gx1) - abs(gy1);
  vec4 sz1 = step(gz1, vec4(0.0));
  gx1 -= sz1 * (step(0.0, gx1) - 0.5);
  gy1 -= sz1 * (step(0.0, gy1) - 0.5);

  vec3 g000 = vec3(gx0.x,gy0.x,gz0.x);
  vec3 g100 = vec3(gx0.y,gy0.y,gz0.y);
  vec3 g010 = vec3(gx0.z,gy0.z,gz0.z);
  vec3 g110 = vec3(gx0.w,gy0.w,gz0.w);
  vec3 g001 = vec3(gx1.x,gy1.x,gz1.x);
  vec3 g101 = vec3(gx1.y,gy1.y,gz1.y);
  vec3 g011 = vec3(gx1.z,gy1.z,gz1.z);
  vec3 g111 = vec3(gx1.w,gy1.w,gz1.w);

  vec4 norm0 = taylorInvSqrt(vec4(dot(g000, g000), dot(g010, g010), dot(g100, g100), dot(g110, g110)));
  g000 *= norm0.x;
  g010 *= norm0.y;
  g100 *= norm0.z;
  g110 *= norm0.w;
  vec4 norm1 = taylorInvSqrt(vec4(dot(g001, g001), dot(g011, g011), dot(g101, g101), dot(g111, g111)));
  g001 *= norm1.x;
  g011 *= norm1.y;
  g101 *= norm1.z;
  g111 *= norm1.w;

  float n000 = dot(g000, Pf0);
  float n100 = dot(g100, vec3(Pf1.x, Pf0.yz));
  float n010 = dot(g010, vec3(Pf0.x, Pf1.y, Pf0.z));
  float n110 = dot(g110, vec3(Pf1.xy, Pf0.z));
  float n001 = dot(g001, vec3(Pf0.xy, Pf1.z));
  float n101 = dot(g101, vec3(Pf1.x, Pf0.y, Pf1.z));
  float n011 = dot(g011, vec3(Pf0.x, Pf1.yz));
  float n111 = dot(g111, Pf1);

  vec3 fade_xyz = fade(Pf0);
  vec4 n_z = mix(vec4(n000, n100, n010, n110), vec4(n001, n101, n011, n111), fade_xyz.z);
  vec2 n_yz = mix(n_z.xy, n_z.zw, fade_xyz.y);
  float n_xyz = mix(n_yz.x, n_yz.y, fade_xyz.x); 
  return 2.2 * n_xyz;
}

float surface3 (vec3 coord) {
   float frequency = 4.0;
   float n = 0.0;   
   n += 1.0     * abs( cnoise( coord * frequency ) );
   n += 0.5     * abs( cnoise( coord * frequency * 2.0 ) );
   n += 0.25    * abs( cnoise( coord * frequency * 4.0 ) );
   n += 0.125   * abs( cnoise( coord * frequency * 8.0 ) );
   n += 0.0625  * abs( cnoise( coord * frequency * 16.0 ) );
   n += 0.03125 * abs( cnoise( coord * frequency * 32.0 ) );
   return n;
}
   
void main(void) {
   gl_TexCoord[0] = gl_MultiTexCoord0; 
   //float n = surface3(vec3(position, time * 0.1)*mat3(1,0,0,0,.8,.6,0,-.6,.8));
   float n = surface3(vec3(gl_MultiTexCoord0.x,gl_MultiTexCoord0.y,gl_MultiTexCoord0.z+time)*mat3(1,0,0,0,.8,.6,0,-.6,.8));
   //vec2 uv = position;
   float lum = length(n);
   //vec3 tc = pow(vec3(1.-lum),vec3(28.+(gl_MultiTexCoord0.x*100.0),14.1+(gl_MultiTexCoord0.y*200.0),7.));
   vec3 tc = pow(vec3(1.-lum),vec3(28.,9.1,7.));
   color = vec4(tc.x,tc.y*2, tc.z, 1.0);
   //vec4 displace_texture = texture3D(sampler1, gl_TexCoord[0].xyz);    
   //float gray = dot(displace_texture.rgb, vec3(0.299, 0.587, 0.114));
   vec4 raw_vertex = gl_Vertex;
   //raw_vertex.z -= gray*25;
   gl_Position = gl_ModelViewProjectionMatrix*raw_vertex;
   depth = gl_Position.z;
   //gl_PointSize = 16.0-(gl_Position.z*0.075);
   gl_PointSize = pointspritesize-(gl_Position.z*0.075);
   //gl_PointSize = 6.0;
}