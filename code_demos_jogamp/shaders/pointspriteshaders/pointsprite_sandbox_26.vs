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
 ** Original can be found here: http://glsl.heroku.com/263/0
 **
 **/

varying vec4 frag_color;
uniform float time;
uniform vec2 resolution;
uniform sampler2D texture;


float box(float edge0, float edge1, float x) {
   return step(edge0, x) - step(edge1, x);
}

float ringShape(vec2 p, float t) {
   return clamp(box(t, t * 1.2, length(p)) - t, 0.0, 1.0);
}

float ringInstance(vec2 p, float t, float xden, float yden) {
   float th = floor(t) * 47.0;
   return ringShape(p - vec2(mod(th, xden) / xden, mod(th, yden) / yden) * 2.0 + 1.0, fract(t));
}

void main(void) {
    gl_TexCoord[0] = gl_MultiTexCoord0;
    
    vec2 p = (-1.0+2.0*gl_TexCoord[0].st)*vec2(resolution.x / resolution.y, 1.0);    
    float t = time / 3.0 + 5.0;
    frag_color.a    = 1.0;
    frag_color.rgb  = ringInstance(p, t - 0.0, 7.0,  13.0) * vec3(1.0, 0.0, 0.0) +
                      ringInstance(p, t - 0.6, 3.0,   5.0) * vec3(0.0, 1.0, 0.0) +
                      ringInstance(p, t - 0.2, 11.0, 23.0) * vec3(0.0, 0.0, 1.0) +
                      ringInstance(p, t - 0.9, 17.0, 19.0) * vec3(1.0, 0.0, 1.0);
    
    float gray = dot(frag_color.rgb, vec3(0.299, 0.587, 0.114)); 
    vec4 raw_vertex = gl_Vertex;
    raw_vertex.z -= min(gray*25.0,25.0);      
    gl_Position = gl_ModelViewProjectionMatrix*raw_vertex;
    gl_PointSize = max(30.0-(gl_Position.z*0.125),4.0);
}