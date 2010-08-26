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
 ** Fragment shader part of the parallax occlusion mapping program. Calculates the displaced
 ** texture coordinates, the fragment normal based on the tangent space normal map, the specular
 ** exponent of the fragment based on the gloss map and mixes all together with the diffuse map
 ** in respect to the lightvector and the cameravector to get the final fragment color.
 **
 **/

uniform sampler2D sampler0_diffuse;
uniform sampler2D sampler1_gloss;
uniform sampler2D sampler2_normal;
uniform sampler2D sampler3_height;
const vec4 diffusecolor = vec4(1.0,1.0,1.0,1.0);
const vec4 specularcolor = vec4(1.0,1.0,1.0,1.0);
const vec2 scaleBias = vec2(0.035,0.035);
varying vec3 eyevector;
varying vec3 lightvector;

void main() {
    //compute displaced texture coordinates ...
    float height = texture2D(sampler3_height , gl_TexCoord[0].st).r;
    float v = height * scaleBias.r - scaleBias.g;
    vec2 newCoords = gl_TexCoord[0].st + (v * normalize(eyevector).xy);
    //---
    vec3 norm = texture2D(sampler2_normal, newCoords).rgb * 2.0 - 1.0;
    vec4 baseColor = texture2D(sampler0_diffuse,newCoords);
    float dist = length(lightvector);
    vec3 normalizedlightvector = normalize(lightvector);
    float nxDir = max(0.0, dot(norm, normalizedlightvector));
    vec4 diffuse = diffusecolor * nxDir;
    float specularPower = 0.0;
    if(nxDir != 0.0) {
        vec3 cameraVector = eyevector;
        vec3 halfVector = normalize(normalizedlightvector + cameraVector);
        float nxHalf = max(0.0,dot(norm, halfVector));
        specularPower = pow(nxHalf, 64.0);
    }
    vec4 specular = specularcolor * specularPower;
    vec4 specularcolor = texture2D(sampler1_gloss,newCoords);
    gl_FragColor = (diffuse * vec4(baseColor.rgb,1.0)) + vec4(specular.rgb * specularcolor.r, 1.0);
}