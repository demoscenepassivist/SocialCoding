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
 ** The fragment shader used for GSLS based depth texture shadow mapping. Compares the z value 
 ** (in light POV) of the vertex rendered with what was rendered to the shadowmap and then decides
 ** if the fragment is in shadow or in light. The generated shadow-weight is then multiplied with the
 ** calculated fragment color to achieve the shadow-effect. Lighting wise the prepared/interpolated
 ** eyespace+light normal is used to do per-pixel phong shading with specular.
 **
 **/

uniform sampler2D shadowmap;
uniform float shadowoffset;
uniform float shadowintensity;
uniform float specularexponent;
const vec3 lightcolor = vec3(1.0, 1.0, 1.0);
varying vec3 N, L;
varying vec4 shadowcoord;

void main() {	
    vec4 shadowCoordinateWdivide = shadowcoord / shadowcoord.w ;
    //glPolygonOffset emulation ...
    shadowCoordinateWdivide.z += shadowoffset;
    float distanceFromLight = texture2D(shadowmap,shadowCoordinateWdivide.st).z;
    float shadow = 1.0;
    if (shadowcoord.w > 0.0) {
        shadow = distanceFromLight < shadowCoordinateWdivide.z ? shadowintensity : 1.0 ;
    }
    //---
    vec3 NN = normalize(N);
    vec4 fragmentcolor = vec4(0.0,0.0,0.0,1.0);
    vec3 NL = normalize(L);
    vec3 NH = normalize(NL + vec3(0.0, 0.0, 1.0));
    float NdotL = max(0.0, dot(NN, NL));
    //accumulate the diffuse contributions
    fragmentcolor.rgb += gl_FrontMaterial.diffuse.rgb * lightcolor * NdotL;
    //accumulate the specular contributions
    if (NdotL > 0.0) {
        //use lightcolor for specular ...
        //fragmentcolor.rgb += lightcolor * pow(max(0.0, dot(NN, NH)), specularexponent);
        //ignore lightcolor and use white lightcolor for specular ...
        fragmentcolor.rgb += pow(max(0.0, dot(NN, NH)), specularexponent);
    }
    fragmentcolor.rgb *= shadow;
    gl_FragColor = fragmentcolor;
}

