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
 ** Advanced fragment shader doing some kind of GPGPU calculation to render a Mandelbrot fractal
 ** zoom. Zooms in up to the limit of single precision floats on the center point in the complex
 ** plane given by xcenter/ycenter. The sampler1D is used as LUT to generate colors from the 
 ** number of iterations.
 **
 **/

uniform vec2 resolution;
uniform float time;
uniform sampler1D sampler0;
const float maxiterations = 512.0;
const float xcenter = 0.3245046418497685;
const float ycenter = 0.04855101129280834;

void main() {
    //complex plane location/movement setup ...
    //inspired by IQ's (http://www.iquilezles.org) cos/sin based zoom ...
    vec2 p = -1.0 + 2.0 * gl_FragCoord.xy / resolution.xy;
    p.x *= resolution.x/resolution.y;
    float zoom = .62+.38*sin(.1*time);
    float coa = cos(0.1*(1.0-zoom)*time);
    float sia = sin(0.1*(1.0-zoom)*time);
    zoom = pow(zoom,9.0);
    vec2 xy = vec2(p.x*coa-p.y*sia,p.x*sia+p.y*coa);
    vec2 cc = vec2(xcenter,ycenter)+xy*zoom;
    float real = cc.x;
    float imaginary = cc.y;
    float constantreal = real;
    float constantimaginary = imaginary;
    //calculate the iterated function system ...
    float r2 = 0.0;
    float iterations;
    for (iterations = 0.0; iterations<maxiterations && r2<4.0; ++iterations) {
        float tempreal = real;
        real = (tempreal*tempreal)-(imaginary*imaginary)+constantreal;
        imaginary = 2.0*tempreal*imaginary+constantimaginary;
        r2 = (real*real)+(imaginary*imaginary);
    }
    //base the color on the number of iterations using the 1D sampler as LUT ...
    if (r2<4.0) {
        gl_FragColor = vec4(0.0,0.0,0.0,1.0);
    } else {
        gl_FragColor = vec4(texture1D(sampler0, iterations/maxiterations).rgb, 1.0);
    }
}