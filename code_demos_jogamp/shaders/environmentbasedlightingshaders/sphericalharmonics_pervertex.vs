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
 ** Vertex shader implementing "Image Based Lighting" with spherical harmonics as described
 ** in the Siggraph 2001 paper "An Efficient Representation for Irradiance Environment Maps" by 
 ** Ravi Ramamoorthi and Pat Hanrahan. In addition to spherical harmonics lighting the mesh is
 ** shaded with a prebaked ambient occlusion map (blender uv-unwrap) to further refine the fake
 ** global illumination impression (this is done in the corresponding fragment shader).
 **
 **/

const float C1 = 0.429043;
const float C2 = 0.511664;
const float C3 = 0.743125;
const float C4 = 0.886227;
const float C5 = 0.247708;

//grace cathedral spherical harmonic coefficients ...
const vec3 L00  = vec3( 0.78908,  0.43710,  0.54161);
const vec3 L1m1 = vec3( 0.39499,  0.34989,  0.60488);
const vec3 L10  = vec3(-0.33974, -0.18236, -0.26940);
const vec3 L11  = vec3(-0.29213, -0.05562,  0.00944);
const vec3 L2m2 = vec3(-0.11141, -0.05090, -0.12231);
const vec3 L2m1 = vec3(-0.26240, -0.22401, -0.47479);
const vec3 L20  = vec3(-0.15570, -0.09471, -0.14733);
const vec3 L21  = vec3( 0.56014,  0.21444,  0.13915);
const vec3 L22  = vec3( 0.21205, -0.05432, -0.30374);

varying vec3 diffuse_sphericalharmonics;

void main(void) {
    gl_TexCoord[0].xy = gl_MultiTexCoord0.xy;
    vec3 normal = normalize(gl_NormalMatrix*gl_Normal);
    diffuse_sphericalharmonics =
        C1*L22*(normal.x*normal.x-normal.y*normal.y)+
        C3*L20*normal.z*normal.z+
        C4*L00-
        C5*L20+
        2.0*C1*L2m2*normal.x*normal.y+
        2.0*C1*L21*normal.x*normal.z+
        2.0*C1*L2m1*normal.y*normal.z+
        2.0*C2*L11*normal.x+
        2.0*C2*L1m1*normal.y+
        2.0*C2*L10*normal.z;
    gl_Position = gl_ModelViewProjectionMatrix*gl_Vertex;
}