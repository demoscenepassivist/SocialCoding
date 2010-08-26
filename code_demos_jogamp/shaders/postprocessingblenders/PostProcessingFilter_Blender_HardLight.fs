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
 ** Postprocessing shader implementing basic 'HARDLIGHT' blending mode. For explanation of the different
 ** blending modes see the original Porter-Duff paper: http://dev.processing.org/bugs/attachment.cgi?id=71
 ** or for more up2date formulas take a look here: http://www.nathanm.com/photoshop-blending-math/ and
 ** here http://dunnbypaul.net/blends/
 **
 **/

uniform sampler2D sampler0;
uniform sampler2D sampler1;
uniform float opacity;
const vec4 white = vec4(1.0,1.0,1.0,1.0);
const vec4 lumCoeff = vec4(0.2125,0.7154,0.0721,1.0);

void main(void) {
    vec4 blend = texture2D(sampler0, gl_TexCoord[0].st);
    vec4 base = texture2D(sampler1, gl_TexCoord[0].st);
    vec4 result;
    float luminance = dot(blend,lumCoeff);
    if (luminance<0.45) {
        result = 2.0*blend*base;
    } else if (luminance>0.55) {
        result = white-2.0*(white-blend)*(white-base);
    } else {
        vec4 result1 = 2.0*blend*base;
        vec4 result2 = white-2.0*(white-blend)*(white-base);
        result = mix(result1,result2,(luminance-0.45)*10.0);
    }
    result = clamp(result,0.0,1.0);  
    gl_FragColor = mix(base,result,opacity);
}