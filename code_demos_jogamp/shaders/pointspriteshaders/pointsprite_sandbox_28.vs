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
 ** Original can be found here: http://glsl.heroku.com/389/0 (by @mnstrmnch)
 **
 **/

varying vec4 frag_color;
uniform float time;
uniform vec2 resolution;
uniform sampler2D texture;

float barWidth = 0.05;
float PI = 3.14159265;

vec3 c0 = vec3( 255.0, 215.0, 0.0 ) / vec3( 255.0 );
vec3 c1 = vec3( 1.0, 01.0, 01.0 );

void main(void) {
    gl_TexCoord[0] = gl_MultiTexCoord0;
    
    vec2 p = -1.0+2.0*gl_TexCoord[0].st; 
    p.y *= 0.95;
    barWidth = (sin(15.05*p.x+18.*time)+2.)*0.05+.005;
    vec3 color = vec3( .5*abs(p.y), 0.1, 0.1 );
    for( int i = 2; i >= 0; i-- ) {
      float barY = sin( time * 1.55324 + float( i ) * 1.0 + p.x ) * 0.533 + sin( time * .194 + p.x * 03.5 ) * 0.2;
      if( p.y > barY - barWidth * 01.5 && p.y < barY + barWidth * 0.5 ) {
         float angle = ( ( p.y - ( barY - barWidth * 0.5 ) ) / barWidth );
         color = mix(  c0+sin(p.x), c1, float( i ) / 47.0 ) * vec3( sin( angle * PI )  );
      }  
    }
    for( int i = 1; i >= 0; i-- ) {
      float barX = sin( time * 1.1347 + float( i ) * 0.3 + p.y * 0.25 ) * 0.25 + sin( time * 1. + float( i ) * 0.1 ) * 0.25;
      if( p.y < ( float( i ) / 47.0 ) * 2.0 - 1.0  && p.x > barX - barWidth * 0.5 && p.x < barX + barWidth * 0.5 ) {
         float angle = ( ( p.x - ( barX - barWidth * 0.5 ) ) / barWidth );
         color = mix( c0, c1, float( i ) / 47.0 ) * vec3( sin( angle * PI ) );
      }
    }

    frag_color = vec4(color,1.0);
    float gray = dot(frag_color.rgb, vec3(0.299, 0.587, 0.114)); 
    vec4 raw_vertex = gl_Vertex;
    raw_vertex.z -= gray*20.0;      
    gl_Position = gl_ModelViewProjectionMatrix*raw_vertex;
    gl_PointSize = max(30.0-(gl_Position.z*0.125),4.0);
}
