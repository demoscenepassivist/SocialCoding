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
 ** Fragment shader implementing phong based lighting with specular and VSM using chebyshev 
 ** probabilist prediction for softshadow mapping. Does the chebyshev upper bound calculation
 ** and depthtexture reprojection.
 **
 **/

uniform sampler2D sampler;
uniform float varianceoffset;
varying vec4 shadowcoordinate;
vec4 shadowcoordinatepostw;
uniform float specularexponent;
const vec3 lightcolor = vec3(1.0, 1.0, 1.0);
varying vec3 N, L;

float chebyshevUpperBound(float distance) {
    vec2 moments = texture2D(sampler,shadowcoordinatepostw.xy).rg;
    //surface is before the light occluder ...
    if (distance <= moments.x) {
        return 1.0;
    }
    //fragment is either in shadow or penumbra. 
    //use chebyshev's upper bound to calculate likelyhood
    //for lit fragment ...
    float variance = moments.y - (moments.x*moments.x);
    variance = max(variance,varianceoffset);
    float d = distance - moments.x;
    float p_max = variance / (variance + d*d);
    return p_max;
}

void main() {
    shadowcoordinatepostw = shadowcoordinate / shadowcoordinate.w;
    float shadow = chebyshevUpperBound(shadowcoordinatepostw.z);
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
    fragmentcolor *= shadow;
    gl_FragColor = fragmentcolor;
}
