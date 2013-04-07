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
 ** JOGL2 port of my PC 4k intro competition entry for Revision 2013. Sure it got a little bigger 
 ** while porting but the shader and control code remained more or less untouched. The intro renders
 ** a fullscreen billboard using a single fragment shader. The shader basically encapsulates a 
 ** sphere-tracing based raymarcher for a single fractal formula with camera handling. The rendering 
 ** technique is a little bit different than usual as the renderer accumulates the spheretraced distances 
 ** of the estimation function over the ray-length, and converts that into a pixel brightness, resulting in
 ** some sort of "X-Ray" look for the marched volume.
 **
 ** Additionally a second post-processing shader is applied to the render output from the raymarching shader.
 ** Post effects are radial blur, vignette, color abbrevation, tv-lines and noise to make the overall look 
 ** more interesting and less 'sterile'. The different intro parts are all parameter and camera position 
 ** variations of the same fractal. 
 **
 ** This is the 'normal', unminified version I used during development.
 **
 ** Papers and articles you should be familiar with before trying to understand the code:
 **
 ** Distance rendering for fractals: http://www.iquilezles.org/www/articles/distancefractals/distancefractals.htm
 ** Sphere tracing: A geometric method for the antialiased ray tracing of implicit surfaces: http://graphics.cs.uiuc.edu/~jch/papers/zeno.pdf
 ** Rendering fractals with distance estimation function: http://www.iquilezles.org/www/articles/mandelbulb/mandelbulb.htm
 **
 ** For an impression how this routine looks like see here: http://www.youtube.com/watch?v=aFCcneO5HIA
 ** Original release from the Revision 2012 can be found here: http://www.pouet.net/prod.php?which=61181
 **/

//base
uniform vec2 rs;  //resolution

//scene timing
uniform int sn;  //scene number
uniform float st; //scene timer
uniform float lt; //previous/last scene time

int fractaliterations,
    animationmode = 0; //0=none 1=simple 2=jellyfish
vec2 fractalfold;
mat3 camerarotation;        //camera rotation
vec3 cameraposition,
     camerarollyawnpitch,
     fractaljulia,
     fractalrotation,
     basecolor,
     backgroundcolor;

float fractalrotationangle,
      organicscenetimer,
      fractalscale,      
      brightness,
      fade,
      maxdistance;

//return rotation matrix for rotating around vector v by angle
mat3 mr(vec3 v, float angle) {
    float c = cos(radians(angle));
    float s = sin(radians(angle));
    return mat3(c+(1.0-c)*v.x*v.x   , (1.0-c)*v.x*v.y-s*v.z  , (1.0-c)*v.x*v.z+s*v.y,
               (1.0-c)*v.x*v.y+s*v.z, c+(1.0-c)*v.y*v.y      , (1.0-c)*v.y*v.z-s*v.x,
               (1.0-c)*v.x*v.z-s*v.y, (1.0 - c)*v.y*v.z+s*v.x, c+(1.0-c)*v.z*v.z);
}

float de(vec3 pos) {
   //geometry animation mode:
   //0=none 
   //1=simple 
   //2=jellyfish
   vec3 ifsposition = pos, 
        juliaposition = fractaljulia,
   //animationmode 1
        ifsps=vec3(0), 
        ifspr=vec3(0), 
        ifspa=vec3(0), 
        ifsspeed=vec3(0), 
        movementspeed=vec3(0), 
        movementamplitude=vec3(0),
        sizevariation = vec3(0,0.01,0.01),
        rotatevariation = vec3(0,0,0.1),
        axisspeeds = vec3(1,-0.82692,0.65384),
        amplitudes = vec3(0.1,0.1,0.1),
        amplitudevariation = vec3(0.0,0.1,0.1),
        speedvariation = vec3(0.29546,-0.52272,1);

   vec2 scalingbounds = vec2(1.0,3.0);
   
   bool lockspeed = false;
   
   float amplitudevariationamount = 0.54237,
         speedvariationamount = 0.54,
         sizevariationstrength = 0.001,
         rotationvariationamount = 0.005,
         speedcorrection = 0.34146,
         amplitudecorrection = 0.04615;

   //jellyfish mode
   if (animationmode==2) {
      ifsposition+=vec3(0,0,-1)*organicscenetimer*0.2069*10.0;
      ifsposition+=vec3(0,0,-1)*(0.8+sin(organicscenetimer*2.5974+10))*1.7241;
      float wz=0.6067*0.6067;
      ifsposition+=cos(ifsposition*0.8048/wz+organicscenetimer*0.7058*10.0)*0.3297*.5*wz;
   }
   
   //animationmode 2
   if (animationmode==2) {
      sizevariation = vec3(0);
      sizevariationstrength = 0.0;
      rotatevariation = vec3(0);
      rotationvariationamount = 0.0;
      scalingbounds = vec2(1.19,2.5); //****
      axisspeeds = vec3(1,0.5,0.5);
      amplitudes = vec3(0.5,1.25,0);
      lockspeed = true;
      speedcorrection = 0.13415;
      amplitudecorrection = 0.15; //***
      amplitudevariation = vec3(0);
      amplitudevariationamount = 0.0;
      speedvariation = vec3(0.13636,0,0);
      speedvariationamount = 0.0;
   }
   
   ifsps=ifsposition*sizevariation*fractalscale*sizevariationstrength*.02;
   ifspr=ifsposition*rotatevariation*rotationvariationamount*.2;

   ifspa=ifsposition*amplitudevariation*amplitudevariationamount*.5;
   ifsspeed=ifsposition*speedvariation*speedvariationamount*2.0;
   movementspeed=(axisspeeds*speedcorrection*10.0);
   movementamplitude=(amplitudes+ifspa)*amplitudecorrection*.5;
   if (lockspeed) {
      movementspeed=vec3(movementspeed.x,movementspeed.x,movementspeed.x)*2.;
   }
   
   vec3 ifsanimation=vec3(
      sin(organicscenetimer*movementspeed.x+ifsspeed.x)*movementamplitude.x,
      cos(organicscenetimer*movementspeed.y+ifsspeed.y)*movementamplitude.y,
      sin(organicscenetimer*movementspeed.z+ifsspeed.z)*movementamplitude.z
   );
   
   float sc=clamp(fractalscale+(ifsps.x+ifsps.y+ifsps.z),scalingbounds.x,scalingbounds.y);
   
   mat3 rot = mr(normalize(fractalrotation+ifsanimation+ifspr), fractalrotationangle);
   
   if (animationmode==0) {
      rot = mr(normalize(fractalrotation), fractalrotationangle);
   }

   int i;
   for (i=0; i<fractaliterations; i++) {
      ifsposition*=rot;
      ifsposition.xy=abs(ifsposition.xy+fractalfold.xy)-fractalfold.xy;
      ifsposition=ifsposition*sc+juliaposition;
   }
   return length(ifsposition)*pow(sc, -float(i));
}

vec3 rd(vec2 fragment_coordinates) {   
   //calculate ray direction from fragment coordinates ...
   vec2 ray_position = (0.5*rs-fragment_coordinates)/vec2(rs.x,-rs.y);
   ray_position.x *= (rs.x/rs.y); //aspect_ratio
   vec3 direction = normalize(camerarotation * vec3(ray_position.x * vec3(1, 0, 0) + ray_position.y * vec3(0, 1, 0) - .9 * vec3(0, 0, 1)));
   float distanceestimation = 0.0,
         xrayintensity=0.0,
         accumulateddistance = 0.0,
         previousdistanceestimation;
   //int maximumraysteps = 250;
   for (int steps=0; steps<250; steps++) {
      previousdistanceestimation = distanceestimation;
      distanceestimation = de(cameraposition + accumulateddistance * direction);
      accumulateddistance += distanceestimation;
      if (accumulateddistance > maxdistance) break;
      xrayintensity+=exp(-1.0*abs(distanceestimation-previousdistanceestimation))-sqrt(accumulateddistance)*fade;
   }
   return vec3(mix(backgroundcolor,basecolor, xrayintensity*brightness*.02));
}

void cm(float factor,vec3 basecolor0, vec3 basecolor1, vec3 backgroundcolor0, vec3 backgroundcolor1) {
   basecolor = mix(basecolor0,basecolor1,factor);
   backgroundcolor = mix(backgroundcolor0,backgroundcolor1,factor);
}

void as(int currentscenenumber, float temp_scenetimer) {
      
   vec3 basecolor_0 = vec3(0.95,0.85,1.0),
        backgroundcolor_0 = vec3(0),
        basecolor_1 = vec3(0.8,1.0,0.7),
        backgroundcolor_1 = vec3(0.1,0.05,0.18),
        basecolor_2 = vec3(0.7,1.0,1.0),
        backgroundcolor_2 = vec3(0.05,0.05,0.08);
   
   //common parameters
   animationmode = 0;
   brightness= 1.0;
   fade=0.3;
   maxdistance=25.0;
   fractaliterations = 25;
   
   //for 6 and 99
   fractalscale = 1.33857;
   fractalfold = vec2(0,0.268);
   fractaljulia = vec3(-0.932,-0.39,-1.211);
   fractalrotation = vec3(-0.304,-0.239,-0.739);
   
   //neuro flyby (end/start)
   if (currentscenenumber==0) {
      cameraposition = vec3(.14,0.09+temp_scenetimer*0.08,6.51);
      fractaliterations = 35;
      fractalscale = 1.22;
      fractalfold = vec2(0.569,1);
      fractaljulia = vec3(-0.57,0.16,-1.31);
      fractalrotation = vec3(0.76,-0.17,-0.52);
      fractalrotationangle = 84;
      maxdistance= 1.9;
      brightness = clamp(temp_scenetimer*0.05,0.0,1.0);
      camerarollyawnpitch = vec3(99,195,497);
      cm(0.0,basecolor_0,basecolor_1,backgroundcolor_0,backgroundcolor_1);
   }
   
   //zoom out surface
   if (currentscenenumber==1) {  
      cameraposition = vec3(-3.10+temp_scenetimer*0.025,-.75+temp_scenetimer*0.001,.82);
      fractaliterations = 20;
      fractalscale = 1.5;
      fractalfold = vec2(.24,12.85);
      fractaljulia = vec3(.45,-1.0,-.31);
      fractalrotation = vec3(-.08,.60,-.93);
      fractalrotationangle = 49;   
      camerarollyawnpitch = vec3(75,70,148);
      cm(0.2,basecolor_0,basecolor_1,backgroundcolor_0,backgroundcolor_1);
   }
   
   //cells
   if (currentscenenumber==2) {
      organicscenetimer = temp_scenetimer*0.08;
      cameraposition = vec3(4.02,-2.67,2.23);
      fractaliterations = 16;
      fractalscale = 1.4+(temp_scenetimer*0.00125);
      fractalfold = vec2(0.91,1);
      fractaljulia = vec3(-2,0.0,-0.19);
      fractalrotation = vec3(0.08,-0.13,0.12);
      fractalrotationangle = -53;
      camerarollyawnpitch = vec3(62+temp_scenetimer*3,78,492);    
      animationmode = 1;
      cm(0.4,basecolor_0,basecolor_1,backgroundcolor_0,backgroundcolor_1);
   }
      
   //organic surface grow
   if (currentscenenumber==3) {
      cameraposition = vec3(6.65,-7.35,-3.69);
      fractalscale = 0.2+((1050.0-(temp_scenetimer*2.75))*0.001)+0.22;
      fractalfold = vec2(1);
      fractaljulia = vec3(-1.72,0.44,-0.34);
      fractalrotation = vec3(0.08,-0.10,-0.97);
      fractalrotationangle = 110;
      fade=0.1;
      camerarollyawnpitch = vec3(818,530,852);
      cm(0.8,basecolor_0,basecolor_1,backgroundcolor_0,backgroundcolor_1);
   }
   
   //organic surface
   if (currentscenenumber==4) {
      //need very high precision floats ...
      cameraposition = vec3(3.01,-3.5+temp_scenetimer*0.07,-2.19);
      fractaliterations = 60;
      fractalscale = 1.14765;
      fractalfold = vec2(0.91057,1);
      fractaljulia = vec3(-1.72034,0.44068,-0.34745);
      fractalrotation = vec3(0.08696,-0.1087,-0.97826);
      fractalrotationangle = 110.603;
      fade=0.23;
      camerarollyawnpitch = vec3(77,331,351);
      cm(0.1,basecolor_1,basecolor_2,backgroundcolor_1,backgroundcolor_2);
   }
   
   //fungi
   if (currentscenenumber==5) {   
      organicscenetimer = temp_scenetimer*0.1;
      cameraposition = vec3(7.1,7.87,.43);
      fractaliterations = 45;
      fractalscale = 1.155;
      fractalfold = vec2(1);
      fractaljulia = vec3(-2,0.389,-0.372);
      fractalrotation = vec3(0.086,-0.130,0.149);
      fractalrotationangle = -45;
      brightness= 0.7;
      fade=0.1;
      maxdistance = 4.0+temp_scenetimer*0.2;
      camerarollyawnpitch = vec3(76,98,238); 
      animationmode = 1;
      cm(1.0,basecolor_0,basecolor_1,backgroundcolor_0,backgroundcolor_1);
   }
      
   //cells camera flyby
   if (currentscenenumber==6) { 
      cameraposition = vec3(.5,-1.67+temp_scenetimer*0.012,3.9);
      fractalrotationangle = -46;
      camerarollyawnpitch = vec3(79,369,341);
      cm(0.2,basecolor_1,basecolor_2,backgroundcolor_1,backgroundcolor_2);
   }
   
   //cellular surface background
   if (currentscenenumber==99) {
      organicscenetimer = temp_scenetimer*0.07;
      cameraposition = vec3(.46,.53,3.6);
      fractalrotationangle = -58;
      brightness= 0.5;
      fade=0.1;
      camerarollyawnpitch = vec3(79,406,341);
      cm(0.4,basecolor_1,basecolor_2,backgroundcolor_1,backgroundcolor_2);
      animationmode = 1;
   } 
   
   //jellyfish
   if (currentscenenumber==7) {
      organicscenetimer = -19.0+(temp_scenetimer*0.17);
      cameraposition = vec3(7.,-22.36,-15.31);
      fractalscale = 1.11;
      fractalfold = vec2(0.94,0.96);
      fractaljulia = vec3(-1.72,0.41,-0.34);
      fractalrotation = vec3(0.08,-0.13,-0.98);
      fractalrotationangle = 106;
      maxdistance=50.0;
      fade=0.09;
      camerarollyawnpitch = vec3(176,53,110);
      animationmode = 2;
      cm(0.6,basecolor_1,basecolor_2,backgroundcolor_1,backgroundcolor_2);
   }
   
   //antarctica
   if (currentscenenumber==8) {   
      cameraposition = vec3(1.26-1.78*(temp_scenetimer*0.01),-2.92+1.41*(temp_scenetimer*0.01),.88);
      fractaliterations = 19;
      fractalscale = 1.38+temp_scenetimer*0.0015;
      fractalfold = vec2(0.34,0.78);
      fractaljulia = vec3(-1.18,-0.93,-0.37);
      fractalrotation = vec3(-0.08,0.6,-0.93);
      fractalrotationangle = 49;
      camerarollyawnpitch = vec3(74,71,118);
      cm(0.8,basecolor_1,basecolor_2,backgroundcolor_1,backgroundcolor_2);
   }
   
   //tree surface camerasweep
   if (currentscenenumber==9) {
      cameraposition = vec3(.44,-1.47,3.93);
      fractaliterations = 30;
      fractalrotationangle = -33;
      fade=0.4;
      camerarollyawnpitch = vec3(63,254,171+temp_scenetimer*2);
      cm(1.0,basecolor_1,basecolor_2,backgroundcolor_1,backgroundcolor_2);
   }
      
   //initial cell blob densifies
   if (currentscenenumber==10) {
      organicscenetimer = temp_scenetimer*0.25;
      cameraposition = vec3(7.,8.8,.49);
      fractaliterations = 15;
      fractalscale = 1.06+(temp_scenetimer*0.004);
      fractalfold = vec2(0.48,1.04);
      fractaljulia = vec3(-1.44,-.0901,-.212);
      fractalrotation = vec3(0.086,-0.130,0.149);
      fractalrotationangle = -46;
      fade=0.07+temp_scenetimer*0.0025;
      camerarollyawnpitch = vec3(76,93,238); 
      animationmode = 1;
      cm(1.0,basecolor_1,basecolor_2,backgroundcolor_1,backgroundcolor_2);
   }
     
   camerarotation = mr(vec3(0, 1, 0), 180.0 - camerarollyawnpitch.y) * mr(vec3(1, 0, 0), -camerarollyawnpitch.z) * mr(vec3(0, 0, 1), camerarollyawnpitch.x);
}

vec3 ss(vec2 offsets) {
    float temp_scenetimer = st;
    vec2 custom_glFragCoord = gl_FragCoord.xy+offsets;
    float noise = fract(sin(dot(custom_glFragCoord.xy ,vec2(12.9898,78.233))) * 43758.5453+(temp_scenetimer*0.25))*(sin(temp_scenetimer+custom_glFragCoord.y)*2+4.0); 
    int temp_scene_number = sn;
    if (temp_scene_number>=1) {
        if (noise>temp_scenetimer) {
            temp_scenetimer = lt;
            temp_scene_number = temp_scene_number-1;           
        } 
   } 
   as(temp_scene_number,temp_scenetimer);
   vec3 color = vec3(0);
   if (temp_scene_number==7) {
      as(99,temp_scenetimer);
      fractaliterations = 17;//16;
      color += rd(custom_glFragCoord)*0.8;
      as(7,temp_scenetimer);      
      //to remove background artifacts
      color *= clamp(rd(custom_glFragCoord),0.15,1.0)*7;
   } else if (temp_scene_number==5) {
      as(99,temp_scenetimer);
      fractaliterations = 13;
      color += rd(custom_glFragCoord)*1.0;
      as(5,temp_scenetimer);      
      //to remove background artifacts
      color *= clamp(rd(custom_glFragCoord),0.05,1.0)*6;
   } else {
      color = rd(custom_glFragCoord);
   }
   return color;
}

void main() {
   //super sampling support
   vec3 color = vec3(0);
//#define antialiasing 0.5       
#ifdef antialiasing
    float n = 0.0;
    for (float x = 0.0; x < 1.0; x += float(antialiasing)) {
        for (float y = 0.0; y < 1.0; y += float(antialiasing)) {          
            color += ss(vec2(x,y));
            n += 1.0;
        }
    }
    color /= n;
#else
   color = ss(vec2(0));
#endif    
    //output  
    gl_FragColor = vec4(color, 1.0);
}
