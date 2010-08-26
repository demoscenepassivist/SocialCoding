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
 ** Postprocessing filter implementing a 'ROBERTS' linear convolution (edge detection) using
 ** either the horizontal or the vertical kernel.
 **
 **/
 
uniform sampler2D sampler0;
uniform vec2 tc_offset[9];

void main(void) {
    vec4 sample[9];
    for (int i = 0; i < 9; i++) {
        sample[i] = texture2D(sampler0, gl_TexCoord[0].st + tc_offset[i]);
    }

//  0 1     -1 0
// -1 0 und  0 1

    gl_FragColor = 1.0*sample[1] + -1.0*sample[3] + -1.0*sample[0] + 1.0*sample[4];
}