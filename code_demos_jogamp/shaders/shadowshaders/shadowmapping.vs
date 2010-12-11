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
 ** Simple Vertex shader used for GSLS based depth texture shadow mapping wich transforms the
 ** vertex with the camera matrices and the same vertex with the light POV matrix. Lighting wise
 ** it prepares the eyespace+light normal to do per-pixel phong shading with specular in the 
 ** pixel shader.
 **
 **/

varying vec4 shadowcoord;
uniform vec3 lightposition;
varying vec3 N, L;

void main() {
    //shadow matrix transformation
    shadowcoord = gl_TextureMatrix[7] * gl_Vertex;
    //vertex MVP transform
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    vec4 V = gl_ModelViewMatrix * gl_Vertex;
    //eye-space normal
    N = gl_NormalMatrix * gl_Normal;
    L = lightposition - V.xyz;
}

