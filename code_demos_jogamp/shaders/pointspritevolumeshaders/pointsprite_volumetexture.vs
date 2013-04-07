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
 ** Vertex shader which is used to sample of volume texture used to render a pointsprite cube
 ** (experimental state).
 **
 ** Remark: This routine currently has a couple of rendering problems on NVidia GPUs.
 **/

uniform sampler3D sampler1;
varying float depth;

void main(void) {
    gl_TexCoord[0] = gl_MultiTexCoord0;
    
    vec4 displace_texture = texture3D(sampler1, gl_TexCoord[0].xyz);    
    //float gray = dot(displace_texture.rgb, vec3(0.299, 0.587, 0.114));
 
    vec4 raw_vertex = gl_Vertex;
    //raw_vertex.z -= gray*25;
    
    gl_Position = gl_ModelViewProjectionMatrix*raw_vertex;
    depth = gl_Position.z;
    
    //gl_PointSize = 25.0-(gl_Position.z*0.25);
    gl_PointSize = 6.0;  
}