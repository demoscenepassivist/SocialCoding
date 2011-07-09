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
 ** Originally planned for a 1K PC intro, this shader proceduarally generates an animated texture
 ** and use it as a base for one or multiple nested plane deformations. For an impression how this 
 ** shader looks like see here: http://www.youtube.com/watch?v=XXXXXXXXX
 **/

uniform float time;
uniform vec2 resolution;

vec4 getSample2D(vec2 position) {
    float divider = position.y*25;
    float modulox = mod(position.x,1.0/divider)*divider;
    vec4 fragment;
    float red = abs(sin(position.x*position.y+time/40.0));
    float green = abs(sin(position.x*position.y+time/30.0));
    float blue = abs(sin(position.x*position.y+time/20.0));
    vec4 fragmentcolor = vec4(red,green,blue,1.0)*10;
    if (modulox>0.5) {
        fragment = vec4(modulox,modulox,modulox,1.0)/fragmentcolor;
    } else {
        fragment = vec4(1.0-modulox,1.0-modulox,1.0-modulox,1.0)/fragmentcolor;
    }
    return fragment;
}

vec4 getVortex(vec2 position) {
    float r2 = sqrt(dot(position*((sin(time*0.005)+1.0)*(abs(sin(time*0.001)*3))),position*((sin(1*0.5)+1.0)*10)));
    return getSample2D(vec2(r2,r2));
}

vec4 getVortex2(vec2 position) {
    float a = atan(position.y,position.x);
    float r = sqrt(dot(position,position));
    vec2 uv;
    uv.x = cos(a)/r;
    uv.y = sin(a)/r;
    uv /= 10.0;
    uv += time*0.05;
    vec3 color = getVortex(uv).rgb;
    return vec4(color*r*1.5,1.0);
}

void main(void) {
    vec2 position = -1.0+2.0*gl_FragCoord.xy/resolution.xy;
    position.x *= resolution.x/resolution.y;
    //2xSS
    position.x += -1.0*(resolution.x/resolution.y);
    position.y += -1.0;
    //4xSS
    //position.x += -3.0*(resolution.x/resolution.y);
    //position.y += -3.0;
    gl_FragColor = getVortex2(position);
}