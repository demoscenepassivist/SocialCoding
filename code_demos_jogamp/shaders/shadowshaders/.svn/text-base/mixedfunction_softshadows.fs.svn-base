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
 ** Simple fragment shader very similar to the postprocessing shader implementing 'MULTIPLY'
 ** blending mode. Used to mix the black&white fake soft shadow map with the scene color rendering. 
 **
 **/

uniform sampler2D sampler0; //shadowmap
uniform sampler2D sampler1; //colormap

void main(void) {
    vec4 shadow = texture2D(sampler0, gl_TexCoord[0].st);
    vec4 color = texture2D(sampler1, gl_TexCoord[0].st);
    gl_FragColor = color*shadow;
}
