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
 ** JOGL2 port of my PC 4k intro competition entry for Revision 2012. This is the post-processing
 ** fragment shader that takes the raymarching shader output from the FBO and applies god-rays, tv-lines
 ** and noise to it to make the overall look more interesting and less 'sterile'.
 **
 ** This is the 'normal', unminified version I used during development.
 **
 ** For an impression how this routine looks like see here: http://www.youtube.com/watch?v=UjgRGDhgehA
 ** Original release from the Revision 2012 can be found here: http://www.pouet.net/prod.php?which=59086
 **
 ** This is a simple adaption to generate a stereoscopic image pair. It used kinda Toe-in (incorrect)
 ** method for stereo. See here for more information on calculating stereoscopic image pairs: 
 ** http://paulbourke.net/stereographics/stereorender/   
 **
 **/

uniform float tm;     //time
uniform vec2 rs;      //resolution
uniform sampler2D s0; //sampler0
uniform int sn;       //scene number
uniform float st;     //current_scene_timer
uniform float cm;     //godrays contrast_multiplier
uniform float eyeoffset;

void main(void) {
    vec2 custom_glFragCoord = gl_FragCoord.xy;
    //godrays
    vec2 position = custom_glFragCoord/(rs*2);
    vec2 temp_position = position;
    vec3 accumulation = vec3(0.0);
    int iterations = 150;
    float contrast;
    vec2 movement = vec2(1.0);

    float temp_scenetimer = st;
    int temp_scene_number = sn;
    vec2 random_seed_1 = vec2(12.9898,78.233);
    float random_seed_2 = 43758.5453;
    float noise = fract(sin(dot(custom_glFragCoord.xy , random_seed_1)) * random_seed_2+(temp_scenetimer*0.25))*(sin(tm+custom_glFragCoord.y)*2+4.0);      
    if (temp_scene_number>=1) {
       if (noise>temp_scenetimer) {
          temp_scene_number = temp_scene_number-1;
       }
    }
    temp_scenetimer = mod(tm*5,110.0);
    if (temp_scene_number==1) {
       movement.x = 0.5;
       contrast = 1.5;
    }
    if (temp_scene_number==2) {
       movement.y = -1.0;
       contrast = 5.0;
    }
    if (temp_scene_number==3) {
       contrast = 1.25;
    }
    if (temp_scene_number==4) {
       movement = vec2(3.0,-0.25);
       contrast = 2.0;
    }
    if (temp_scene_number==5) {
       movement.y = 0.0;
       contrast = 2.5;
    }
    if (temp_scene_number==6) {
       movement = vec2(0.75,0.75);
       contrast = 2.5;
    }
    if (temp_scene_number==7) {
       movement = vec2(0.0,1.0);
       contrast = 2.0;
    }
    if (temp_scene_number==8) {
       movement.y = 1.5;
       contrast = 2.5;
    }
    if (temp_scene_number==9) {
       movement.y = 0.95;
       contrast = 2.0;
    }
    contrast *= cm;
    
    movement.x += (eyeoffset*2);
    
    float fadefactor = 1.0/iterations;
    float multiplier = 1.0;
    for( int i=0; i<iterations; i++ ) {
       vec3 texturesample = texture2D(s0,position+temp_position).xyz;
       accumulation += multiplier*smoothstep(0.1,1.0,texturesample*texturesample);
       multiplier *= 1.0-fadefactor;
       temp_position += ((movement*0.25)-position)/iterations;
    }
    accumulation /= iterations;
    //contrast enhance to accentuate bright fragments
    vec3 color = texture2D(s0,custom_glFragCoord/rs).rgb+(accumulation*(contrast/(1.0+dot(position,position))));

    position = custom_glFragCoord / rs.xy;
    position.y *=-1.0;
      
    //circular vignette fade
    color *= 0.5 + 0.5*16.0*position.x*position.y*(1.0-position.x)*(-1.0-position.y);    
    //tvlines effect
   
    //color *= 1.0+0.3*sin(position.y*(rs.y*1.55));
    
    //tvflicker effect
    color *= 1.15+0.2*sin(1.75*tm);
    //crazy noise
    //float c = fract(sin(dot(position.xy ,random_seed_1)) * random_seed_2+(tm*0.5));
    //color = (color*0.85)+(color*0.25*vec3(c));
    gl_FragColor = vec4(color,1.0);
}