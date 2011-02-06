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
 ** Fragment shader to procedurally generate some kind of 'spiral interference' pattern known
 ** from the good old AMIGAAA!!! (bitplane based xor interference patterns) on a fullscreen billboard.
 **
 **/

uniform float time;
uniform vec2 resolution;
uniform float variation;
const float exponent = 0.15f;
const float speed = -10.0f;

void main(void) {
    float rings = 50.0*sin(time*0.2);    
    vec2 position = -4.0+2.0*gl_FragCoord.xy/resolution.xy*4.0;
    position.x *= resolution.x/resolution.y;
    vec2 positionoffset1 = vec2(cos(time*0.43)*3.0,sin(time*0.83))*1.5;
    vec2 positionoffset2 = vec2(sin(time*0.8),cos(time*0.63))*1.5;
    vec2 spiralpos1 = position+positionoffset1;
    vec2 spiralpos2 = position+positionoffset2;
    float circleradius1 = sqrt(dot(spiralpos1,spiralpos1));
    float circleradius2 = sqrt(dot(spiralpos2,spiralpos2));

    float rad1 = pow(dot(spiralpos1,spiralpos1),exponent);

    if (variation==1.0 || variation==2.0) {
        rad1 *= sin(gl_FragCoord.x*0.001+(time*0.1));
    }

    float ang1 = atan(spiralpos1.y/spiralpos1.x); 
    vec4 value1 = vec4(sin(ang1+rings*rad1+speed*time));
    vec4 color1 = vec4(circleradius1/5.0,(circleradius1/5.0+circleradius2/5.0)/1.5,circleradius2/5.0,1.0);
    value1 = vec4(0.5,0.5,0.5,0.5)*(vec4(1.0,1.0,1.0,1.0)+value1);
    if(spiralpos1.x<0.0) {
        value1 = 1.0-value1;
    }

    float ang2 = atan(spiralpos2.y/spiralpos2.x);
    float rad2 = pow(dot(spiralpos2,spiralpos2),exponent);

    if (variation==1.0) {
        rad2 *= sin(gl_FragCoord.y*0.001+(time*0.1));  
    }

    vec4 value2 =  vec4(sin(ang2+rings*rad2+speed*time));
    value2 = vec4(0.5,0.5,0.5,0.5)*(vec4(1.0,1.0,1.0,1.0)+value2);
    vec4 color2 = vec4(circleradius1/5.0,(circleradius2/5.0+circleradius1/5.0)/2.5,circleradius1/3.0,1.0);
    if(spiralpos2.x<0.0) {
        value2 = 1.0-value2;
    }
    if (variation==2.0 || variation==3.0) {
        gl_FragColor = value1*color1;
    } else {
        gl_FragColor = value1*color1*value2*color2;
    }
}