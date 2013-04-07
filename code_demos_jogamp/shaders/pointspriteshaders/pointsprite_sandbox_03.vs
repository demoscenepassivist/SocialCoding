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

float orbitDistance = 0.025;
float waveLength = 200.0;

void main(void) {
    gl_TexCoord[0] = gl_MultiTexCoord0;
    vec2 p1 = (vec2(sin(time), cos(time))*orbitDistance)+0.5;
    vec2 p2 = (vec2(sin(time+3.142), cos(time+3.142))*orbitDistance)+0.5;
    float d1 = 1.-length(gl_TexCoord[0].st-p1);
    float d2 = 1.-length(gl_TexCoord[0].st.st-p2);

    float wave1 = sin(d1*waveLength+(time*5.))*0.5 + 0.5 * (((d1 - 0.5) * 2.) + 0.5);
    float wave2 = sin(d2*waveLength+(time*5.))*0.5+0.5 * (((d1 - 0.5) * 2.) + 0.5);
    float c = d1 > 0.99 || d2 > 0.99 ? 1. : 0.;
    c + wave1*wave2;
    frag_color = vec4(c + wave1*wave2,c,c,1.);

    float gray = dot(frag_color.rgb, vec3(0.299, 0.587, 0.114)); 
    vec4 raw_vertex = gl_Vertex;
    raw_vertex.z -= gray*15;        
    gl_Position = gl_ModelViewProjectionMatrix*raw_vertex;
    gl_PointSize = max(30.0-(gl_Position.z*0.125),4.0);
}
