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
 ** Original can be found here: http://glsl.heroku.com/280/0 (by @mnstrmnch)
 **
 **/

varying vec4 frag_color;
uniform float time;
uniform vec2 resolution;
uniform sampler2D texture;

float barWidth = 0.05;
float PI = 3.14159265;

vec3 c0 = vec3( 255.0, 215.0, 0.0 ) / vec3( 255.0 );
vec3 c1 = vec3( 1.0, 0.0, 0.0 );

void main(void) {
    gl_TexCoord[0] = gl_MultiTexCoord0;
   
    vec2 p = -1.0+2.0*gl_TexCoord[0].st; 
    vec3 color = vec3( 0.0 );
    for( int i = 35; i >= 0; i-- ) {
        float barY = sin( time * 1.324 + float( i ) * 0.1 + p.x ) * 0.33 + sin( time * 1.194 + p.x * 0.75 ) * 0.35;
        if( p.y > barY - barWidth * 0.5 && p.y < barY + barWidth * 0.5 ) {
            float angle = ( ( p.y - ( barY - barWidth * 0.5 ) ) / barWidth );
            color = mix( c0.zyx, c1.zyx, float( i ) / 23.0 ) * vec3( sin( angle * PI ) );
        }
    }

    frag_color = vec4(color,1.0);
    float gray = dot(frag_color.rgb, vec3(0.299, 0.587, 0.114)); 
    vec4 raw_vertex = gl_Vertex;
    raw_vertex.z -= gray*20.0;      
    gl_Position = gl_ModelViewProjectionMatrix*raw_vertex;
    gl_PointSize = max(30.0-(gl_Position.z*0.125),4.0);
}