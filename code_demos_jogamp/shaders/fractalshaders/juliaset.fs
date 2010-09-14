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
 ** Advanced fragment shader doing some kind of GPGPU calculation to render a Julia set fractal
 ** transformation. The sampler1D is used as LUT to generate colors from the number of iterations.
 **
 **/

uniform vec2 resolution;
uniform float time;
uniform sampler1D sampler0;
const float maxiterations = 64.0;

void main(void) {
    vec2 p =-1.0+2.0*gl_FragCoord.xy/resolution.xy;
    //complex plane location/movement setup ...
    //inspired by IQ's (http://www.iquilezles.org) cos/sin based movement ...
    vec2 cc = vec2(cos(0.15*time),sin(0.15*time*1.423));
    vec2 z = p*vec2(1.33,1.0);
    float iterations;
    for(iterations=0; iterations<maxiterations; iterations++) {
        z = cc + vec2(z.x*z.x-z.y*z.y,2.0*z.x*z.y);
        if(dot(z,z)>100.0) {
            break;
        }
    }
    gl_FragColor = vec4(texture1D(sampler0, iterations/maxiterations).rgb, 1.0);
}