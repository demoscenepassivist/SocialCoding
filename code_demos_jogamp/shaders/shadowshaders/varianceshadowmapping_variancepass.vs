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
 ** Vertex shader implementing phong based lighting with specular and VSM using chebyshev 
 ** probabilist prediction for softshadow mapping. The projection matrix for the depth texture is
 ** is stored in texture matrix 7.
 **
 **/

varying vec4 shadowcoordinate;
varying vec3 N, L;
uniform vec3 lightposition;

void main() {
    shadowcoordinate = gl_TextureMatrix[7] * gl_Vertex;
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    vec4 V = gl_ModelViewMatrix * gl_Vertex;
    //eye-space normal
    N = gl_NormalMatrix * gl_Normal;
    L = lightposition - V.xyz;
    gl_FrontColor = gl_Color;
}
