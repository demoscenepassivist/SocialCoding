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
 ** Fragment shader to procedurally generate some kind of 'blob interference' pattern known
 ** from the good old AMIGAAA!!! (copperplasma/bitplane based interference patterns) on a 
 ** fullscreen billboard.
 **
 **/

uniform float time;
uniform vec2 resolution;
uniform float variation;

vec4 getSample(vec2 position) {
    float color = 0.0;
    position *= 1.5;
    float red = abs(sin(position.x*position.y+time/5.0));
    float green = abs(cos(position.x*position.y+time/4.0));
    float blue = abs(sin(position.x*position.y+time/3.0));
    color += sin(position.x*cos(time/15.0)*80.0)+cos(position.y*cos(time/15.0)*10.0);
    color += sin(position.y*sin(time/15.0)*40.0)+cos(position.x*sin(time/15.0)*20.0);
    return vec4(color*red*cos(time*0.5),color*green,color*blue*sin(time),1.0);
}

vec4 getVortex(vec2 position) {
    float a = atan(position.y, position.x);
    float r = sqrt(dot(position,position));
    a *= 1.0;
    r *= -0.25;
    vec2 uv;
    uv.x = cos(a)/r;
    uv.y = sin(a)/r;
    uv /= 5.0;
    vec3 color = getSample(uv).rgb;
    return vec4(color*r*1.5,1.0);
}

void main(void) {
    vec2 position = -1.0+2.0*gl_FragCoord.xy/resolution.xy;
    vec4 vortex1 = getVortex(vec2(position.x,position.y));
    gl_FragColor = vortex1;
}