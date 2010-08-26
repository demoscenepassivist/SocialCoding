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
 ** Postprocessing filter implementing a 'BOX BLUR' linear convolution using an average 
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

//   1 1 1
//   1 1 1   / 9
//   1 1 1

    gl_FragColor =  (sample[0] + sample[1] + sample[2] + 
                     sample[3] + sample[4] + sample[5] + 
                     sample[6] + sample[7] + sample[8]) / 9.0;
}
