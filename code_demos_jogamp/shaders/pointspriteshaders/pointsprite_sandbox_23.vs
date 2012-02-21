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

const float exponent = 0.15f;
const float speed = -10.0f;

void main(void) {
    gl_TexCoord[0] = gl_MultiTexCoord0;
    
    float rings = 50.0*sin(time*0.2);
    vec2 position = -4.0+2.0*gl_TexCoord[0].st*4.0;
    vec2 positionoffset1 = vec2(cos(time*0.43)*3.0,sin(time*0.83))*1.5;
    vec2 positionoffset2 = vec2(sin(time*0.8),cos(time*0.63))*1.5;
    vec2 spiralpos1 = position+positionoffset1;
    vec2 spiralpos2 = position+positionoffset2;
    float circleradius1 = sqrt(dot(spiralpos1,spiralpos1));
    float circleradius2 = sqrt(dot(spiralpos2,spiralpos2));
    float ang1 = atan(spiralpos1.y/spiralpos1.x);
    float rad1 = pow(dot(spiralpos1,spiralpos1),exponent);
    rad1 *= sin((gl_TexCoord[0].s*resolution.x)*0.001+(time*0.1));    
    vec4 value1 = vec4(sin(ang1+rings*rad1+speed*time));
    vec4 color1 = vec4(circleradius1/5.0,(circleradius1/5.0+circleradius2/5.0)/1.5,circleradius2/5.0,1.0);
    value1 = vec4(0.5,0.5,0.5,0.5)*(vec4(1.0,1.0,1.0,1.0)+value1);
    if(spiralpos1.x<0.0) {   
        value1 = 1.0-value1;
    }
    float ang2 = atan(spiralpos2.y/spiralpos2.x);
    float rad2 = pow(dot(spiralpos2,spiralpos2),exponent);
    rad2 *= sin((gl_TexCoord[0].t*resolution.y)*0.001+(time*0.1));    
    vec4 value2 =  vec4(sin(ang2+rings*rad2+speed*time));
    value2 = vec4(0.5,0.5,0.5,0.5)*(vec4(1.0,1.0,1.0,1.0)+value2);
    vec4 color2 = vec4(circleradius1/5.0,(circleradius2/5.0+circleradius1/5.0)/2.5,circleradius1/3.0,1.0);
    if(spiralpos2.x<0.0) {   
        value2 = 1.0-value2;
    }
    frag_color = value1*color1*value2*color2;
    
    float gray = dot(frag_color.rgb, vec3(0.299, 0.587, 0.114)); 
    vec4 raw_vertex = gl_Vertex;
    raw_vertex.z -= min(gray*20.0,40.0);      
    gl_Position = gl_ModelViewProjectionMatrix*raw_vertex;
    gl_PointSize = max(30.0-(gl_Position.z*0.125),4.0);
}


