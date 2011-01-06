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
 ** Horizontal/vertical "Gaussian Blur" fragment shader used as blur pass for VSM (using hardcoded 
 ** gaussian weights). The "scale" uniform is used to configure the filter for horizontal or vertical
 ** dimension blur.
 **
 **/

uniform vec2 scale;
uniform sampler2D sampler;

void main() {
    vec4 color = vec4(0.0);
    color += texture2D( sampler, gl_TexCoord[0].st + vec2( -3.0*scale.x, -3.0*scale.y ) )*0.015625;
    color += texture2D( sampler, gl_TexCoord[0].st + vec2( -2.0*scale.x, -2.0*scale.y ) )*0.09375;
    color += texture2D( sampler, gl_TexCoord[0].st + vec2( -1.0*scale.x, -1.0*scale.y ) )*0.234375;
    color += texture2D( sampler, gl_TexCoord[0].st + vec2(  0.0        ,  0.0         ) )*0.3125;
    color += texture2D( sampler, gl_TexCoord[0].st + vec2(  1.0*scale.x,  1.0*scale.y ) )*0.234375;
    color += texture2D( sampler, gl_TexCoord[0].st + vec2(  2.0*scale.x,  2.0*scale.y ) )*0.09375;
    color += texture2D( sampler, gl_TexCoord[0].st + vec2(  3.0*scale.x, -3.0*scale.y ) )*0.015625;
    gl_FragColor = color;
}