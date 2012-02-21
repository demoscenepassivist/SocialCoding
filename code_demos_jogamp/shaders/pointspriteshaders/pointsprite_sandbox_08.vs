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

float p0 = 1.95;
float p1 = 3.169;
float p2 = 5.0;
float p3 = 3.0;

void main(void) {
    gl_TexCoord[0] = gl_MultiTexCoord0;

    vec2 pos = gl_TexCoord[0].st;
    float d0 = distance (pos, vec2(sin(time*.112), sin(time*.091)));
    float d1 = distance (pos, vec2(sin(time*.029), sin(time*.081)));
    float d2 = distance (pos, vec2(sin(time*.033), sin(time*.107)));
    float d3 = distance (pos, vec2(sin(time*.104), sin(time*.116)));
    float d4 = distance (pos, vec2(cos(time*.105), sin(time*.051)));
    float d5 = distance (pos, vec2(sin(time*.046), sin(time*.141)));
    float d6 = distance (pos, vec2(sin(time*.107), sin(time*.031)));
    float d7 = distance (pos, vec2(sin(time*.078), sin(time*.121)));
    float R = mod( +d0 +d1 +d2 +d3 -d4 -d5 -d6 -d7*.9, 1.0 );
    float G = mod( -d0 +d1 -d2 -d3 -d4 +d5 +d6*.9 +d7, 1.0 );
    float B = mod( -d0 +d1 -d2 +d3 +d4 +d5*.9 -d6 -d7, 1.0 );
    frag_color = vec4(p0 - p1 * normalize (p2 + p3 * log (vec3 (R, G, B))), 1.0);
    
    float gray = dot(frag_color.rgb, vec3(0.299, 0.587, 0.114)); 
    vec4 raw_vertex = gl_Vertex;
    raw_vertex.z -= gray*15;        
    gl_Position = gl_ModelViewProjectionMatrix*raw_vertex;
    gl_PointSize = max(30.0-(gl_Position.z*0.125),4.0);
}