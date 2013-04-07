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
 ** Fragment shader which is used to sample of volume texture used to render a pointsprite cube
 ** (experimental state).
 **
 ** Remark: This routine currently has a couple of rendering problems on NVidia GPUs.
 **/
 
uniform sampler2D sampler0;
uniform sampler3D sampler1;
varying float depth;

void main(void) {
    float normalized_depth = 1.0-(depth/175.0);
    //gl_FragColor = texture2D(sampler0, gl_PointCoord)*vec4(normalized_depth,normalized_depth,normalized_depth,1.0);
    
    vec4 pointsprite_fragment = texture2D(sampler0, gl_PointCoord);
    vec4 displace_texture = texture3D(sampler1, gl_TexCoord[0].xyz);
    
    gl_FragColor = pointsprite_fragment*displace_texture*vec4(normalized_depth,normalized_depth,normalized_depth,1);
}