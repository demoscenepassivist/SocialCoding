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

void main(void) {
    gl_TexCoord[0] = gl_MultiTexCoord0;
    
    vec2 position = gl_TexCoord[0].st;
    frag_color += sin( position.x * cos( time / 15.0 ) * 80.0 ) + cos( position.y * cos( time / 15.0 ) * 10.0 );
    frag_color += sin( position.y * sin( time / 10.0 ) * 40.0 ) + cos( position.x * sin( time / 25.0 ) * 40.0 );
    frag_color += sin( position.x * sin( time / 5.0 ) * 10.0 ) + sin( position.y * sin( time / 35.0 ) * 80.0 );
    frag_color *= sin( time / 10.0 ) * 0.5;
    frag_color = vec4( vec3( frag_color.r, frag_color.g * 0.5, sin( frag_color.b + time / 3.0 ) * 0.75 ), 1.0 );
    
    float gray = dot(frag_color.rgb, vec3(0.299, 0.587, 0.114)); 
    vec4 raw_vertex = gl_Vertex;
    raw_vertex.z -= gray*15;    
    gl_Position = gl_ModelViewProjectionMatrix*raw_vertex;
    gl_PointSize = max(30.0-(gl_Position.z*0.125),4.0);
}