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
 ** Fragment shader part of "Image Based Lighting" with spherical harmonics. Multiplies the
 ** calculated spherical harmonics diffuse contribution with a prebaked ambient occlusion 
 ** map (blender uv-unwrap) to further refine the fake global illumination impression.
 **
 **/

varying vec3 diffuse_sphericalharmonics;
uniform sampler2D sampler0;

void main(void) {
    gl_FragColor = vec4(diffuse_sphericalharmonics*texture2D(sampler0, gl_TexCoord[0].xy).rgb, 1.0);
}