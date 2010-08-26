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
 ** Postprocessing shader implementing basic 'SUBSTRACT' blending mode. For explanation of the different
 ** blending modes see the original Porter-Duff paper: http://dev.processing.org/bugs/attachment.cgi?id=71
 ** or for more up2date formulas take a look here: http://www.nathanm.com/photoshop-blending-math/ and
 ** here http://dunnbypaul.net/blends/
 **
 **/

uniform sampler2D sampler0;
uniform sampler2D sampler1;
uniform float opacity;

void main(void) {
    vec4 blend = texture2D(sampler0, gl_TexCoord[0].st);
    vec4 base = texture2D(sampler1, gl_TexCoord[0].st);
    vec4 result = base-blend;
         result = clamp(result,0.0,1.0);
    gl_FragColor = mix(base,result,opacity);
}