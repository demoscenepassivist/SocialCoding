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
    vec2 position = -1.0+2.0*gl_TexCoord[0].st;
    position.x *= abs(sin(time*0.25));
    position.y *= abs(sin(time*0.25));
    bool modulox = mod(position.x,0.2)>0.1;
    bool moduloy = mod(position.y,0.2)>0.1;
    vec4 fragment;
    if (modulox) {
        fragment = vec4(1.0,1.0,1.0,0.0);
    }
    if (moduloy) {
        fragment = vec4(1.0,1.0,1.0,0.0);
    }
    if (modulox && moduloy) {
        fragment = vec4(0.25,0.25,0.25,0.0);
    }    
    frag_color = fragment;
    float gray = dot(frag_color.rgb, vec3(0.299, 0.587, 0.114)); 
    vec4 raw_vertex = gl_Vertex;
    raw_vertex.z -= min(gray*25.0,25.0);      
    gl_Position = gl_ModelViewProjectionMatrix*raw_vertex;
    gl_PointSize = max(30.0-(gl_Position.z*0.125),4.0);
}