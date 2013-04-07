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
 ** JOGL2 port of my PC 4k intro competition entry for Revision 2013. This is the post-processing
 ** fragment shader that takes the raymarching shader output from the FBO and applies radial blur,
 ** vignette, color abbrevation, tv-lines and noise to make the overall look more interesting and
 ** less 'sterile'.  As this shader is the 'orignal' minified version of the shader and a little
 ** bit hard to understand I also included the 'normal' version I used during development: 
 ** 'kohlenstoffeinheit_development_post.fs'.
 **
 ** For an impression how this routine looks like see here: http://www.youtube.com/watch?v=aFCcneO5HIA
 ** Original release from the Revision 2012 can be found here: http://www.pouet.net/prod.php?which=61181
 **/

uniform vec2 rs;
uniform float st;
uniform sampler2D s0;

float s(vec2 s) {
    return fract(sin(dot(s.xy,vec2(12.9898,78.233)))*43758.5+st*.5);
}

vec3 v(vec2 v) {
    vec2 t=pow(abs(vec2(v.x-.5,v.y-.5)),vec2(2));
    float x=s(v.xy)*.004*smoothstep(.05,.45,t.x+t.y)*5.5;
    vec2 y=v+x,r=v-x;
    return((texture2D(s0,y)+texture2D(s0,r)+texture2D(s0,vec2(y.x,r.y))+texture2D(s0,vec2(r.x,y.y)))/4.).xyz;
}

void main() {
    vec2 y=gl_FragCoord.xy/rs.xy,r=.9*(y.xy-.5);
    vec3 t=vec3(v(1.0675*r+.5).x,v(1.045*r+.5).y,v(1.0225*r+.5).z);
    y.y*=-1.;
    t*=.5+8.*y.x*y.y*(1.-y.x)*(-1.-y.y);
    t*=1.+.3*sin(y.y*(rs.y*1.55));
    t*=1.45+.3*sin(1.75*st);
    t=t*.85+t*.25*vec3(s(y));
    gl_FragColor=vec4(t,1.);
}