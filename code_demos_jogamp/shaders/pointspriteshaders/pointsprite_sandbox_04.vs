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
    
    vec2 position = gl_TexCoord[0].st;
    float t = time + position.x;
    vec2 o, off;
    o = 1.0 - abs(2.0 * fract(15.0 * position) - 1.0);
    off = .5 * o + .5 * pow(o, vec2(7));
    float grid2 = .5 * max(off.x, off.y);
    o = 1.0 - abs(2.0 * fract(1.0 * position) - 1.0);
    off = .5 * o + .5 * pow(o, vec2(11));
    float grid1 = .5 * max(off.x, off.y);
    vec4 grid = vec4(0, .5 * grid2 + grid1, 0, 1);
    float f = clamp(1.0 - abs(position.y - .3 * sin(t*6.0)-.5), 0.0, 1.0);
    float func1 = pow(f, 22.0);
    vec4 func = .7 * vec4(func1, func1, func1, 1);
    frag_color = grid + func;
    
    float gray = dot(frag_color.rgb, vec3(0.299, 0.587, 0.114)); 
    vec4 raw_vertex = gl_Vertex;
    raw_vertex.z -= gray*15;        
    gl_Position = gl_ModelViewProjectionMatrix*raw_vertex;
    gl_PointSize = max(30.0-(gl_Position.z*0.125),4.0);
}