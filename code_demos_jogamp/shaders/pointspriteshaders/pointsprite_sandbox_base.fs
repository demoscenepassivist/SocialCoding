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
 ** Basic point sprite fragment shader sampling one texture and getting a on-the-fly generated 
 ** varying from the vertex shader (a procedural generated fragment). The texture is the 
 ** pointsprite itself and varying is the fragment that should be represented by the 2D pointsprite. 
 ** Both fragment are multiplied to color the pointsprite in the color of the generated image.
 **
 **/

uniform sampler2D sampler0;
varying vec4 frag_color;

void main() {
    vec4 pointsprite_fragment = texture2D(sampler0, gl_PointCoord);
    vec3 processed_frag_color = frag_color.rgb*vec3(0.4,0.4,0.4);
    gl_FragColor = vec4(processed_frag_color*texture2D(sampler0, gl_PointCoord).rgb,1.0);
}