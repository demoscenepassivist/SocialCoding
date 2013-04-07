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
 ** varying from the vertex shader (a procedural fractal-bitmap-orbit trap). The texture is the 
 ** pointsprite itself and varying is the fragment that should be represented by the 2D pointsprite. 
 ** Both fragment are multiplied to color the pointsprite in the color of the generated image.
 **
 **/

#version 120

uniform sampler2D sampler0;
//uniform sampler2D sampler1;
varying float depth;
varying vec4 frag_color;

void main() {
    float normalized_depth = 1.0-(depth/110.0);
    //gl_FragColor = texture2D(sampler0, gl_PointCoord)*vec4(normalized_depth,normalized_depth,normalized_depth,1.0);
    vec4 pointsprite_fragment = texture2D(sampler0, gl_PointCoord);
    //vec4 displace_texture = texture2D(sampler1, gl_TexCoord[0].st);
    //gl_FragColor = pointsprite_fragment*displace_texture;
    gl_FragColor = vec4(frag_color.rgb*texture2D(sampler0, gl_PointCoord).rgb,1.0);
}
