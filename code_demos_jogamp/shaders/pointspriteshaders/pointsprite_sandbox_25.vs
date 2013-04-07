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

vec4 getSample(vec2 position) {
        float color = 0.0;
        position *= 1.5;
        float red = abs( sin( position.x * position.y + time / 5.0 ) );
        float green = abs( cos( position.x * position.y + time / 4.0 ) );
        float blue = abs( sin( position.x * position.y + time / 3.0 ) );
        color += sin(position.x*cos(time/15.0)*80.0)+cos(position.y*cos(time/15.0)*10.0);
        color += sin(position.y*sin(time/15.0)*40.0)+cos(position.x*sin(time/15.0)*20.0);
        return vec4(color*red*cos(time*0.5),color*green,color*blue*sin(time), 1.0 );
}

vec4 getVortex(vec2 position) {
        float a = atan( position.y, position.x);
        float r = sqrt( dot( position, position ) );
        a *= 1.0;
        r *= -0.25;
        vec2 uv;
        uv.x = cos( a ) / r;
        uv.y = sin( a ) / r;
        uv /= 5.0;
        vec3 color = getSample(uv).rgb;
        return vec4( color * r * 1.5, 1.0 );
}

void main(void) {
    gl_TexCoord[0] = gl_MultiTexCoord0;
    
    vec2 position = -1.0+2.0*gl_TexCoord[0].st;
    vec4 vortex1 = getVortex(vec2(position.x,position.y));
    frag_color = vortex1;
    
    float gray = dot(frag_color.rgb, vec3(0.299, 0.587, 0.114)); 
    vec4 raw_vertex = gl_Vertex;
    raw_vertex.z -= min(gray*25.0,25.0);      
    gl_Position = gl_ModelViewProjectionMatrix*raw_vertex;
    gl_PointSize = max(30.0-(gl_Position.z*0.125),4.0);
}