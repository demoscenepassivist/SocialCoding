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
 ** JOGL2 port of my PC 4k intro competition entry for Revision 2012. This is the raymarching fragment
 ** shader that is rendered to a fullscreen billboard. The shader basically encapsulates a 
 ** sphere-tracing based raymarcher for a single fractal formula with camera handling. The different 
 ** intro parts are all parameter and camera position variations of the same fractal.
 **
 ** This is the 'normal', unminified version I used during development.
 **
 ** Papers and articles you should be familiar with before trying to understand the code:
 **
 ** Distance rendering for fractals: http://www.iquilezles.org/www/articles/distancefractals/distancefractals.htm
 ** Ambient occlusion techniques: http://www.iquilezles.org/www/articles/ao/ao.htm
 ** Sphere tracing: A geometric method for the antialiased ray tracing of implicit surfaces: http://graphics.cs.uiuc.edu/~jch/papers/zeno.pdf
 ** Rendering fractals with distance estimation function: http://www.iquilezles.org/www/articles/mandelbulb/mandelbulb.htm
 **
 ** For an impression how this routine looks like see here: http://www.youtube.com/watch?v=UjgRGDhgehA
 ** Original release from the Revision 2012 can be found here: http://www.pouet.net/prod.php?which=59086
 **
 ** This is a simple adaption to generate a stereoscopic image pair. It used kinda Toe-in (incorrect)
 ** method for stereo. See here for more information on calculating stereoscopic image pairs: 
 ** http://paulbourke.net/stereographics/stereorender/   
 **
 **/

//base
uniform vec2 rs;  //resolution

//scene timing
uniform int sn;   //scene number
uniform float st; //intra scene timer
uniform float lt; //previous/last scene time

//camera
vec3  cp; //camera position
mat3  cr; //camera rotation

//coloring
float ff; //fog falloff

//ifs
float fu; //fractal_spheresubstract
float fd; //fractal_distancemult
vec3 cs;  //fractal_csize
float fs; //fractal_size
vec3 fc;  //fractal_c

uniform float eyeoffset;

float dE(vec3 p) {
   float dEfactor=1.;
   //int fractal_iterations = 12;
   for(int i=0;i<12;i++){
      //box folding
      p=2.*clamp(p, -cs, cs)-p;
      //inversion
      float k=max(fs/dot(p,p),1.);
      p*=k;
      dEfactor*=k;
      //julia seed
      p+=fc;
   }
   //call basic shape and scale its DE
   //need to adjust fractal_distancemult with non zero julia seed
   float rxy=length(p.xy)-fu;
   //distance from pos to the pseudo kleinian basic shape ...
   return (fd*max(rxy,abs(length(p.xy)*p.z)/sqrt(dot(p,p)))/abs(dEfactor));
}

vec4 rd(vec2 fragment_coordinates) {   
   //calculate ray direction from fragment coordinates ...
   vec2 ray_position = (0.5*rs-fragment_coordinates)/vec2(rs.x,-rs.y);
   
   //stereoscopic eye offset ...
   //ray_position.x -= 0.015; //right
   //ray_position.x += 0.015; //left
   
   //ray_position.x -= 0.0075; //right
   //ray_position.x += 0.0075; //left
   
   //ray_position.x -= 0.00325; //right
   //ray_position.x += 0.00325; //left
   
   ray_position.x += eyeoffset;
   
   ray_position.x *= (rs.x/rs.y); //aspect_ratio
   vec3 ray_direction = normalize(cr * vec3(ray_position.x * vec3(1, 0, 0) + ray_position.y * vec3(0, 1, 0) - .9 * vec3(0, 0, 1)));
   //sphere tracing initialization ...
   float ray_length = 0.0;
   vec3  ray = cp+ray_length*ray_direction;
   float epsilon = 0.0000006;
   float  calculated_distance;
   int   tracingsteps = 0;
   bool  hit = false;
   ray = cp+ray_length*ray_direction;
   //magic :) DO NOT TOUCH!
   float eps_factor = 2.0*.743294*(1.0/rs.y)*.6;
   //actual sphere tracing ...
   //#define tracingsteps_max 200
   for (int i=0; i<200; i++) {
      tracingsteps = i;
      calculated_distance = dE(ray);     
      //small stepback to remove 'fuzzel'-structures near inside shooting range
      //#define raylength_max 10000.0
      if (hit && calculated_distance.x<epsilon || ray_length>10000.) {
         tracingsteps--;
         break;
      }
      hit = false;
      ray_length += calculated_distance;
      ray = cp+ray_length*ray_direction;
      epsilon = ray_length * eps_factor;
      if (calculated_distance<epsilon) {
         hit = true;
      }
   }
   //---   
   //intersects ?
   //#define tracingsteps_max 200
   float glow_amount = float(tracingsteps)/float(200);
   vec4 color_primary = vec4(1.0,1.0,1.0,0.45);             //alpha-channel represents intensity
   vec4 color_background_primary = vec4(1.0,1.0,0.0,1.0);
   vec4 color_background_secondary = vec4(0.0,0.0,0.0,1.0);
   vec4 color_glow_inside = vec4(0.0,1.0,1.0,0.55);         //alpha-channel represents intensity 
   float aof = 1.0;
   //asin(1.0)=pi/2
   vec4  bg_color = vec4(clamp(mix(color_background_secondary.rgb, color_background_primary.rgb, (sin(ray_direction.y * asin(1.0)) + 1.0) * 0.5), 0.0, 1.0), 1.0);
   vec4 color = color_primary;
   if (hit) {
      float glow = clamp(glow_amount * color_glow_inside.a * 3.0, 0.0, 1.0);
      //---      
      //calculate_normal: gradient calculation in x,y and z from intersection position
      //#define normal_min 1.5e-7
      float epsilon_normal = max(epsilon*0.5,1.5e-7);
      vec3 normal = normalize(vec3(
         dE(ray+vec3(epsilon_normal, 0, 0)).x-dE(ray-vec3(epsilon_normal, 0, 0)).x,
         dE(ray+vec3(0, epsilon_normal, 0)).x-dE(ray-vec3(0, epsilon_normal, 0)).x,
         dE(ray+vec3(0, 0, epsilon_normal)).x-dE(ray-vec3(0, 0, epsilon_normal)).x)
      );
      //---
      //AO approximation: http://www.iquilezles.org/www/material/nvscene2008/rwwtt.pdf
      float occlusion_factor = 1.0;
      //float ambientocclusion_spread = 9.00000;
      //float ambientocclusion_intensity = 0.15000;   
      //float surface_offset = epsilon;
      //surface_offset *= 9.;
      float surface_offset = epsilon*9.;
      float ao_contribution = .15/surface_offset;
      //start with small offset from surface ...
      float surface_distance = 2.0*surface_offset;
      //#define ambientocclusion_iterations 4
      for (int i=0; i<4; ++i) {
         occlusion_factor -= (surface_distance-dE(ray+normal*surface_distance).x)*ao_contribution;
         surface_distance += surface_offset;
         //contribution lowers with distance to surface
         ao_contribution *= 0.5; 
      }
      aof = clamp(occlusion_factor, 0.0, 1.0);
      //--- 
      //blinn phong shading model
      //base color, incident, point of intersection, normal
      //ambient colour based on background gradient
      //asin(1.0)=pi/2  
      vec3 ambColor = clamp(mix(color_background_secondary.rgb, color_background_primary.rgb, (sin(normal.y * asin(1.0)) + 1.0) * 0.5), 0.0, 1.0);
      ambColor = mix(vec3(.5), ambColor, .3);
      //vec3 light_position = vec3(-16.00000,100.00000,-60.00000);
      float diffuse = max(dot(normal, normalize(vec3(-16.,100.,-60.)-ray)), 0.0);       
      //vec3 clamped_ambientcolor = clamp(color.rgb * color_primary.a, 0.0, 1.0);
      vec3 clamped_ambientcolor = color.rgb * color_primary.a;
      //float light_specular = 0.80000;
      //float light_specular_exponent = 4.00000;
      //float temp_specular = pow(diffuse, 4.);
      color.rgb = (ambColor * clamped_ambientcolor + clamped_ambientcolor * diffuse + pow(diffuse, 4.) * .8)*aof;
      color.rgb = mix(color.rgb, color_glow_inside.rgb, glow);
    } else {
        //Zzzzz ... no intersection ...
    } 
    //float color_fog_intensity = 0.01;   
    color.rgb = mix(bg_color.rgb, color.rgb, exp(-pow(ray_length * exp(ff), 2.0) * .01));
    return vec4(color.rgb,1.0);
}

//return rotation matrix for rotating around vector v by angle
mat3 mr(vec3 v, float angle) {
    float c = cos(radians(angle));
    float s = sin(radians(angle));
    return mat3(c+(1.0-c)*v.x*v.x   , (1.0-c)*v.x*v.y-s*v.z  , (1.0-c)*v.x*v.z+s*v.y,
               (1.0-c)*v.x*v.y+s*v.z, c+(1.0-c)*v.y*v.y      , (1.0-c)*v.y*v.z-s*v.x,
               (1.0-c)*v.x*v.z-s*v.y, (1.0 - c)*v.y*v.z+s*v.x, c+(1.0-c)*v.z*v.z);
}

void main() {
    //camera
    float camera_roll = 0.;
    float camera_pitch = 90.;
    float camera_yaw = 0.;
    fd = 0.763;
    fu = 10.0;
    fs = 1.0;
    fc = vec3(0);
    ff = -0.50000;
    cs = vec3(0.80800,0.80800,1.16700);
    float gamma_correction = 1.00000;    
    //float temp_scenetimer = clamp(current_scene_timer,0.0,100.0);
    float temp_scenetimer = st;
    int temp_scene_number = sn;
    vec2 custom_glFragCoord = gl_FragCoord.xy;    
    float noise = fract(sin(dot(custom_glFragCoord.xy ,vec2(12.9898,78.233))) * 43758.5453+(temp_scenetimer*0.25))*(sin(temp_scenetimer+custom_glFragCoord.y)*2+4.0); 
    if (temp_scene_number>=1) {
       if (noise>temp_scenetimer) {
          temp_scenetimer = lt;
          temp_scene_number = temp_scene_number-1;
       }
    }  
    //#define scene_fadein 0 
    if (temp_scene_number==0) {
       gamma_correction = clamp(temp_scenetimer*0.055,0.0,1.0);
       camera_pitch = 23.9;
       cp = vec3(0.8,-1.16339+16.0-temp_scenetimer*0.095,-1.80153);
       cs.y = 0.58000;
    }
    //#define scene_wires_sidescroller 1
    if (temp_scene_number==1) {
       cp = vec3(0.7212+temp_scenetimer*0.00068,0.10000,-2.35000);
       cs.xy = vec2(0.50000);
    }
    //#define scene_wires_insidescroller 2
    if (temp_scene_number==2) {
       cp = vec3(0.7+(-0.0092+temp_scenetimer*0.000180),0.1,-2.3495);
       cs.xy = vec2(0.50000);
    }
    //#define scene_grid_hallofharad 3
    if (temp_scene_number==3) {
       fu = 1.01000;
       cp = vec3(temp_scenetimer*0.05,0.02000,2.00000-100.0*0.1);
       cs.x = 0.90000;
    }
    //#define scene_grid_sideoverview 4
    if (temp_scene_number==4) {
       fu = 1.01000;
       cp = vec3(0.0007,0.02000,0.10000-temp_scenetimer*0.00028);
       camera_pitch = 0.0;
       camera_yaw = temp_scenetimer*0.009*180;
       cs.x = 0.90000;
    }
    //#define scene_spike_verticalcore 5    
    if (temp_scene_number==5) {
       fu = 0.9;
       cp = vec3(0.0+temp_scenetimer*0.00025,1.40000+0.004,-2.38200+(-0.05+temp_scenetimer*0.002));
    }
    //#define scene_spike_introduction 6  
    if (temp_scene_number==6) {  
       cp = vec3(1.18000,0.08000+temp_scenetimer*0.000055,-0.24000);
       camera_pitch = 120.0;
       cs = vec3(0.50000,0.50000,1.04000);
    }
    //#define scene_spike_wiredballsvertical 7   
    if (temp_scene_number==7) {
       fd = 0.70000;
       fs = 1.34000;
       cp = vec3(0.69+(-0.0092+temp_scenetimer*0.0008),0.1,-2.3495);
       camera_pitch = 130+sin(temp_scenetimer*0.075)*50;
       camera_roll = 90.0;
       cs.xy = vec2(0.50000);
    }
    //#define scene_alien_backbone 8
    if (temp_scene_number==8) {
       cp = vec3(0.0,-0.86000+temp_scenetimer*0.0003,0.30000);
       camera_pitch = 80+(temp_scenetimer*0.65);     
       fc.z = -0.38000;
    }    
    float brightness = 0.0;
    //#define scene_alien_reactorcore 9
    if (temp_scene_number==9) {
       gamma_correction = clamp(temp_scenetimer*0.025,1.0,100.0);
       fu = 1.20000;
       cp = vec3(0.00,1.40000+(-0.020+temp_scenetimer*0.0018),-2.34100);
       fc.z = 0.25840;
       ff =  clamp(temp_scenetimer*0.05,3.6,100.0);
       brightness = clamp((temp_scenetimer-35)*0.005,0.0,1.0);       
    }
    cr = mr(vec3(0, 1, 0), 180.0 - camera_yaw) * mr(vec3(1, 0, 0), -camera_pitch) * mr(vec3(0, 0, 1), camera_roll);    
    //super sampling support
    vec4 color = vec4(0.0);
#define antialiasing 0.5       
#ifdef antialiasing
    float n = 0.0;
    for (float x = 0.0; x < 1.0; x += float(antialiasing)) {
        for (float y = 0.0; y < 1.0; y += float(antialiasing)) {
            color += rd(custom_glFragCoord.xy + vec2(x, y));
            n += 1.0;
        }
    }
    color /= n;
#else
    color = rd(custom_glFragCoord.xy);
#endif    
    //output
    gl_FragColor = vec4(pow(color.rgb+brightness, vec3(1.0 / gamma_correction)), color.a);
} 