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
 ** Postprocessing filter implementing a 'GAUSSIAN BLUR' linear convolution using a gaussian 
 ** (low-pass) 3x3 kernel)
 **
 **/

uniform sampler2D sampler0;
uniform vec2 tc_offset[9];

void main(void) {
    vec4 sample[9];
    for (int i = 0; i < 9; i++) {
        sample[i] = texture2D(sampler0, gl_TexCoord[0].st + tc_offset[i]);
    }

//   3 5 3
//   5 8 5   / 40
//   3 5 3

    gl_FragColor =  (3.0*sample[0] + (5.0*sample[1]) + 3.0*sample[2] + 
                    (5.0*sample[3])+  8.0*sample[4]  +(5.0*sample[5])+ 
                     3.0*sample[6] + (5.0*sample[7]) + 3.0*sample[8]) / 40.0;
}