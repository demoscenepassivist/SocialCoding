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
 ** Procedural point sprite vertex shader generating some interesting patterns color- and
 ** depth wise. Most of these shaders are taken from Mr.Doobs "GLSL Sandbox" wich can be found
 ** here: http://glsl.heroku.com/ (credits+links are given if possible). Some minor adjustments
 ** have been made to integrate them here as pointsprite shaders. The rest of the procedural 
 ** shaders are from my "Monkey Mathica" series of procedural texture shaders.
 **
 **/

varying vec4 frag_color;

uniform float time;
uniform vec2 resolution;
uniform sampler2D texture;

#define pi2_inv 0.159154943091895335768883763372

float border(vec2 uv, float thickness){
   uv = fract(uv - vec2(0.5));
   uv = min(uv, vec2(1.)-uv)*2.;
// return 1.-length(uv-0.5)/thickness;
   return clamp(max(uv.x,uv.y)-1.+thickness,0.,1.)/thickness;;
}

vec2 spiralzoom(vec2 domain, vec2 center, float n, float spiral_factor, float zoom_factor, vec2 pos){
   vec2 uv = domain - center;
   float d = length(uv);
   return vec2( atan(uv.y, uv.x)*n*pi2_inv + log(d)*spiral_factor, -log(d)*zoom_factor) + pos;
}

void main(void) {
    gl_TexCoord[0] = gl_MultiTexCoord0;

    vec2 uv = gl_TexCoord[0].st;
    uv = 0.5 + (uv - 0.5)*vec2(resolution.x/resolution.y,1.);
    uv = uv-0.5;
    vec2 spiral_uv = spiralzoom(uv,vec2(0.),8.,-.5,1.8,vec2(0.5,0.5)*time*0.5);
    vec2 spiral_uv2 = spiralzoom(uv,vec2(0.),3.,.9,1.2,vec2(-0.5,0.5)*time*.8);
    vec2 spiral_uv3 = spiralzoom(uv,vec2(0.),5.,.75,4.0,-vec2(0.5,0.5)*time*.7);
    frag_color = vec4(border(spiral_uv,0.9), border(spiral_uv2,0.9) ,border(spiral_uv3,0.9),1.);

    float gray = dot(frag_color.rgb, vec3(0.299, 0.587, 0.114)); 
    vec4 raw_vertex = gl_Vertex;
    raw_vertex.z -= gray*15;        
    gl_Position = gl_ModelViewProjectionMatrix*raw_vertex;
    gl_PointSize = max(30.0-(gl_Position.z*0.125),4.0);
}