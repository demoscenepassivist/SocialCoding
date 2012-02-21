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

float GetHeight( vec2 p ) {
   float height = sin( length( p + vec2( sin( time * 0.94 ), cos( time ) ) * 0.15 ) * 100.0 );
   height += sin( length( p + vec2( sin( time * 0.74 ), cos( time * 0.65 ) ) * 0.15 ) * 100.0 );
   height += sin( length( p + vec2( sin( time * 0.54 ), cos( time * 0.85 ) ) * 0.15 ) * 100.0 );
   return ( height / 3.0 ) * 0.05;
}

float arFix = 1.0 / resolution.x;

void main(void) {
    gl_TexCoord[0] = gl_MultiTexCoord0;
    
    vec2 centrePos = ( (gl_TexCoord[0].st*resolution.xy) - resolution.xy * 0.5 ) * arFix;
    vec2 rightPos = ( (gl_TexCoord[0].st*resolution.xy) + vec2( 0.5, 0.0 ) - resolution.xy * 0.5 ) * arFix;
    vec2 downPos = ( (gl_TexCoord[0].st*resolution.xy) + vec2( 0.0, 0.5 ) - resolution.xy * 0.5 ) * arFix;
    vec3 centre = vec3( centrePos, GetHeight( centrePos ) );
    vec3 right = vec3( rightPos, GetHeight( rightPos ) );
    vec3 down = vec3( downPos, GetHeight( downPos ) );
    vec3 normal = normalize( cross( right - centre, down - centre ) );
    vec3 light = vec3( sin( time ) * 0.5, cos( time * 1.13 ) * 0.125, 1.0 ) - centre;
    vec3 color = dot( normal, normalize( light ) ) * ( 1.0 / pow( length( light ), 128.0 ) ) * vec3( 1.0, 0.0, 0.0 );
    light = vec3( sin( time * 0.5 ) * 0.5, cos( time * 0.73 ) * 0.125, 1.0 ) - centre;
    color += dot( normal, normalize( light ) ) * ( 1.0 / pow( length( light ), 128.0 ) ) * vec3( 0.0, 1.0, 0.0 );
    light = vec3( sin( time * 1.34 ) * 0.5, cos( time * 0.93 ) * 0.125, 1.0 ) - centre;
    color += dot( normal, normalize( light ) ) * ( 1.0 / pow( length( light ), 128.0 ) ) * vec3( 0.0, 0.0, 1.0 );
    frag_color = vec4( color, 1.0 );
        
    float gray = dot(frag_color.rgb, vec3(0.299, 0.587, 0.114)); 
    vec4 raw_vertex = gl_Vertex;
    raw_vertex.z -= gray*0.25;        
    gl_Position = gl_ModelViewProjectionMatrix*raw_vertex;
    gl_PointSize = max(30.0-(gl_Position.z*0.125),4.0);
}