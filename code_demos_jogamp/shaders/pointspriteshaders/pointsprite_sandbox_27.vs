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
 ** Original can be found here: http://glsl.heroku.com/380/0 (by @paulofalcao Blobs)
 **
 **/

varying vec4 frag_color;
uniform float time;
uniform vec2 resolution;
uniform sampler2D texture;

float makePoint(float x,float y,float fx,float fy,float sx,float sy,float t){
   float xx=x+sin(t*fx)*sx;
   float yy=y+cos(t*fy)*sy;
   return 1.0/sqrt(xx*xx+yy*yy);
}

void main(void) {
    gl_TexCoord[0] = gl_MultiTexCoord0;
    
    vec2 p = -0.25+0.5*gl_TexCoord[0].st;    
    p=p*2.0;
    float x=p.x;
    float y=p.y;
    float a=makePoint(x,y,3.3,2.9,0.3,0.3,time);
    a=a+makePoint(x,y,1.9,2.0,0.4,0.4,time);
    a=a+makePoint(x,y,0.8,0.7,0.4,0.5,time);
    a=a+makePoint(x,y,2.3,0.1,0.6,0.3,time);
    a=a+makePoint(x,y,0.8,1.7,0.5,0.4,time);
    a=a+makePoint(x,y,0.3,1.0,0.4,0.4,time);
    a=a+makePoint(x,y,1.4,1.7,0.4,0.5,time);
    a=a+makePoint(x,y,1.3,2.1,0.6,0.3,time);
    a=a+makePoint(x,y,1.8,1.7,0.5,0.4,time);   
    float b=makePoint(x,y,1.2,1.9,0.3,0.3,time);
    b=b+makePoint(x,y,0.7,2.7,0.4,0.4,time);
    b=b+makePoint(x,y,1.4,0.6,0.4,0.5,time);
    b=b+makePoint(x,y,2.6,0.4,0.6,0.3,time);
    b=b+makePoint(x,y,0.7,1.4,0.5,0.4,time);
    b=b+makePoint(x,y,0.7,1.7,0.4,0.4,time);
    b=b+makePoint(x,y,0.8,0.5,0.4,0.5,time);
    b=b+makePoint(x,y,1.4,0.9,0.6,0.3,time);
    b=b+makePoint(x,y,0.7,1.3,0.5,0.4,time);
    float c=makePoint(x,y,3.7,0.3,0.3,0.3,time);
    c=c+makePoint(x,y,1.9,1.3,0.4,0.4,time);
    c=c+makePoint(x,y,0.8,0.9,0.4,0.5,time);
    c=c+makePoint(x,y,1.2,1.7,0.6,0.3,time);
    c=c+makePoint(x,y,0.3,0.6,0.5,0.4,time);
    c=c+makePoint(x,y,0.3,0.3,0.4,0.4,time);
    c=c+makePoint(x,y,1.4,0.8,0.4,0.5,time);
    c=c+makePoint(x,y,0.2,0.6,0.6,0.3,time);
    c=c+makePoint(x,y,1.3,0.5,0.5,0.4,time);
    vec3 d=normalize(vec3(a,b,c));
    frag_color = vec4(d.x,d.y,d.z,1.0);
    float gray = dot(frag_color.rgb, vec3(0.299, 0.587, 0.114)); 
    vec4 raw_vertex = gl_Vertex;
    raw_vertex.z -= gray*75.0;      
    gl_Position = gl_ModelViewProjectionMatrix*raw_vertex;
    gl_PointSize = max(30.0-(gl_Position.z*0.125),4.0);
}