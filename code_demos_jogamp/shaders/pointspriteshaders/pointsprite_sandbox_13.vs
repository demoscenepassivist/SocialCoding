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

vec4 getSample2D(vec2 position) {
    float divider = position.y*25;
    float modulox = mod(position.x,1.0/divider)*divider; 
    vec4 fragment;
    float red = abs(sin(position.x*position.y+time/5.0));
    float green = abs(sin(position.x*position.y+time/4.0));
    float blue = abs(sin(position.x*position.y+time/3.0));
    vec4 fragmentcolor = vec4(red,green,blue,1.0)*10;
    if (modulox>0.5) {
        fragment = vec4(modulox,modulox,modulox,1.0)/fragmentcolor;
    } else {
        fragment = vec4(1.0-modulox,1.0-modulox,1.0-modulox,1.0)/fragmentcolor;    
    }
    return fragment;
}

vec4 getVortex(vec2 position) {
    vec2 uv;    
    float a = atan(position.y,position.x);
    float r = sqrt(dot(position,position));
    float s = r*5; //* (1.0+0.8*cos(time*1.0));
    //float s = time*2;
    uv.x =          .02*position.y+.03*cos(-time+a*3.0)/s;
    uv.y = .1*time +.02*position.x+.03*sin(-time+a*3.0)/s;
    float w = .9 + pow(max(1.5-r,0.0),4.0);
    //w*=0.6+0.4*cos(time+3.0*a);
    vec3 col = getSample2D(uv*1.5).xyz;
    return vec4(col*w,1.0);
}

void main(void) {
    gl_TexCoord[0] = gl_MultiTexCoord0;

    frag_color = getVortex(-1.0+2.0*gl_TexCoord[0].st);

    float gray = dot(frag_color.rgb, vec3(0.299, 0.587, 0.114)); 
    vec4 raw_vertex = gl_Vertex;
    raw_vertex.z -= min(gray*25.0,25.0);      
    gl_Position = gl_ModelViewProjectionMatrix*raw_vertex;
    gl_PointSize = max(30.0-(gl_Position.z*0.125),4.0);
}



