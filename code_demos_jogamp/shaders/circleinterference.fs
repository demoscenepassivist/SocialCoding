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
 ** Fragment shader to procedurally generate some kind of 'circle interference' pattern known
 ** from the good old AMIGAAA!!! (bitplane based xor interference patterns) on a fullscreen billboard.
 **
 **/

uniform float time;
uniform vec2 resolution;
uniform float variation;

void main(void) {
    vec2 position = -1.0+2.0*gl_FragCoord.xy/resolution.xy;
    //compensate for aspect ratio distortion ...
    position.x *= resolution.x/resolution.y;
    vec2 positionoffset1 = vec2(cos(time*0.43),sin(time*0.83))*1.5;
    vec2 positionoffset2 = vec2(sin(time*0.8),cos(time*0.63))*1.5;
    float circleradius1 = sqrt(dot(position+positionoffset1,position+positionoffset1));
    float circleradius2 = sqrt(dot(position-positionoffset2,position-positionoffset2));
    float moduloradius1 = mod(circleradius1,0.1);
    float moduloradius2 = mod(circleradius2,0.1);
    bool modulocompare1 = moduloradius1>0.05;
    bool modulocompare2 = moduloradius2>0.05;
    vec4 color1;
    vec4 color2;
    //classic xor black and white look ...
    if (variation==0.0) {
        color1 = vec4(0.0,0.0,0.0,1.0);
        color2 = vec4(1.0,1.0,1.0,1.0);
    }
    //more colorful xor variation ...
    if (variation==1.0) {
        color1 = vec4(circleradius1/7.0,(circleradius1/7.0+circleradius2/7.0)/1.5,circleradius2/7.0,1.0);
        color2 = vec4(1.0,1.0,1.0,1.0);
    }
    //tried to emulate a 3 bitplane amiga xor, but failed i guess :)
    if (variation==2.0) {
        float shaded2;
        if (modulocompare2) { shaded2 = moduloradius1*25.0; }
        if (modulocompare1) { shaded2 = moduloradius2*25.0; }
        float shaded1 = 1.0;
        if (modulocompare2) { shaded1 = moduloradius1*15.0; }
        if (modulocompare1) { shaded1 = moduloradius2*15.0; }
        color1 = vec4(circleradius1/3.0*shaded1*0.75,(circleradius1/3.0+circleradius2/3.0)/1.5*shaded1,circleradius2/3.0*shaded1,1.0);
        color2 = vec4(shaded2,shaded2,shaded2,1.0)*color1;
        color1 = color2;
    }
    //artificial xor operation ... X-)
    vec4 fragcolor = color1;
    if (modulocompare1) { fragcolor = color2; }
    if (modulocompare2) { fragcolor = color2; }
    if ((modulocompare1) && (modulocompare2)) { fragcolor = color1; }
    gl_FragColor = fragcolor;
}