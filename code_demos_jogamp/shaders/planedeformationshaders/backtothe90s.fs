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
 ** Simple plane deformation fragment shader to render some kind of vortex to a billboard. 
 ** Shader code is from this site: http://www.iquilezles.org/blog/?p=1236
 **
 **/

uniform float time;
uniform vec2 resolution;

void main(void) {
    vec2 p = (2.0*gl_FragCoord.xy-resolution)/resolution.y;

    float r = length(p);
    float a = atan(p.x,p.y);

    float f = cos(p.y*12.0/(r*r) + time)*cos(p.x* 3.0/(r*r) + time);
    vec3 col = vec3( cos(a+0.0), cos(a+0.5), cos(a+1.0) ) *f + f*f*f*f + 0.2/r;

    gl_FragColor = vec4(col,1.0);
}