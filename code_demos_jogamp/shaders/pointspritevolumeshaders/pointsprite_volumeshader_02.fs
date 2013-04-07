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
 ** Simple fragment shader to colorize a pointsprite from the calculated varying passed from
 ** vertex shader (experimental state).
 **
 ** Remark: This routine currently has a couple of rendering problems on NVidia GPUs.
 **/

uniform sampler2D sampler0;
varying float depth;
varying vec4 color;

void main(void) {
    //float normalized_depth = 1.0-(depth/175.0);
    //float normalized_depth = 1.0;
    vec4 pointsprite_fragment = texture2D(sampler0, gl_PointCoord);    
    //float gray = dot(displace_texture.rgb, vec3(0.299, 0.587, 0.114));
    //gl_FragColor = pointsprite_fragment*color*vec4(normalized_depth,normalized_depth,normalized_depth,1);
    gl_FragColor = pointsprite_fragment*color;
}